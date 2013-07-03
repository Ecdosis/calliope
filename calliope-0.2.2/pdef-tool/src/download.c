#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include "download.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
#define MAXLINE 4096
#define HOST_LEN 64
#define MESSAGE "GET /pdef/?%s HTTP/1.0\r\n"\
"Host: %s\r\n\r\n"
static char message[256];
static char line[MAXLINE];
/**
 * Is this string a number?
 * @param token the string
 * @return 1 if it is an integer
 */
static int is_number( char *token )
{
    int i,slen = strlen( token );
    for ( i=0;i<slen;i++ )
    {
        if ( !isdigit(token[i]) )
            return 0;
    }
    return 1;
}
/**
 * Is the address a dotted ipv4 address?
 * @param addr the addres to test
 * @return 1 if it is else 0
 */
static int is_dotted( char *addr )
{
    char *addr2 = strdup( addr );
    if ( addr2 != NULL )
    {
        char *token = strtok( addr2, "." );
        int num = 0;
        while ( token != NULL )
        {
            if ( is_number(token) )
                num++;
            else
                break;
            token = strtok( NULL, "." );
        }
        free( addr2 );
        return num==4;
    }
    else
        return 0;
}
/**
 * Break a url into host and port
 * @param url the url to parse
 * @param host the host name to store
 * @param limit length of data in host
 * @param port the port number
 */
static void parse_url( char *url, char *host, int limit, int *port )
{
    char *url_copy = strdup( url );
    if ( url_copy != NULL )
    {
        char *token = strtok( url_copy, "/:" );
        *port = 80;
        while ( token != NULL )
        {
            if ( strlen(token) > 0 )
            {
                if ( is_number(token) )
                    *port = atoi(token);
                else
                    strncpy( host, token, limit );
            }
            token = strtok( NULL, "/:" );
        }
        free( url_copy );
    }
}
/**
 * Convert the host name to a dotted notation if it is not so already (ipv4)
 * @param host the host name to convert
 * @param dotted target dotted ipv4 representation
 * @param limit amount of store in dotted
 * @return 1 if it worked else 0
 */
static int gethost( char *host, char *dotted, int limit )
{
    int res = 0;
    if ( is_dotted(host) )
        strncpy( dotted, host, limit );
    struct hostent *he = gethostbyname( host );
    if ( he != NULL )
    {
        int i = 0;
        if ( he->h_addr_list[0] != NULL) 
        {
            char *addr = inet_ntoa( *(struct in_addr*)(he->h_addr_list[i++]));
            if ( addr != NULL )
            {
                snprintf( dotted, limit, "%s", addr );
                res = 1;
            }
        }
    }
    return res;
}
/**
 * Print the formats array as a set of GET parameters
 * @param formats the formats to print
 * @param dest the destination string
 * @param limit the number of chars in dest
 */
static void print_formats( char **formats, char *dest, int limit )
{
    int i=0;
    memset( dest, 0, limit );
    while ( formats[i] != NULL )
    {
        strncat( dest, "FORMAT=", limit );
        strncat( dest, formats[i++], limit );
    }
}
/**
 * Skip over the HTTP header returned on downloading
 * @param resp the response from the service
 * @param pos VAR param update to point to first valid byte of response
 */
static void skip_http_header( char *resp, int *pos )
{
    int i,len = strlen(resp)-4;
    for ( i=0;i<len;i++ )
    {
        if ( resp[i] == '\r' && resp[i+1] == '\n' && resp[i+2] == '\r' 
            && resp[i+3] == '\n' )
        {
            *pos = i+4;
            break;
        }
    }
}
/**
 * Read from an open socket and write to the destination file
 * @param sock the socket to read from
 * @param dst the file to write the read bytes to
 * @return the number of bytes read
 */
