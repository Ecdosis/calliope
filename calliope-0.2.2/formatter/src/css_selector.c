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
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdio.h>
#include <unicode/uchar.h>
#include <unicode/ustring.h>
#include "css_selector.h"
#include "error.h"
#include "utils.h"
#include "memwatch.h"

/**
 * Represent a css selector (bit on the left of a css rule)
 */
struct css_selector_struct
{
    /** the html element name */
	UChar *element;
    /** the html class name or xml property name */
	UChar *class;
};
/**
 * Create a css selector manually
 * @param html_name the html name of the selector
 * @param xml_name the class name
 * @param kind the rule kind
 */
css_selector *css_selector_create( UChar *html_name, UChar *xml_name )
{
    css_selector *s = calloc( 1, sizeof(css_selector) );
    if ( s != NULL )
    {
        if ( html_name != NULL )
            s->element = u_strdup(html_name);
        s->class = u_strdup(xml_name);
        if ( s->class == NULL )
        {
            css_selector_dispose(s);
            warning("css_selector: failed to allocate new css selector\n");
            s = NULL;
        }
    }
    else
        warning("css_selector: failed to allocate new selector\n");
    return s;
}
/**
 * Delete a selector and free its memory
 * @param s the selector in question
 */
void css_selector_dispose( css_selector *s )
{
    if ( s->element != NULL )
    {
        free( s->element );
        s->element = NULL;
    }
    if ( s->class != NULL )
    {
        free( s->class );
        s->class = NULL;
    }
    free( s );
}
/**
 * Make a deep copy of this selector.
 * @param s the selector in question
 * @return the cloned selector
 */
css_selector *css_selector_clone( css_selector *s )
{
    css_selector *copy = calloc( 1, sizeof(css_selector) );
    if ( copy == NULL )
        warning( "css_selector: failed to clone selector\n");
    else
    {
        if ( s->element != NULL )
        {
            copy->element = u_strdup(s->element);
            if ( copy->element == NULL )
            {
                warning("css_selector: failed to copy selector element\n" );
                css_selector_dispose( copy );
                return NULL;
            }
        }
        if ( s->class != NULL )
        {
            copy->class = u_strdup(s->class);
            if ( copy->class == NULL )
            {
                warning("css_selector: failed to copy selector class\n" );
                css_selector_dispose(copy);
                copy = NULL;
            }
        }
    }
    return copy;
}
/**
 * Get the element value for a selector
 * @param s the selector in question
 * @return a string
 */
UChar *css_selector_get_element( css_selector *s )
{
    return s->element;
}
/**
 * Get the class value for a selector
 * @param s the selector in question
 * @return a string
 */
UChar *css_selector_get_class( css_selector *s )
{
    return s->class;
}
/**
 * Parse an individual selector. We only accept basic
 * rules of the type element.class. All other types are ignored.
 * @param sel store the selector here
 * @param data the raw CSS data beginning with the selector
 * @param len its length
 * @return the finished selector
 */
css_selector *css_selector_parse( const UChar *data, int len )
{
	int i,end = len-1;
    int start = 0;
	// ignore leading white-space
	while ( u_isspace(data[start]) )
		start++;
    while ( u_isspace(data[end]) )
        end--;
	for ( i=start;i<end;i++ )
	{
		if ( data[i] == '.' && i > start )
		{
            css_selector *sel_temp = calloc(1, sizeof(css_selector));
            if ( sel_temp != NULL )
            {
                //replaced strndup (unavailable BSD)
                sel_temp->element = calloc( (i-start)+1,sizeof(UChar) );
                if ( sel_temp->element != NULL )
                {
					u_strncpy( sel_temp->element, &data[start], i-start );
                    sel_temp->element[(i-start)] = 0;
                }
                sel_temp->class = calloc( (end-i)+1, sizeof(UChar) );
                if ( sel_temp->element == NULL || sel_temp->class == NULL )
                {
                    warning("css_selector: failed to allocate memory for "
                        "selector\n" );
                    css_selector_dispose( sel_temp );
                    sel_temp = NULL;
                }
                else
                {
                    u_strncpy( sel_temp->class, &data[i+1], end-i );
                    sel_temp->class[end-i] = 0;
                }
            }
            else
                warning("css_selector: failed to allocate new selector\n");
			return sel_temp;
		}
        // else we ignore it
	}
	return NULL;
}
