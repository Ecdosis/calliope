/*
 * This file is part of mmpupload.

    mmpupload is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mmpupload is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mmpupload.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <stdio.h>
#include <dirent.h>
#include <sys/types.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <math.h>
#include "upload.h"
#include "node.h"
#include "response.h"
#include "url.h"
#include "config.h"
#include "utils.h"
#include "mmp.h"
#include "hashmap.h"
#include "path.h"
#include "item.h"
// test memory
#ifdef MEMWATCH
#include "memwatch.h"
#endif
// test timing of read and write routines
#ifdef PROFILE
static long read_time = 0;
static long write_time = 0;
#endif

#define SOCK_TIMEOUT 0.2f
#define MAXLINE 4096
#define MVD_CORTEX_PATH "/import/literal/cortex/"
#define MVD_CORCODE_PATH "/import/literal/corcode/"
#define TEXT_CORTEX_PATH "/import/text/cortex/"
#define TEXT_CORCODE_PATH "/import/text/corcode/"
#define LITERAL_PATH "/import/literal/"
#define XML_PATH "/import/xml/"
#define MIXED_PATH "/import/mixed/"
#define VERSION_KEY "VERSION_"
#define PATH_SEPARATOR '/'
/*
 * Upload PDEF archive 
 */
static char line[MAXLINE];
static char *json_keys[] = {"filter","stripper","splitter","corform",
"style","author","section","version1","title","format"};
/**
 * Write n bytes 
 * @param fd the descriptor to write to
 * @param vptr the data to write
 * @param n its length
 * @return -1 on error, else number of bytes written
 */
static ssize_t writen( int fd, const void *vptr, size_t n )
{
    size_t nleft;
    ssize_t nwritten;
    const char *ptr;
#ifdef PROFILE
    long start = epoch_time();
#endif
    ptr = vptr;
    nleft = n;
    while ( nleft > 0 )
    {
        //printf("about to call write with nleft=%d\n",(int)nleft);
        if ((nwritten = write(fd,ptr,nleft)) <= 0 )
        {
            //printf("nwritten=%d errno=%d\n",(int)nwritten,errno);
            if ( errno == EINTR )
                nwritten = 0;
            else
            {
                n = -1;
                break;
            }
        }
        nleft -= nwritten;
        ptr += nwritten;
    }
#ifdef PROFILE
    long end = epoch_time();
    write_time += end-start;
#endif
    return n;
}
/**
 * Read from a socket and verify that the HTML response was OK
 * @param sock the socket to read from
 * @return 1 if it succeeds or 0 if not
 */
static int readn( int sock )
{
	int n,res=0;
#ifdef PROFILE
    long start = epoch_time();
#endif
    response *r = response_create();
	if ( r != NULL )
    {
        for ( ; ; )
        {
            n=read( sock, line, MAXLINE );
            if ( n < 0 )
            {
                if ( errno == EINTR )
                    fprintf(stderr,"archive: interrupted system call, wrote "
                        "%d bytes already\n",response_get_len(r));
                else if ( errno != EAGAIN )
                {
                    printf( "archive: failed to read. err=%s socket=%d\n",
                    strerror(errno),sock);
                    break;
                }
                // try again
            }
            else if ( n == 0 )
            {
                // just finished reading
                res = response_ok( r );
                response_dump( r );
                break;
            }
            else
            {
                response_append( r, line, n );
            }
        }
        response_dispose( r );
    }
#ifdef PROFILE
    long end = epoch_time();
    read_time += end-start;
#endif
    return res;
}
/**
 * Set the timeout on a socket
 * @param sockfd the socket file descriptor
 * @param secs the number of seconds to timeout after
 * @return 1 if it worked, else 0
 */
