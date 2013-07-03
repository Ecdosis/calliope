#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <dirent.h>
#include "hashmap.h"
#include "path.h"
#include "config.h"
#include "item.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif

struct path_struct
{
    char *path;
    struct path_struct *next;
};
int res = 1;
path *path_create( char *fname )
{
    path *p = calloc( 1, sizeof(path) );
    if ( p != NULL )
    {
        p->path = strdup( fname );
        if ( p->path == NULL )
        {
            free( p );
            p = NULL;
        }
    }
    if ( p == NULL )
        fprintf(stderr,"path creation failed\n");
    return p;
}
void path_dispose( path *p )
{
    if ( p->path != NULL )
        free( p->path );
    free( p );
}
path *path_dispose_all( path *h )
{
    path *p = h;
    while ( p != NULL )
    {
        path *next = p->next;
        path_dispose( p );
        p = next;
    }
    return NULL;
}
/**
 * Derive the path name 
 */
void path_name( path *p, char *name, int limit )
{
    char *dup = strdup( p->path );
    char *token = strtok( dup, "/" );
    char *last = token;
    while ( token != NULL )
    {
        last = token;
        token = strtok( NULL, "/" );
    }
    if ( last != NULL )
        strncpy( name, last, limit );
    else
        strncpy( name, p->path, limit );
}
char *path_get( path *p )
{
    return p->path;
}
path *path_next( path *p )
{
    return p->next;
}
static int is_directory( char *relpath, char *dirname )
{
    if ( strcmp(dirname,"..")!=0&&strcmp(dirname,".")!=0 )
    {
        int len = strlen(relpath)+strlen(dirname)+1;
        char *fullpath = malloc(len+1);
        if ( fullpath != NULL )
        {
            snprintf(fullpath,len+1,"%s/%s",relpath,dirname);
            DIR *dirp = opendir( fullpath );
            if ( dirp != NULL )
            {
                closedir( dirp );
                free( fullpath );
                return 1;
            }
            else
                free( fullpath );
        }
    }
    return 0;
}
static char *path_extend( char *p, char *ext, int dispose_old )
{
    int len = 0;
    if ( p != NULL )
        len += strlen(p)+1;
    len += strlen( ext )+1;
    char *new_path = calloc(len,1);
    if ( new_path != NULL )
    {
        if ( p != NULL )
        {
            strcpy( new_path, p );
            strcat( new_path, "/" );
            if ( dispose_old )
                free( p );
        }
        strcat( new_path, ext );
    }
    return new_path;
}
/**
 * Scan a directory for files. Don't do anything with them yet.
 * @param dir the directory to scan
 * @param head set to the head of a list of file-paths
 * @param tail set to the current path tail
 */
int path_scan( char *dir, path **head, path **tail )
{
    int res = 1;
    DIR *dp;
    dp = opendir( dir );
    if ( dp != NULL )
    {
        struct dirent *ep = readdir(dp);
        while ( ep != NULL && res )
        {
	        if ( strcmp(ep->d_name,"..")!=0 &&strcmp(ep->d_name,".")!=0 )
            {
                if ( is_directory(dir,ep->d_name) )
	            {
                    char *new_path = path_extend( dir, ep->d_name, 0 );
                    if ( new_path != NULL )
                    {
                        res = path_scan( new_path, head, tail );
                        free( new_path );
                    }
                    else
                        res = 0;
	            }
                else
                {
                    char *new_path = path_extend( dir, ep->d_name, 0 );
                    if ( new_path != NULL )
                    {
                        path *fp = calloc( 1, sizeof(path) );
                        if ( fp != NULL )
                        {
                            fp->path = strdup( new_path );
                            if ( fp->path != NULL )
                            {
                                if ( *head == NULL )
                                    *head = *tail = fp;
                                else
                                {
                                    (*tail)->next = fp;
                                    *tail = fp;
                                }
                            }
                            else
                                res = 0;
                        }
                        else
                            res = 0;
                        free( new_path );
                    }
                    else
                        res = 0;
                }
            }
            ep = readdir( dp ); 
        }
    }
    return res;
}
void path_append( path *fp, char *p )
{
    path *temp = fp;
    while ( temp->next != NULL )
        temp = temp->next;
    temp->next = calloc( 1, sizeof(path) );
    temp = temp->next;
    temp->path = strdup(p);
}
static void print_paths( path *head )
{
    path *fp = head;
    while ( fp != NULL )
    {
        printf("%s\n",fp->path );
        fp = fp->next;
    }
}
static char *chomp( char *str )
{
    int str_len = strlen(str);
    char *pos = strrchr(str,'.');
    if ( pos != NULL )
    {
        int suf_len = strlen( pos );
        str = strdup( str );
        str[str_len-suf_len] = 0;
    }
    return str;
}
static int path_ends( char *p, char *suf )
{
    int plen = strlen(p);
    int slen = strlen(suf);
    if ( slen<plen )
        return strcmp(&p[plen-slen],suf)==0;
    else
        return 0;
}
/**
 * Parse the path for items
 * @param fp the path object
 * @param hm the hashmap to store items in
 */
