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
#include <string.h>
#include "response.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
#define BLOCK_SIZE 4096
#define MAX(a,b) (a>b)?a:b
/*
 * Store and parse a HTML response
 */
struct response_struct
{
    char *buffer;
    int pos;
    int allocated;
};
/**
 * Create an instance of a response
 */
response *response_create()
{
    response *r = calloc( 1, sizeof(response) );
    if ( r != NULL )
    {
        r->buffer = malloc( BLOCK_SIZE );
        if ( r->buffer == NULL )
        {
            response_dispose( r );
            r = NULL;
        }
        else
        {
            r->pos = 0;
            r->allocated = BLOCK_SIZE;
        }
    }
    return r;
}
/**
 * Add some text to the response
 * @param r the response in question
 * @param line the line to add
 * @param n its length
 * @return 1 if it worked, else 0
 */
int response_append( response *r, char *line, int n )
{
    if ( r->pos+n+1 > r->allocated )
    {
        int new_allocated = r->allocated + MAX(BLOCK_SIZE,n);
        char *new_buffer = malloc( new_allocated );
        if ( new_buffer != NULL )
        {
            memcpy( new_buffer, r->buffer, r->pos );
            free( r->buffer );
            r->allocated = new_allocated;
        }
        else
        {
            fprintf(stderr,"response: failed to reallocate block\n");
            return 0;
        }
    }
    memcpy( &r->buffer[r->pos], line, n );
    r->pos += n;
    // always null-terminate
    r->buffer[r->pos] = 0;
    return 1;
}
/**
 * Dispose of a response object
 * @param r the response in question
 */
void response_dispose( response *r )
{
    if ( r->buffer != NULL )
        free( r->buffer );
    free( r );
}
/**
 * Is the response OK? If it was a HTML response parse it here and 
 * analyse the return code
 * @param r the response object
 * @return 1 if it was OK, else 0
 */
int response_ok( response *r )
{
    int i,start,code=500,state = 0;
    for ( i=0;i<r->pos;i++ )
    {
        switch ( state )
        {
            case 0: // looking for 1st space
                if ( isspace(r->buffer[i]) )
                {
                    start = i+1;
                    state = 1;
                }
                break;
            case 1: // looking for 2nd space
                if ( isspace(r->buffer[i]) )
                {
                    int len = i-start;
                    char *ptr = malloc( len+1 );
                    if ( ptr != NULL )
                    {
                        // extract the HTTP return code
                        strncpy( ptr, &r->buffer[start], len );
                        ptr[i-start] = 0;
                        code = atoi( ptr );
                        free( ptr );
                    }
                }
                else if ( !isdigit(r->buffer[i]) )
                    state = -1;
                break;
        }
        if ( state == -1 )
            break;
    }
    return code >= 200 && code < 300;
}
void response_dump( response *r )
{
    printf("%s\n", r->buffer );
}
int response_get_len( response *r )
{
    return r->pos;
}
