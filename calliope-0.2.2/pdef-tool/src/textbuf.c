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
#include "textbuf.h"
#include "utils.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
#define BLOCK_SIZE 4096
/*
 * Store text in a resizeable buffer
 */
struct textbuf_struct
{
    char *buf;
    int pos;
    int allocated;
};
/**
 * Create an initial textbuf
 * @return the object primed and ready
 */ 
textbuf *textbuf_create()
{
    textbuf *tb = calloc( 1, sizeof(textbuf) );
    if ( tb != NULL )
    {
        tb->allocated = BLOCK_SIZE;
        tb->buf = malloc( BLOCK_SIZE );
        if ( tb->buf == NULL )
        {
            textbuf_dispose( tb );
            tb = NULL;
            fprintf(stderr,"textbuf: failed to allocate buf\n");
        }
    }
    else
        fprintf(stderr,"textbuf: failed to allocate object\n");
    return tb;
}
/**
 * Dispose of a textbuf possibly partly allcated
 * @param tb the object in question
 */
void textbuf_dispose( textbuf *tb )
{
    if ( tb->buf != NULL )
        free( tb->buf );
    free( tb );
}
/**
 * Add a string to the text buffer
 * @param tb the textbuf object instance
 * @param str the string to add to it
 * @return 1 if it worked else 0
 */
int textbuf_add( textbuf *tb, char *str )
{
    int slen = strlen( str );
    if ( slen+tb->pos+1 >= tb->allocated )
    {
        int newsize = max( slen+tb->allocated, BLOCK_SIZE+tb->allocated );
        char *tbuf = realloc( tb->buf, newsize );
        if ( tbuf == NULL )
        {
            fprintf(stderr,"textbuf: resize failed\n");
            return 0;
        }
        else
        {
            tb->buf = tbuf;
            tb->allocated = newsize;
        }
    }
    memcpy( &tb->buf[tb->pos], str, slen );
    tb->pos += slen;
    // terminating NULL
    tb->buf[tb->pos] = 0;
    return 1;
}
/**
 * Get the contents of this textbuf
 * @param tb a string NULL-terminated
 * @return 
 */
char *textbuf_get( textbuf *tb )
{
    return tb->buf;
}