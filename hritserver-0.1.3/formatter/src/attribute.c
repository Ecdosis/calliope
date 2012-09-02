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
#include <string.h>
#include "attribute.h"
#include "error.h"
#include "memwatch.h"
struct attribute_struct
{
    char *name;
    char *value;
    attribute *next;
};
/**
 * Create an attribute
 * @param name its name
 * @param value its value
 * @return  the finished attribute or NULL
 */
attribute *attribute_create( char *name, char *value )
{
    attribute *attr = calloc( 1, sizeof(attribute) );
    if ( attr != NULL )
    {
        attr->name = strdup( name );
        if ( attr->name == NULL )
        {
            attribute_dispose( attr );
            warning("attribute: failed to allocate attribute name\n");
            attr = NULL;
        }
        else
        {
            attr->value = strdup( value );
            if ( attr->value == NULL )
            {
                attribute_dispose( attr );
                warning("attribute: failed to allocate attribute value\n");
                attr = NULL;
            }
        }
    }
    else
        warning("attribute: failed to allocate attribute struct\n");
    return attr;
}
/**
 * Clone an existing attribute
 * @param attr the attribute to clone
 * @return the attribute or NULL
 */
attribute *attribute_clone( attribute *attr )
{
    return attribute_create( attr->name, attr->value );
}
/**
 * Dispose of an attribute
 * @param attr the attr to throw away
 */
void attribute_dispose( attribute *attr )
{
    if ( attr->next != NULL )
        attribute_dispose( attr->next );
    if ( attr->name != NULL )
    {
        free( attr->name );
        attr->name = NULL;
    }
    if ( attr->value != NULL )
    {
        free( attr->value );
        attr->value = NULL;
    }
    free( attr );
    attr = NULL;
}
/**
 * Add one attribute onto the end of the list of which we are a part
 * @param attrs the list of attributes
 * @param attr the new attribute to append to the end
 */
void attribute_append( attribute *attrs, attribute *attr )
{
    while ( attrs->next != NULL )
        attrs = attrs->next;
    attrs->next = attr;
}
char *attribute_get_name( attribute *attr )
{
    return attr->name;
}
char *attribute_get_value( attribute *attr )
{
    return attr->value;
}
attribute *attribute_get_next( attribute *attr )
{
    return attr->next;
}
int attribute_count( attribute *attr )
{
    int count = 0;
    while ( attr != NULL )
    {
        count++;
        attr = attr->next;
    }
    return count;
}