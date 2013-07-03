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
#include "cJSON.h" 
#include "kv_array.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
#define KEY "key"
#define VALUE "value"
/**
 * Turn a list of cJSON objects with key-value pairs into a string array.
 * Each pointer to a string is allocated and needs to be deallocated.
 * @param array pointer to the JSON array
 * @return an array
 */
char **kv_array_create( cJSON *array )
{
    // count
    int len = 0;
    int j = 0;
    cJSON *item = array->child;
    char *key=NULL;
    char *value=NULL;
    while ( item != NULL )
    {
        len++;
        item = item->next;
    }
    // copy
    item = array->child;
    char **vector = calloc((len+1)*2,sizeof(char*));
    if ( vector != NULL )
    {
        while ( item != NULL )
        {
            cJSON *child = item->child;
            while ( child != NULL )
            {
                if ( strcmp(child->string,KEY)==0
                    && child->type==cJSON_String )
                {
                    if ( key != NULL )
                        free( key );
                    key = strdup(child->valuestring);
                    if ( key == NULL )
                        fprintf(stderr,"kv_array: strdup failed\n");
                }
                else if ( strcmp(child->string,VALUE)==0 
                    && child->type==cJSON_String )
                {
                    if ( value != NULL )
                        free( value );
                    value = strdup(child->valuestring);
                    if ( value == NULL )
                        fprintf(stderr,"kv_array: strdup failed\n");
                }
                if ( key != NULL && value != NULL )
                {
                    vector[j++] = key;
                    vector[j++] = value;
                    key = NULL;
                    value = NULL;
                }
                child = child->next;
            }
            item = item->next;
        }
    }
    else
        fprintf(stderr,"kv_array:failed to allocate\n");
    //debug
/*
    int i=0;
    while ( vector[i] != NULL )
        printf("%s\n",vector[i++]);
*/
    return vector;
}
/**
 * Clone an existing kv_array of allocated keys and values
 * @param kva the array to clone
 * @return a deep copy of the array or NULL if it failed
 */
char **kv_array_clone( char **kva )
{
    int i = 0;
    while ( kva[i] != NULL )
        i++;
    char **copy = calloc( i+1, sizeof(char*) );
    if ( copy != NULL )
    {
        i = 0;
        while ( kva[i] != NULL )
        {
            copy[i] = strdup(kva[i]);
            if (copy[i] == NULL )
            {
                i = 0;
                while ( copy[i] != NULL )
                    free( copy[i] );
                free( copy );
                copy = NULL;
                fprintf(stderr,"kv_array: failed to clone string\n");
                break;
            }
            i++;
        }
    }
    else
        fprintf(stderr,"kv_array: failed to clone array\n");
    return copy;
}
/**
 * Dispose of a kvarray
 * @param kv_array the array
 */
void kv_array_dispose( char **kva )
{
    int i = 0;
    while ( kva[i] != NULL )
    {
        free( kva[i++] );
        if ( kva[i] != NULL )
            free( kva[i++] );
    }
    free( kva );
}
