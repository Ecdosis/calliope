/*
 * This file is part of formatter.
 *
 *  formatter is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  formatter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with formatter.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2011
 */
/**
 * The purpose of this module is to render a particular standoff markup
 * format called "STIL", with the addition of the plain base text and a
 * CSS file, into HTML. There might be formats other than STIL, so this
 * file needs to be called in an interchangeable way. 
 * @author Desmond Schmidt (c) 2011
 */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <unicode/uchar.h>
#include <unicode/ustring.h>
#include "css_property.h"
#include "css_selector.h"
#include "hashmap.h"
#include "attribute.h"
#include "annotation.h"
#include "range.h"
#include "css_rule.h"
#include "range_array.h"
#include "hashset.h"
#include "formatter.h"
#include "cJSON.h"
#include "STIL/STIL.h"
#include "plain_text.h"
#include "utils.h"
#include "memwatch.h"
#include "error.h"
#include "encoding.h"

#ifdef XML_LARGE_SIZE
#if defined(XML_USE_MSC_EXTENSIONS) && _MSC_VER < 1400
#define XML_FMT_INT_MOD "I64"
#else
#define XML_FMT_INT_MOD "ll"
#endif
#else
#define XML_FMT_INT_MOD "l"
#endif

struct userdata_struct
{
    range_array *ranges;
    hashset *props;
    int absolute_off;
    range *current;
};
/**
 * Parse the loaded STIL file
 * @param root the root node
 * @param u the userdata struct
 */
void stil_parse( cJSON *root, struct userdata_struct *u )
{
    cJSON *item = root->child;
    while ( item != NULL )
    {
        if ( strcmp(item->string,"ranges")==0 )
        {
            cJSON *range = item->child;
            while ( range != NULL )
            {
                cJSON *field = range->child;
                u->current = range_create_empty();
                while ( field != NULL )
                {
                    if ( strcmp(field->string,"name")==0 )
                    {
                        UChar u_valuestring[32];
                        str2ustr(field->valuestring,u_valuestring,32);
                        range_set_name( u->current, u_valuestring );
                    }
                    else if ( strcmp(field->string,"len")==0 )
                    {
                        range_set_len( u->current, field->valueint );
                    }
                    else if ( strcmp(field->string,"reloff")==0 )
                    {
                        u->absolute_off += field->valueint;
                        range_set_absolute( u->current, u->absolute_off );
                        range_set_reloff( u->current, field->valueint );
                    }
                    else if ( strcmp(field->string,"removed")==0 )
                    {
                        range_set_removed( u->current, 1 );
                    }
                    else if ( strcmp(field->string,"annotations")==0 )
                    {
                        struct cJSON *sibling = field->child;
                        while ( sibling != NULL )
                        {
                            int child_len = measure_from_encoding( 
                                sibling->child->string, 
                                strlen(sibling->child->string), "UTF-8" );
                            int value_len = measure_from_encoding( 
                                sibling->child->valuestring, 
                                strlen(sibling->child->valuestring), "UTF-8" );
                            UChar *child = calloc(child_len,sizeof(UChar));
                            UChar *value = calloc(value_len,sizeof(UChar));
                            int res1 = convert_from_encoding(
                                sibling->child->string, 
                                strlen(sibling->child->string),
                                child, child_len, "UTF-8" );
                            int res2 = convert_from_encoding(
                                sibling->child->valuestring, 
                                strlen(sibling->child->valuestring),
                                value, value_len, "UTF-8" );
                            annotation *a = NULL;
                            if ( res1 > 0 && res2 > 0 && child != NULL && value != NULL )
                                a = annotation_create_simple(child, value );
                            if ( a != NULL )
                                range_add_annotation( u->current, a );
                            if ( value != NULL )
                                free( value );
                            if ( child != NULL )
                                free( child );
                            sibling = sibling->next;
                        }
                    }
                    // ignore content
                    field = field->next;
                }  
                range = range->next;
                if ( u->current != NULL )
                {
                    UChar *r_name = range_name(u->current);
                    if ( !hashset_contains(u->props, r_name) )
                        hashset_put( u->props, r_name );
                    //fprintf(stderr,"adding range %s\n",r_name);
                    range_array_add( u->ranges, u->current );
                    u->current = NULL;
                }   
            }
        }
        // ignore "style"
        item = item->next;
    }
}
/**
 * Load the markup file with NON-overlapping ranges, reading it using cJSON.
 * @param mdata the overlapping markup data
 * @param mlen its length
 * @param ranges the loaded standoff ranges sorted on absolute offsets
 * @param props store in here the names of all the properties
 * @return 1 if it loaded successfully, else 0
 */
int load_stil_markup( const UChar *mdata, int mlen, range_array *ranges, 
    hashset *props )
{
    int res = 0;
    int cdata_len = measure_to_encoding( (UChar*)mdata, mlen, "UTF-8");
    char *cdata = malloc( cdata_len );
    if ( cdata != NULL )
    {
        int nbytes = convert_to_encoding( (UChar*)mdata, mlen, cdata, 
            cdata_len, "UTF-8" );
        if ( nbytes == cdata_len )
        {
            cJSON *root = cJSON_Parse( cdata );
            if ( root != NULL )
            {
                struct userdata_struct u;
                u.props = props;
                u.ranges = ranges;
                u.absolute_off = 0;
                u.current = NULL;
                stil_parse( root, &u );
                cJSON_Delete( root );
                res = 1;
            }
            else
                warning("failed to parse JSON. mdata len=%d\n",mlen);
        }
        else
            warning("failed to convert to UTF-8\n");
        free( cdata );
    }
    else
        warning("failed to allocate UTF-8 buffer\n");
    return res;
}
