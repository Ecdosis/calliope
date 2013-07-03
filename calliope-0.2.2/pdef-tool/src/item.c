#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "config.h"
#include "hashmap.h"
#include "path.h"
#include "item.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
/**
 * A item is a group of paths of the same type
 */
struct item_struct
{
    // unique identifier fields:
    char *docid;
    int type;
    char *db;
    char *versionID;
    // auxilliary data
    path *paths;
    config *cf;
};
item *item_create( char *docid, int type, char *db )
{
    item *i = calloc( 1, sizeof(item) );
    if ( i != NULL )
    {
        i->docid = strdup( docid );
        if ( db != NULL )
            i->db = strdup( db );
        i->type = type;
    }
    else
        fprintf(stderr,"item:failed to create object\n");
    return i;
}
void item_dispose( item *it )
{
    path *p = it->paths;
    while ( p != NULL )
    {
        path *next = path_next( p );
        path_dispose( p );
        p = next;
    }
    if ( it->cf != NULL )
        config_dispose( it->cf );
    if ( it->docid != NULL )
        free( it->docid );
    if ( it->db != NULL )
        free( it->db );
    if ( it->versionID != NULL )
        free( it->versionID );
    free( it );
}
int item_type( item *it )
{
    return it->type;
}
static char *item_type_str( item *it )
{
    switch ( it->type )
    {
        case MVD_CORTEX:
            return "MVD cortex";
            break;
        case MVD_CORCODE:
            return "MVD corcode";
            break;
        case TEXT_CORTEX:
            return "TEXT cortex";
            break;
        case TEXT_CORCODE:
            return "TEXT corcode";
            break;
        case XML:
            return "XML";
            break;
        case MIXED:
            return "MIXED";
            break;
    }
}
void item_print( item *it )
{
    int npaths = item_num_paths(it);
    char *files = (npaths==1)?"file":"files";
    printf("%s: %s, %d %s", it->docid, item_type_str(it), npaths, files );
    if ( it->cf != NULL )
    {
        printf(", config: ");
        config_print( it->cf );
    }
    if ( it->versionID != NULL )
        printf(", versionID: %s",it->versionID);
    printf("\n");
}
config *item_config( item *it )
{
    return it->cf;
}
char *item_key( item *i )
{
    char *db = (i->db==NULL)?"":i->db;
    int len = strlen(i->docid)+strlen(db)+2;
    char *key = calloc( 1, len );
    snprintf(key,len,"%s%d%s",i->docid,i->type,db);
    return key;
}
char *item_versionID( item *it )
{
    return it->versionID;
}
void item_set_versionID( item *it, char *versionID )
{
    if ( it->versionID != NULL )
        free( it->versionID );
    it->versionID = strdup( versionID );
}
char *item_docid( item *it )
{
    return it->docid;
}
char *item_db( item *it )
{
    return it->db;
}
void item_add_path( item *it, char *p )
{
    if ( it->paths == NULL )
        it->paths = path_create(p);
    else
        path_append( it->paths, p );
}
void item_set_config( item *it, config *cf )
{
    if ( it->cf != NULL )
        config_dispose( it->cf );
    it->cf = cf;
}
/**
 * Is the directory path of the first file exactly equal to the second path?
 * @param pt the full path including the file name
 * @param p the directory path
 * @param plen the length of p
 * @return 1 if p is the FULL directory path of pt else 0
 */
int in_dir( char *pt, char *p, int plen )
{
    if ( plen<strlen(pt) && strncmp(pt,p,plen)==0 )
        return strchr(&pt[plen+1],'/') == NULL;
    else
        return 0;
}
/**
 * Are two fnames equal minus their suffixes?
 * @param a the first path/filename
 * @parma b the second filename/path
 * @return 1 if the paths and filenames are equal not their suffixes
 */
int fnames_equal( char *a, char *b )
{
    char *adotpos = strrchr( a, '.' );
    char *bdotpos = strrchr( b, '.' );
    int adotlen = (adotpos==NULL)?0:strlen(adotpos);
    int bdotlen = (bdotpos==NULL)?0:strlen(bdotpos);
    int alen = strlen( a ) - adotlen;
    int blen = strlen( b ) - bdotlen;
    return alen==blen && strncmp(a,b,alen)==0;
}
/**
 * Does the config path uniquely apply to a file in the current directory?
 * @param it the item
 * @param p the directory path
 * @param fname the full config file path
 * @return 
 */
int item_path_unique( item *it, char *p, char *fname )
{
    path *temp = it->paths;
    int plen = strlen(p);
    while ( temp != NULL )
    {
        char *pt = path_get(temp);
        if ( in_dir(pt,p,plen) && fnames_equal(fname,pt) )
            return 1;
        else
            temp = path_next(temp);
    }
    return 0;
}
int item_path_starts( item *it, char *p, char *fname )
{
    path *temp = it->paths;
    int plen = strlen(p);
    while ( temp != NULL )
    {
        char *pt = path_get(temp);
        if ( strlen(pt)>=plen && strncmp(p,pt,plen)==0 )
            return 1;
        else
            temp = path_next(temp);
    }
    return 0;
}
int item_num_paths( item *it )
{
    int num = 0;
    path *p = it->paths;
    while ( p != NULL )
    {
        num++;
        p = path_next(p);
    }
    return num;
}
/**
 * Get the list of paths we store
 * @param it the item in question
 * @return the path list
 */
path *item_paths( item *it )
{
    return it->paths;
}