static void path_parse( path *fp, hashmap *hm )
{
    item *it;
    int state = 0;
    char *key;
    char *old;
    char *current = NULL;
    char *docid = NULL;
    char *db = NULL;
    char *versionID = NULL;         
    int type = 0;
    char *path_copy = strdup( fp->path );
    if ( path_copy != NULL )
    {
        char *token = strtok( path_copy, "/" );
        while ( token != NULL && state >= 0 )
        {
            switch ( state )
            {
                case 0:
                    if ( strlen(token)>0 )
                    {
                        if ( token[0] == '+' )
                        {
                            docid = path_extend( docid, &token[1], 1 );
                            state = 1;
                        }
                        else if ( token[0] == '@' )
                        {
                            db = strdup( &token[1] );
                            type = LITERAL;
                            state = 2;
                        }
                        else if ( state == '%' )
                        {
                            docid = strdup( &token[1] );
                            state = 3;
                        }
                        // else just build path
                    }
                    break;
                case 1: // extending docid
                    if ( token[0] == '%' )
                    {
                        state = 3;
                        docid = path_extend( docid, &token[1], 1 );
                    }
                    else
                        docid = path_extend( docid, token, 1 );
                    break;
                case 2: // literal path
                    docid = path_extend( docid, token, 1 );
                    break;
                case 3: // decide item type
                    if ( strcmp(token,"MVD")==0 )
                        state = 4;
                    else if ( strcmp(token,"TEXT")==0 )
                        state = 5;
                    else if ( strcmp(token,"XML")==0 )
                        state = 6;
                    else if ( strcmp(token,"MIXED")==0 )
                        state = 7;
                    else if ( !path_ends(token,".conf") )
                    {
                        fprintf(stderr,"path:unexpected path %s/%s\n",
                            current,token);
                        state = -1;
                    }
                    break;
                case 4: // MVD type
                    if ( strcmp(token,"cortex.mvd")==0 )
                        type = MVD_CORTEX;
                    else if ( strcmp(token,"corcode")==0 )
                        state = 8;
                    else if ( !path_ends(token,".conf") )
                    {
                        fprintf(stderr,"path:unexpected path %s/%s\n",
                            current,token);
                        state = -1;
                    }
                    break;
                case 5: // TEXT type
                    if ( strcmp(token,"corcode")==0 )
                        state = 9;
                    else if ( strcmp(token,"cortex")==0 )
                    {
                        state = 10;
                        type = TEXT_CORTEX;
                    }
                    else if ( !path_ends(token,".conf") )
                    {
                        fprintf(stderr,"path:unexpected path %s/%s\n",
                            current,token);
                        state = -1;
                    }
                    break;
                case 6: // XML
                    old = token;
                    if ( is_directory(current,token) )
                        versionID = path_extend( versionID, token, 1 );
                    type = XML;
                    break;
                case 7: // MIXED
                    old = token;
                    if ( is_directory(current,token) )
                        versionID = path_extend( versionID, token, 1 );
                    type = MIXED;
                    break;
                case 8: // MVD corcode
                    docid = path_extend( docid, token, 1 );
                    type = MVD_CORCODE;
                    break;
                case 9: // TEXT corcode
                    docid = path_extend( docid, token, 1 );
                    type = TEXT_CORCODE;
                    state = 11;
                    break;
                case 10: case 11: // TEXT cortex,corcode
                    if ( versionID == NULL )
                    {
                        char *first=calloc(strlen(token)+2,1);
                        if ( first != NULL )
                        {
                            strcpy( first,"/");
                            strcat( first, token );
                            versionID = path_extend( versionID, first, 1 );
                            free( first );
                        }
                        else
                        {
                            fprintf(stderr,"path: failed to allocate versionID\n");
                            state = -1;
                        }
                    }
                    else
                        versionID = path_extend( versionID, token, 1 );
                    break;
            }
            current = path_extend( current, token, 1 );
            token = strtok( NULL, "/" );
        }
        if ( type != NO_TYPE && !path_ends(current,".conf") )
        {
            it = item_create( docid, type, db );
            key = item_key( it );
            if ( key != NULL )
            {
                if ( hashmap_contains(hm,key) )
                {
                    item_dispose( it );
                    it = hashmap_get(hm,key);
                }
                else
                    hashmap_put( hm, key, it );
                free( key );
            }
            item_add_path( it, current );
            if ( versionID != NULL )
                item_set_versionID( it, versionID );
        }
        if ( db != NULL )
            free( db );
        if ( docid != NULL )
            free( docid );
        if ( current != NULL )
            free( current );
        if ( versionID != NULL )
            free( versionID );
        free( path_copy );
    }
    else
        fprintf(stderr,"path:failed to duplicate path\n");
}
/**
 * Print out the item map for debugging purposes
 * @param hm the hashmap to print
 */
