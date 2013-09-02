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
    along with mmpupload.  If not, see <http://www.gnu.org/licenses/>.*/
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <string.h>
#include <time.h>
#include "mmp.h"
#include "part.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
/*
 * Represent a mime multipart upload
 */
struct mmp_struct
{
    char *boundary;
    part *parts;
    int has_files;
    int content_len;
};
/**
 * Generate a random boundary of the requisite length
 * @param len the length of the boundary
 * @return the boundary
 */
static char *random_boundary( int len )
{
    int i;
    static char *base64="0123456789ABCDEFGHIJKLMNOPQRSTU"
    "VWXYZabcdefghijklmnopqrstuvwxyz+/";
    char *b = malloc( len+1 );
    if ( b != NULL )
    {
        for ( i=0;i<len;i++ )
        {
            int index = rand() % 64;
            b[i] = base64[index];
        }
        b[len] = 0;
    }
    return b;
}
/**
 * Create a mime multipart object
 * @return the object or NULL on failure
 */
mmp *mmp_create()
{
    srand( (unsigned)time(NULL) );
    mmp *m = calloc( 1, sizeof(mmp) );
    if ( m == NULL )
        fprintf(stderr,"mmp: failed to allocate mime multipart object\n");
    else
    {
        m->boundary = random_boundary( 32 );
        if ( m->boundary == NULL  )
        {
            mmp_dispose( m );
            fprintf( stderr, "mmp: failed to allocate part of mmp object\n" );
            m = NULL;
        }
    }
    return m;
}
/**
 * Dispose of this mmp object
 * @param m the instance in question
 */
void mmp_dispose( mmp *m )
{
    if ( m->boundary != NULL )
        free( m->boundary );
    part *p = m->parts;
    while ( p != NULL )
    {
        part *next = part_next( p );
        part_dispose( p );
        p = next;
    }
    free( m );
}
/**
 * Add the new part to our collection
 * @param m the mmp object
 * @param p the new part
 */
static void mmp_install_part( mmp *m, part *p )
{
    if ( m->parts == NULL )
        m->parts = p;
    else
    {
        part *q = m->parts;
        while ( part_next(q) != NULL )
        {
            q = part_next(q);
        }
        part_set_next( q, p );
    }
}
/**
 * Add an ordinary text field to the mmp object
 * @param m the mmp object instance
 * @param name the name of the field
 * @param value its text value
 * @return 1 if it worked, else 0
 */
int mmp_add_field( mmp *m, char *name, char *value )
{
    char *copy = strdup(value);
    if ( copy != NULL )
    {
        part *p = part_create( name, (unsigned char *)copy, strlen(value), 
            formdata, "text/plain" );
        if ( p != NULL )
        {
            mmp_install_part( m, p );
            return 1;
        }
    }
    else
        fprintf(stderr,"mmp: failed to dup string\n");
    return 0;
}
/**
 * Find an appropriate mime-type for the file
 * @param path the file's name
 * @return a string being a valid mime-type
 */
const char *mime_type( const char *path )
{
    char *pos = strrchr( path, '.' );
    if ( pos == NULL )
        return "text/plain";
    else if ( strcmp(pos,".xml")==0 )
        return "application/octet-stream";
    else if ( strcmp(pos,".zip")==0 )
        return "application/zip";
    else if ( strcmp(pos,".gif")==0 )
        return "image/gif";
    else if ( strcmp(pos,".jpg")==0 )
        return "image/jpeg";
    else if ( strcmp(pos,".png")==0 )
        return "image/png";
    else if ( strcmp(pos,".svg")==0 )
        return "image/svg+xml";
    else
        return "text/plain";
}
/**
 * Add a file's contents to the mmp object
 * @param m the mmp object instance
 * @param name the name of the field
 * @param path the file path
 * @return 1 if it worked, else 0
 */