static int socket_set_timeout( int sockfd, float secs )
{
    int res;
    struct timeval tv;
    float scaled = 1000000.0f*secs;
    tv.tv_sec = scaled/1000000;
    tv.tv_usec = scaled-(float)(tv.tv_sec*1000000);
    res = setsockopt( sockfd, SOL_SOCKET, SO_RCVTIMEO, &tv,sizeof(tv) );
    if ( res != -1 )
        res = setsockopt( sockfd, SOL_SOCKET, SO_SNDTIMEO, &tv,sizeof(tv) );
    if ( res == -1 )
        fprintf(stderr,"archive: failed to set timeout on socket\n");
    return res==0;
}
/**
 * Upload a composed mmp block to the server
 * @param host the url of the upload service
 * @param body the body of the mmp
 * @param len its length
 * @return 1 if it worked, else 0
 */
static int upload_all( url *host, unsigned char *body, int len )
{
    int res = 0;
    //puts(body);
    char *address = url_get_host( host );
    int port = url_get_port( host );
    int sock = socket( AF_INET, SOCK_STREAM, 0 );
    if ( sock != -1 )
    {
        struct sockaddr_in addr;
        if ( socket_set_timeout(sock, SOCK_TIMEOUT) )
        {
            memset( &addr, 0, sizeof(addr) );
            struct hostent *he = gethostbyname( address );
            if ( he != NULL )
            {
                memcpy( &addr.sin_addr, he->h_addr_list[0], he->h_length );
                addr.sin_family = AF_INET;
                addr.sin_port = htons(port);
                res = connect( sock, (const struct sockaddr *)&addr, sizeof(addr) );
                if ( res == 0 )
                {
                    //fwrite( body, 1, len, stdout );
                    ssize_t n = writen( sock, body, len );
                    printf("wrote %d, tried to write %d bytes\n",(int)n,len);
                    if ( n != len )
                    {
                        fprintf(stderr,
                            "archive: can't send to %s on port %d. error=%s\n",
                            address, port, strerror(errno) );
                        return 0;
                    }
                    else
                    {
                        return readn( sock );
                    }
                }
                else
                    fprintf(stderr,"archive: failed to connect to %s\n",address );
            }
            else
                fprintf(stderr,"archive: couldn't resolve host %s\n",address);
        }
        close( sock );
    }
    else
    {
        fprintf( stderr,
            "archive: failed to open socket. error=%s\n", strerror(errno) );
    }
    return res;
}
/**
 * Upload a composed mmp message to a url
 * @param my_mmp the mmyp ready to go
 * @param my_url the the full url to send it to
 * @return 1 if successful else 0
 */
static int upload_to_url( mmp *my_mmp, char *my_url )
{
    int res = 1;
    int len = 0;
    unsigned char *text = NULL;
    url *u = url_create( (const char*)my_url );
    if ( u != NULL )
    {
        text = mmp_get( my_mmp, &len, "POST", url_get_host(u), 
            url_get_path(u) );
        if ( text != NULL )
        {
            /*FILE *dst = fopen("mmp.txt","w");
            if ( dst != NULL )
            {
                fwrite( text, 1, len, dst );
                fclose( dst );
            } */
            res = upload_all( u, text, len );
            free( text );
        }
        url_dispose( u );
    }
    return res;
}
/**
 * Dispose of the item map
 * @param hm the map to dispose of
 */
static void item_map_dispose( hashmap *hm )
{
    int i,size = hashmap_size( hm );
    char **array = calloc( size, sizeof(char*) );
    if ( array != NULL )
    {
        hashmap_to_array( hm, array );
        for ( i=0;i<size;i++ )
        {
            item *it = hashmap_get( hm, array[i] );
            item_dispose( it );
        }
        free( array );
    }
    hashmap_dispose( hm );
}
/**
 * Set the fields in the mmp file if config is present
 * @param m the mime-multipart object
 * @param it the item
 * @return 1 if it worked
 */
