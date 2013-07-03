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
#include "part.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
/*
 * Represent part of a mime multipart upload
 */
struct part_struct
{
    char *name;
    char *encoding;
    unsigned char *body;
    part_kind kind;
    int len;
    struct part_struct *next;
};
/**
 * Create a part for later writing out
 * @param name the name of the part which may be a form value
 * @param value the form value or body of file already allocated
 * @param len the length of the body
 * @param kind the kind of the part: form data or file contents
 * @param enc the mime type of the data
 */
part *part_create( const char *name, unsigned char *value, int len, 
    part_kind kind, const char *enc )
{
    part *p = calloc( 1, sizeof(part) );
    if ( p != NULL )
    {
        p->name = strdup( name );
        p->encoding = strdup( enc );
        p->body = value;
        p->kind = kind;
        p->len = len;
        if ( p->name == NULL || p->encoding == NULL )
        {
            fprintf(stderr,"part: failed to copy name or encoding\n");
            free( p );
            p = NULL;
        }
    }
    else
        fprintf(stderr,"part: failed to allocate object\n");
    return p;
}
/**
 * Dispose of a part
 * @param p the part to delete
 */
void part_dispose( part *p )
{
    if ( p->name != NULL )
        free( p->name );
    if ( p->encoding != NULL )
        free( p->encoding );
    if ( p->body != NULL )
        free( p->body );
    free( p );
}
/**
 * Compute the exact size of the part
 * @param p the part in question
 * @param boundary the boundary to use
 * @return the exact amount of memory required by the part
 */
int part_size( part *p, char *boundary )
{
    int used = 0;
    if ( p->kind == formdata )
    {
        used = strlen(boundary)+4;
        used += 43+strlen(p->name);
        used += 2+strlen((char*)p->body);
    }
    else // file
    {
        used = 4+strlen( boundary );
        used += 68+strlen( p->name );
        used += 18+strlen( p->encoding );
        //if ( p->len > 0 )
        //    used += 35; //  content transfer encoding
        used += p->len + 2;
    }
    return used;
}
/**
 * Get the next part in the list
 * @param p the part preceding it
 * @return the next part
 */
part *part_next( part *p )
{
    return p->next;
}
/**
 * Attach this part to the next
 * @param q the part to attach to
 * @param p the part to attach to it
 */
void part_set_next( part *q, part *p )
{
    q->next = p;
}
/**
 * Write the part into the destination data
 * @param p the part to write
 * @param body the body of data to write into
 * @param boundary the mmp boundary to use
 * @param len the length consumed in body
 */
int part_get( part *p, unsigned char *body, char *boundary, int len )
{
    int used = 0;
    if ( p->kind == formdata )
    {
        used = snprintf( (char*)body, len, "--%s\r\n",boundary );
        used += snprintf( (char*)&body[used], len-used,
            "Content-Disposition: form-data; name=\"%s\"\r\n\r\n", p->name );
        used += snprintf( (char*)&body[used], len-used, "%s\r\n", 
            (char*)p->body );
    }
    else // file
    {
        used = snprintf( (char*)body, len, "--%s\r\n",boundary );
        used += snprintf( (char*)&body[used], len-used,
            "Content-Disposition: form-data; name=\"uploadedfile[]\"; "
            "filename=\"%s\"\r\n", p->name );
        used += snprintf( (char*)&body[used], len-used, 
            "Content-Type: %s\r\n\r\n", (char*)p->encoding );
        if ( p->len > 0 )
        {
            memcpy( &body[used], p->body, p->len );
            used += p->len;
        }
        memcpy ( &body[used], "\r\n", 2 );
        used += 2;
    }
    return used;
}