int mmp_add_file( mmp *m, char *name, char *path )
{
    FILE *src = fopen( path, "r" );
    int success = 0;
    if ( !m->has_files )
    {
        // this dummy file is required - don't ask me why
        m->has_files = 1;
        part *p = part_create( "", NULL, 0, filedata, 
            "application/octet-stream");
        mmp_install_part( m, p );
    }
    if ( src != NULL )
    {
        fseek( src, 0L, SEEK_END );
        int sz = ftell( src );
        if ( sz > 0 )
        {
            fseek ( src , 0L , SEEK_SET ); 
            unsigned char *data = malloc( sz );
            if ( data != NULL )
            {
                int res = fread( data, 1, sz, src );
                if ( res == sz )
                {
                    part *p = part_create( name, (unsigned char *)data, sz, 
                        filedata, mime_type(path)  );
                    if ( p != NULL )
                    {
                        mmp_install_part( m, p );
                        success = 1;
                    }
                }
            }
            else
            {
                fprintf(stderr,"mmp: failed to allocate data size=%d\n",sz);
            }
        }
        else
            fprintf(stderr,"mmp: couldn't read file %s\n",path);
        fclose( src );
    }
    else
        fprintf(stderr,"mmp: couldn't find file %s\n",path);
    return success;
}
/**
 * Get the mime multipart footer size
 * @param m the mmp object
 * @return the number of bytes written
 */
static int mmp_footer_size( mmp *m )
{
    return strlen(m->boundary)+8;
}
/**
 * Get the mime multipart header size
 * @param m the mmp object
 * @return the number of bytes written
 */
static int mmp_header_size( mmp *m, char *method, char *host, char *path )
{
    // HTTP method
    int len = strlen( path );
    len += strlen( method );
    len += 12;  // constant text length
    // Host
    len += 8 + strlen(host);
    // connection close
    len += 19;
    // content type + boundary
    len += 46 + strlen(m->boundary);
    // content-length
    len += 20;  // add length of "length" later
    return len;
}
/**
 * Get the mime multipart header
 * @param m the mmp object
 * @param body the body already allocated correctly to write to
 * @param len the length available in body
 * @param method the HTTP method
 * @param path the path onthe server to the resource
 * @param host the dns name of the host
 * @return the number of bytes written
 */
static int mmp_get_header( mmp *m, unsigned char *body, int len, char *method, 
    char *host, char *path )
{
    int pos = snprintf( (char*)body, len, "%s %s HTTP/1.1\r\n", method, path );
    pos += snprintf( (char*)&body[pos], len-pos, "Host: %s\r\n", host );
    pos += snprintf( (char*)&body[pos], len-pos, "Connection: close\r\n" );
    pos += snprintf( (char*)&body[pos], len-pos, "Content-Type: multipart/form-"
        "data; boundary=%s\r\n",m->boundary );
    pos += snprintf( (char*)&body[pos], len-pos, "Content-Length: %d\r\n\r\n",
        m->content_len );
    return pos;
}
/**
 * Get the mime multipart footer
 * @param m the mmp object
 * @param body the body already allocated correctly to write to
 * @param len the length available in body
 * @return the number of bytes written
 */
static int mmp_get_footer( mmp *m, unsigned char *body, int len )
{
    return snprintf( (char*)body, len, "--%s--\r\n\r\n", m->boundary );
}
/**
 * Get the entire mime multipart body
 * @param m the mmp object
 * @param len contains length of the entire message including header on exit
 * @param method the http method such as POST
 * @param host the host address like localhost
 * @param path the absolute web path to the resource
 * @return body of the mmp 'text' to be freed by caller or NULL
 */
unsigned char *mmp_get( mmp *m, int *len, char *method, char *host, char *path )
{
    int num_parts = 0;
    m->content_len = 0;
    part *p = m->parts;
    while ( p != NULL )
    {
        m->content_len += part_size( p, m->boundary );
        p = part_next( p );
        num_parts++;
    }
    m->content_len += mmp_footer_size(m);
    *len = m->content_len;
    *len += mmp_header_size(m,method,host,path)+log10(m->content_len)+1;
    unsigned char *body = malloc( *len + 1 );
    if ( body != NULL )
    {
        int pos = 0;
        pos = mmp_get_header( m, &body[pos], *len, method, host, path );
        p = m->parts;
        while ( p != NULL )
        {
            pos += part_get( p, &body[pos], m->boundary, *len-pos );
            p = part_next(p);
        }
        pos += mmp_get_footer( m, &body[pos], *len-pos );
        if ( pos == *len )
            return body;
        else
        {
            fprintf(stderr,"actual body length (%d) and predicted "
                "length (%d) don't match\n",pos,*len);
        }
    }
    else
        fprintf(stderr,"mmp: failed to allocate mime multipart body\n");
    if ( body != NULL )
        free( body );
    *len = 0;
    return NULL;
}