static int set_item_config( mmp *m, item *it )
{
    int res = 1;
    config *cf = item_config( it );
    if ( cf != NULL )
    {
        int i,njson_keys = sizeof(json_keys)/sizeof(char*);
        for ( i=0;i<njson_keys;i++ )
        {
            if ( res && config_get(cf,json_keys[i]) != NULL )
            {
                char *value = config_get(cf,json_keys[i]);
                res = mmp_add_field( m, json_keys[i], value );
                if ( !res )
                    break;
            }
        }
        if ( res )
        {
            i = 0;
            char **versions = (char**)config_get(cf, "versions" );
            if ( versions != NULL && versions[i] != NULL )
            {
                while ( versions[i] != NULL )
                {
                    int keylen = strlen(versions[i])+1+strlen(VERSION_KEY);
                    char *key = malloc( keylen );
                    if ( key != NULL )
                    {
                        snprintf(key,keylen,"%s%s",VERSION_KEY,versions[i]);
                        mmp_add_field(m,key,versions[i+1]);
                        free( key );
                    }
                    i += 2;
                }
            }
        }
    }
    return res;
}
/**
 * Find out where to upload the item
 * @param cf the config that applies
 * @param it the item to upload
 * @param url store the url here
 * @param limit space in url
 * @return 1 if it worked
 */
int get_full_url( item *it, config *cf, char *url, int limit )
{
    int res = 1;
    char *base_url = config_get( cf, "base_url" );
    if ( base_url != NULL )
    {
        // decide which url to send it to
        int kind = item_type( it );
        char *res_path = NULL;
        switch ( kind )
        {
            case MVD_CORTEX:
                res_path = MVD_CORTEX_PATH;
                break;
            case MVD_CORCODE:
                res_path = MVD_CORCODE_PATH;
                break;
            case TEXT_CORTEX:
                res_path = TEXT_CORTEX_PATH;
                break;
            case TEXT_CORCODE:
                res_path = TEXT_CORCODE_PATH;
                break;
            case LITERAL:
                res_path = LITERAL_PATH;
                break;
            case XML:
                res_path = XML_PATH;
                break;
            case MIXED:
                res_path = MIXED_PATH;
                break;
            default:
                res = 0;
                fprintf(stderr,
                    "archive: unknown item type %d\n", kind);
                break;
        }
        if ( res )
        {
            if ( ends_with(base_url,"/") )
                res_path++;
            char *db = (kind==LITERAL)?item_db(it):"";
            snprintf( url, limit, "%s%s%s",base_url, res_path, db );
        }
    }
    else
    {
        res = 0;
        fprintf(stderr,"upload: no base url\n");
    }
    return res;
}
/**
 * Upload all the items in the item-map
 * @param hm the map
 * @return 1 if it worked
 */
static int upload_item_map( hashmap *hm )
{
    int res = 1;
    int size = hashmap_size( hm );
    char **keys = calloc( size, sizeof(char*) );
    if ( keys != NULL )
    {
        int i,res=1;
        hashmap_to_array( hm, keys );
        for ( i=0;i<size;i++ )
        {
            item *it = hashmap_get( hm, keys[i] );
            mmp *m = mmp_create();
            if ( m != NULL )
            {
                path *p = item_paths( it );
                while ( p != NULL )
                {
                    char name[64];
                    path_name( p, name, 64 );
                    res = mmp_add_file( m, name, path_get(p) );
                    p = path_next(p);
                }
                if ( res )
                    res = mmp_add_field( m, "DOC_ID", item_docid(it) );
                if ( res )
                    res = set_item_config( m, it );
                if ( res )
                {
                    char full_url[128];
                    res = get_full_url( it, item_config(it), full_url, 128 );
                    if ( res )
                        res = upload_to_url(m, full_url);
                }
            }
            mmp_dispose( m );
        }
        free( keys );
    }
    else
    {
        fprintf(stderr,"upload: failed to extract hm keys\n");
        res = 0;
    }
    return res;
}
/**
 * Scan a directory for uploadable items, then upload them.
 * @param dir the directory to look for uploadables
 * @return 1 if it worked, else 0
 */
int upload( char *dir )
{
    int res = 1;
    path *head, *tail;
    head = tail = NULL;
    res = path_scan( dir, &head, &tail );
    if ( res )
    {
        hashmap *hm = path_process( head );
        path_find_config( head, hm );
        upload_item_map( hm );
        item_map_dispose( hm );
        path_dispose_all( head );
    }
    else
        res = 0;
#ifdef PROFILE
    fprintf(stderr,"read_time=%ld microseconds; write_time=%ld microseconds\n",
        read_time,write_time);
#endif
    return res;
}
