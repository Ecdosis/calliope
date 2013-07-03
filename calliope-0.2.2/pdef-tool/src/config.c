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
#include "config.h"
#include "utils.h"
#include "hashmap.h"
#include "kv_array.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
#define STRING_KIND 1
#define ARRAY_KIND 2
/*
 * A config file provides key-value mappings for child directories
 */
struct config_struct
{
    int kind;
    char *key;
    void *value;
    struct config_struct *next;
};
/**
 * Create a new config
 * @return an initialised config with a working map
 */
config *config_create()
{
    config *cf = calloc(1,sizeof(config));
    if ( cf == NULL )
        fprintf(stderr,"config: failed to create config object\n");
    return cf;
}
/**
 * Make a deep copy of the config object
 * @param cf the config object
 * @return a complete copy thereof
 */
config *config_clone( config *cf )
{
    config *head = NULL;
    config *tail = NULL;
    while ( cf != NULL )
    {
        config *copy = config_create();
        if ( head == NULL )
        {
            head = copy;
            tail = head;
        }
        copy->kind = cf->kind;
        copy->key = strdup( cf->key );
        if ( copy->kind == STRING_KIND )
            copy->value = strdup( (char*)cf->value );
        else
            copy->value = kv_array_clone((char**)cf->value);
        if ( cf->key == NULL || cf->value == NULL )
        {
            fprintf(stderr,"config: failed to clone config\n");
            head = config_dispose( head );
            break;
        }
        if ( copy != head )
        {
            tail->next = copy;
            tail = copy;
        }
        cf = cf->next;
    }
    return head;
}
/**
 * Dispose of a config object. Complex because the values in the map must 
 * be disposed of properly
 * @param cf the object to dispose
 */
void *config_dispose( config *cf )
{
    while ( cf != NULL )
    {
        config *next = cf->next;
        if ( cf->key != NULL )
            free( cf->key );
        if ( cf->value != NULL )
        {
            if ( cf->kind == STRING_KIND )
                free( cf->value );
            else if ( cf->kind == ARRAY_KIND )
                kv_array_dispose( (char**)cf->value );
        }
        free( cf );
        cf = next;
    }
    return NULL;
}
void config_print( config *cf )
{
    printf("{");
    while ( cf != NULL )
    {
        if ( cf->key != NULL )
            printf( "%s: ", cf->key );
        if ( cf->value != NULL )
        {
            if ( cf->kind == STRING_KIND )
                printf( "%s",(char*)cf->value );
            else if ( cf->kind == ARRAY_KIND )
            {
                int i = 0;
                printf("[");
                char **tmp = (char**)cf->value;
                while ( tmp[i] != NULL )
                {
                    printf("%s: %s",tmp[i],tmp[i+1]);
                    if ( tmp[i+2] != NULL )
                        printf(", ");
                    i += 2;
                }
                printf("]");
            }
        }
        if ( cf->next != NULL )
            printf(", ");
        cf = cf->next;
    }
    printf("}");
}
/**
 * Add a key-value entry to the config list
 * @param cf the config head of list
 * @param key the key
 * @param kind its kind
 * @param value its value
 * @return 1 if it worked else 0
 */
int config_add( config *cf, char *key, int kind, void *value )
{
    int res = 0;
    config *cf_new;
    if ( cf->key == NULL )
        cf_new = cf;
    else
    {
        config *temp = cf;
        cf_new = config_create();
        while ( temp->next != NULL )
            temp = temp->next;
        temp->next = cf_new;
    }
    if ( cf_new != NULL )
    {
        cf_new->key = strdup( key );
        cf_new->kind = kind;
        if ( kind == ARRAY_KIND )
            cf_new->value = value;
        else
            cf_new->value = strdup( (char*)value );
        res = cf_new->key!=NULL&&cf_new->value != NULL;
    }
    return res;
}
/**
 * Read the keys in the new json file and add them or overwrite them in a 
 * copy of the parent
 * @param parent the parent config to copy
 * @param root the new JSON document
 * @return a clone of the existing config or a new config
 */
config *config_parse( config *parent, cJSON *root )
{
    config *cf;
    int res = 1;
    if ( parent == NULL )
        cf = config_create();
    else
        cf = config_clone( parent );
    if ( cf != NULL )
    {
        cJSON *child = root->child;
        char **array;
        while ( res && child != NULL )
        {
            char buffer[32];
            switch ( child->type )
            {
                case cJSON_False:
                    res = config_add(cf,child->string,STRING_KIND,"false");
                    break;
                case cJSON_True:
                    res = config_add(cf,child->string,STRING_KIND,"true");
                    break;
                case cJSON_NULL:
                    res = config_add(cf,child->string,STRING_KIND,"null");
                    break;
                case cJSON_Number:
                    snprintf(buffer,32,"%e",child->valuedouble);
					res = config_add(cf,child->string,STRING_KIND,buffer);
                    break;
                case cJSON_String:
                    res = config_add(cf,child->string,STRING_KIND,
                        child->valuestring);
                    break;
                case cJSON_Array:
                    // we only handle {"key", "value"} arrays
                    array = kv_array_create(child);
                    if ( array != NULL )
                        res = config_add(cf,child->string,ARRAY_KIND,array);
                    else
                        fprintf(stderr,
                            "config: failed to allocate array pointer\n");
                    break;
                case cJSON_Object:
                    break;
            }
            child = child->next;
        }
        if ( !res )
        {
            fprintf(stderr,"config: failed to parse\n");
            cf = config_dispose( cf );
        }
    }
    return (cf==NULL)?parent:cf;
}
/**
 * Get the object contained in this config 
 * @param cf the config file
 * @param key the key for the object stored
 * @return NULL if not found else the object (user must cast)
 */
void *config_get( config *cf, char *key )
{
    config *temp = cf;
    while ( temp != NULL && strcmp(temp->key,key)!=0 )
        temp = temp->next;
    return (temp==NULL)?NULL:temp->value;
}
/**
 * Load the config file
 * @param path the path to the config file
 * @param parent the parent config to update but not modify, may be NULL
 * @return a cloned and updated config or NULL
 */
config *config_update( char *path, config *parent )
{
    config *cf = parent;
    FILE *fp = fopen( path, "r" );
    if ( fp != NULL )
    {
        int sz = file_size( fp );
        if ( sz > 0 )
        {
            char *mdata = malloc( sz );
            if ( mdata != NULL )
            {
                int read = fread( mdata, 1, sz, fp );
                if ( read == sz )
                {
                    cJSON *root = cJSON_Parse( mdata );
                    if ( root != NULL )
                    {
                        cf = config_parse(parent,root);
                        cJSON_Delete( root );
                    }
                    else
                        fprintf(stderr,"archive: failed to parse JSON\n");
                }
                else
                    fprintf(stderr,"archive: failed to load config\n");
                free( mdata );
            }
            else
                fprintf(stderr,"archive: failed to allocate file buffer\n");
        }
        else
            fprintf(stderr,"archive: failed to read %s\n",path);
        fclose( fp );
    }
    else
        fprintf(stderr,"archive: failed to open %s\n",path);
    return cf;
}