static int readn( int sock, FILE *dst )
{
	int n,total = 0;
    int first = 1;
	for ( ; ; )
    {
        n=read( sock, line, MAXLINE );
        if ( n < 0 )
        {
            total = -1;
			fprintf( stderr,"failed to read. err=%s socket=%d\n",
                strerror(errno),sock);
            break;
        }
        else if ( n == 0 )
        {
            // just finished reading
            break;
        }
		else
        {
            int skip = 0;
            if ( first )
            {
                skip_http_header( line, &skip );
                first = 0;
            }
            int res = fwrite( &line[skip], 1, n-skip, dst );
            if ( res != n-skip )
            {
                if ( res >0 )
                    total += res;
                break;
            }
            total += n-skip;
        }
    }
	return total;
}
/**
 * Replace illegal characters in the parameter list with their escaped chars
 * @param str the string to escape
 * @param limit
 */
static void url_escape( char *str, int limit )
{
    int i,j;
    char *rep = calloc( limit, 1 );
    if ( rep != NULL )
    {
        for ( j=0,i=0;i<limit&&j<limit-3;i++ )
        {
            if ( str[i] == ' ' )
            {
                rep[j++] = '%';
                rep[j++] = '2';
                rep[j++] = '0';
            }
            else if ( str[i] == '/' )
            {
                rep[j++] = '%';
                rep[j++] = '2';
                rep[j++] = 'F';
            }
            else
                rep[j++] = str[i];
        }
        strncpy( str, rep, limit );
        free( rep );
    }
}
/**
 * Download to disk a portion of the server's DSE data
 * @param url the url with which to query for the PDEF web-server
 * @param formats an array of formats: MIXED,XML,MVD or TEXT
 * @param docid a wildcard-terminated prefix for the documents to download
 * @param name the name of the downloaded archive
 * @param zip_type the type of zip, either zip or tar_gz
 * @param add_required if 1 then add required corforms and configs
 * @return 1 if it worked else 0
 */
int download( char *url, char **formats, char *docid, char *name, 
    char *zip_type, int add_required )
{
    int sock = socket( AF_INET, SOCK_STREAM, IPPROTO_TCP );
    if ( sock >= 0 )
    {
        char host[HOST_LEN];
        int res,port;
        char dotted[32];
        struct sockaddr_in addr;
        parse_url( url, host, HOST_LEN, &port );
        res = gethost( host, dotted, 32 );
        if ( res )
        {
            memset( &addr, 0, sizeof(addr) );
            addr.sin_family = AF_INET;
            res = inet_pton( AF_INET, dotted, &addr.sin_addr.s_addr );
            if ( res )
            {
                addr.sin_port = htons(port);
                res = connect( sock,(struct sockaddr*)&addr,
                    sizeof(struct sockaddr));
                if ( res == 0 )
                {
                    char params[128];
                    char forms[128];
                    char *add_reqd_str = (add_required)?"true":"false";
                    print_formats( formats, forms, 128 );
                    // add params
                    snprintf(params,128,
                        "DOC_ID=%s&add_required=%s&NAME=%s&zip_type=%s&%s",
                        docid,add_reqd_str,name,zip_type,forms);
                    url_escape( params, 128 );
                    if ( port != 80 )
                    {
                        char port_str[32];
                        snprintf(port_str,32,":%d",port);
                        strncat( host, port_str, HOST_LEN );
                    }
                    snprintf( message, 256, MESSAGE, params, host );
                    res = send( sock, message, strlen(message), 0 );
                    if ( res == -1 )
                        printf("err=%s\n",strerror(errno));
                    else
                    {
                        char dst_name[128];
                        char *suffix = (strcmp(zip_type,"zip")==0?"zip":"tar.gz");
                        snprintf( dst_name, 128, "%s.%s", name, suffix );
                        FILE *dst = fopen( dst_name, "w" );
                        int nbytes = readn( sock, dst );
                        printf("nbytes=%d\n",nbytes);
                        fclose( dst );
                    }
                }
            }
            else
                fprintf(stderr,"download: inet_pton failed\n");
        }
        else
            fprintf(stderr,"download: gethost failed\n");
    }
}
#ifdef TEST_DOWNLOAD
int main( int argc, char **argv )
{
    char *formats[2];
    formats[0] = "MVD";
    formats[1] = NULL;
    int res = download( "http://localhost:8080/", formats, 
        "english/shakespeare/.*", "archive", 
        "tar_gz" );
}
#endif