static void path_print_map( hashmap *hm )
{
    char **keys = calloc( hashmap_size(hm), sizeof(char*) );
    if ( keys != NULL )
    {
        int i;
        hashmap_to_array( hm, keys );
        for ( i=0;i<hashmap_size(hm);i++ )
        {
            item *it = hashmap_get(hm,keys[i]);
            item_print( it );
        }
        free( keys );
    }
}
/**
 * Process the paths by grouping them into items
 * @param head the first path in the list
 * @return a map of docids to items
 */
hashmap *path_process( path *head )
{
    path *p = head;
    hashmap *hm = hashmap_create();
    while ( p != NULL )
    {
        path_parse( p, hm );
        p = p->next;
    }
    return hm;
}
/**
 * Apply a config file to all subordinate items
 * @param hm the map of docid-based keys to items
 * @param p the path-prefix to look for
 * @param fname the file path of the config file
 */
static void path_apply_conf( hashmap *hm, char *p, char *fname )
{
    int i,size = hashmap_size( hm );
    char **keys = calloc( size, sizeof(char*) );
    if ( keys != NULL )
    {
        hashmap_to_array( hm, keys );
        for ( i=0;i<size;i++ )
        {
            item *it = hashmap_get(hm,keys[i]);
            if ( item_path_unique(it,p,fname) )
            {
                config *cf = item_config( it );
                cf = config_update( fname, cf );
                item_set_config( it, cf );
                break;
            }
            else if ( item_path_starts(it,p,fname) )
            {
                config *cf = item_config( it );
                cf = config_update( fname, cf );
                item_set_config( it, cf );
            }
        }
        free( keys );
    }
}
/**
 * Find files ending in ".conf"
 * @param head the head of the list
 * @param hm map of docid-based keys to items (sets of paths of the same type)
 */
void path_find_config( path *head, hashmap *hm )
{
    path *p = head;
    while ( p != NULL )
    {
        char *token = strtok( p->path, "/" );
        char *current = NULL;
        
        while ( token != NULL )
        {
            if ( path_ends(token,".conf") )
            {
                int clen = strlen(current);
                char *fname = calloc(clen+strlen(token)+2,1);
                if ( fname != NULL )
                {
                    strcpy( fname, current );
                    strcat( fname, "/" );
                    strcat( fname, token );
                    path_apply_conf( hm, current, fname );
                    free( fname );
                }
            }
            current = path_extend( current, token, 1 );
            token = strtok( NULL, "/" );
        }
        if ( current != NULL )
            free( current );
        p = path_next( p );
    }
}