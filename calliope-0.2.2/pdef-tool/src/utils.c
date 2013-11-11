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
#include <string.h>
#include <stdio.h>
#include <dirent.h>
#ifdef MEMWATCH
#include "memwatch.h"
#endif
/*
 * Various utilities required by the mmpupload program
 */
char *uploadable[] = {".json",NULL};
/**
 * Get the size of an open file
 * @param fp the pointer to the open file
 * @return its length of -1 on failure
 */
int file_size( FILE *fp )
{
    fseek( fp, 0L, SEEK_END );
    int sz = ftell( fp );
    if ( sz > 0 )
        fseek( fp , 0L , SEEK_SET ); 
    return sz;
}
/**
 * Does a directory contain a particular file name?
 * @param dir the directory to search
 * @param name the file to look for
 * @return 1 if it is there, else 0
 */
int contains_file( char *dir, char *name )
{
    DIR *dirp = opendir(dir);
    struct dirent *dp = readdir( dirp );
    while ( dp != NULL )
    {
        if ( strcmp(dp->d_name,name)==0 )
            return 1;
        dp = readdir( dirp );
    }
    closedir( dirp );
    return 0;
}
/**
 * Is this filename a directory?
 * @param relative path to the folder from the current WD
 * @return 1 if it is else 0
 */
int is_directory( char *relpath, char *dirname )
{
    if ( strcmp(dirname,"..")!=0&&strcmp(dirname,".")!=0 )
    {
        int len = strlen(relpath)+strlen(dirname)+1;
        char *path = malloc(len+1);
        if ( path != NULL )
        {
            snprintf(path,len+1,"%s/%s",relpath,dirname);
            DIR *dirp = opendir( path );
            if ( dirp != NULL )
            {
                closedir( dirp );
                free( path );
                return 1;
            }
            else
                free( path );
        }
    }
    return 0;
}
/**
 * Is this folder a set of files to merge into an MVD?
 * @param fname name of a folder
 * @return 1 if it is else 0
 */
int is_mvd_name( char *fname )
{
    return strlen(fname)>0&&fname[0]=='%';
}
/**
 * Is this folder name the start of a docid?
 * @param fname name of a folder
 * @return 1 if it is else 0
 */
int is_docid_name( char *fname )
{
    return strlen(fname)>0&&strncmp(fname,"+",1)==0;
}
/**
 * Is this folder name the start of a literal path (including the database)?
 * @param fname name of a folder
 * @return 1 if it is else 0
 */
int is_literal_name( char *fname )
{
    return strlen(fname)>0&&strncmp(fname,"@",1)==0;
}
/**
 * Does a string end with a particular suffix?
 * @param string the string in question
 * @param suffix the putative suffix
 * @return 1 if it does else 0
 */
int ends_with( char *string, char *suffix )
{
    int len1 = strlen( string );
    int len2 = strlen( suffix );
    if ( len2 <= len1 )
    {
        int i,pos;
        for ( i=0,pos=len1-len2;i<len2;i++,pos++ )
            if ( string[pos] != suffix[i] )
                return 0;
        return 1;
    }
    return 0;
}
/**
 * Does a string start with a particular prefix?
 * @param string the string in question
 * @param prefix the putative prefix
 * @return 1 if it does else 0
 */
int starts_with( char *string, char *prefix )
{
    int len1 = strlen( string );
    int len2 = strlen( prefix );
    if ( len2 <= len1 )
    {
        int i,pos;
        for ( i=0;i<len2;i++ )
        {
            if ( string[i] != prefix[i] )
                return 0;
        }
        return 1;
    }
    return 0;
}
/**
 * Simple max function
 * @param a an int 
 * @param b another int
 * @return greater of a or b
 */
int max( int a, int b )
{
    return (a>b)?a:b;
}
/**
 * Compare a string to the contents of a test file
 * @param file the file to load and compare 
 * @param text the text to compare to the file contents
 * @param line store the line number of the mismatch
 * @param pos store the position of the mismatch
 * @return 1 if all characters were the same else 0
 */
int compare_to_file( const char *file, char *text, int *line, int *pos )
{
    FILE *src = fopen( file, "r" );
    if ( src != NULL )
    {
        int len = file_size( src );
        int tlen = strlen( text );
        char *fcont = malloc( len );
        if ( fcont != NULL )
        {
            int lineno = 1;
            int charno = 0;
            int n = fread( fcont, 1, len, src );
            if ( n == len )
            {
                int i;
                for ( i=0;i<len&&i<tlen;i++ )
                {
                    if ( fcont[i] != text[i] )
                    {
                        *line = lineno;
                        *pos = charno;
                        free( fcont );
                        return 0;
                    }
                    else if ( fcont[i] == '\n' )
                    {
                        lineno++;
                        charno = 0;
                    }
                }
                free( fcont );
                return 1;
            }
            else
                fprintf(stderr,"compare_to_file: failed to read %s\n",file );
            free( fcont );
        }
        else
            fprintf(stderr,"compare_to_file: failed allocation\n");
    }
    else
        fprintf(stderr,"compare_to_file: %s not available\n",file);
}
/**
 * Allocate a compound path that must be freed by the caller
 * @param parent the path to the parent directory
 * @param file the file or directory name
 * @param sep the path separator
 * @return an allocated path or NULL
 */
static char *allocate_general_path( char *parent, char *file, const char *sep )
{
    int res,len = strlen(parent)+strlen(file)+strlen(sep)+1;
    char *path = malloc( len );
    if ( path != NULL )
    {
        res = snprintf( path, len, "%s%s%s", parent, sep, file );
        if ( res == len-1 )
            return path;
        else
            free( path );
    }
    return NULL;
}
/**
 * Allocate a file path
 * @param parent the parent dir path
 * @param file the actual file or dir name
 * @return an allocated path caller must free
 */
char *allocate_path( char *parent, char *file )
{
    return allocate_general_path( parent, file, "/" );
}
/**
 * Replace spaces with "%20" in an allocated string
 * @param str the string to update
 * @param replace if 1 replace the original string
 * @param copied if str was reallocated set this to 1
 * @return 1 if it succeeded else 0
 */
static int escape_spaces( char **str, int replace, int *copied )
{
    int nspaces = 0;
    int j,i = 0;
    char *ptr = *str;
    *copied = 0;
    // count spaces
    while ( ptr[i] != 0 )
    {
        if ( ptr[i] == ' ' )
            nspaces++;
        i++;
    }
    // replace them and reallocate str
    if ( nspaces > 0 )
    {
        char *rep = calloc( strlen(*str)+1+(nspaces*2), 1 );
        if ( rep != NULL )
        {
            i = j = 0;
            ptr = *str;
            while ( ptr[i] != 0 )
            {
                if ( ptr[i] == ' ' )
                {
                    rep[j++] = '%';
                    rep[j++] = '2';
                    rep[j++] = '0';
                }
                else
                    rep[j++] = ptr[i];
                i++;
            }
            if ( replace )
                free( *str );
            *str = rep;
            *copied = 1;
            return 1;
        }
        else
        {
            fprintf(stderr,"utils: failed to reallocate string\n");
            return 0;
        }
    }
    else
        return 1;
}
/**
 * Allocate a docid
 * @param parent the parent doc path
 * @param name the subpath
 * @return an allocated path caller must free or NULL if it failed
 */
char *allocate_docid( char *parent, char *name )
{
    char *docid = NULL;
    char *suffix = strrchr(name,'.');
    char *bare_name=NULL;
    int copied;
    if ( suffix!=NULL&&strcmp(suffix,".json")==0 )
    {
        bare_name = malloc( strlen(name)-4 );
        if ( bare_name != NULL )
        {
            char *esc_bare_name = bare_name;
            int len = strlen(name)-5;
            strncpy( bare_name, name, len);
            bare_name[len] = 0;
            if ( escape_spaces(&esc_bare_name,1,&copied) )
                docid = allocate_general_path( parent, esc_bare_name, "%2F" );
            if ( copied )
                free( esc_bare_name );
            free( bare_name );
            
        }
        else
            fprintf(stderr,"utils: failed to allocate docid\n");
    }
    else
    {
        char *temp = name;
        if ( escape_spaces(&temp,0,&copied) )
        {
            docid = allocate_general_path( parent, temp, "%2F" );
            if ( copied )
                free( temp );
        }
    }
    return docid;
}
/**
 * Swap the last component in a path for a new filename
 * @param path the path to swap
 * @param name the name to replace 
 * @return the new path allocated and freeable
 */
char *swap_file(const char *path, const char *name )
{
    int plen,nlen;
    char *p = strrchr( path, '/' );
    if ( p != NULL )
        plen = strlen(path)-strlen(p);
    else
        plen = 0;
    nlen = strlen(name);
    char *new_path = malloc( plen+2+nlen );
    if ( new_path != NULL )
    {
        if ( plen > 0 )
        {
            memcpy( new_path, path, plen );
            memcpy( &new_path[plen], "/", 1 );
            plen++;
        }
        memcpy( &new_path[plen], name, nlen );
        new_path[nlen+plen] = 0;
    }
    else 
        fprintf( stderr, "swap_file: allocation failed\n" );
    return new_path;
}
/**
 * et the file name as last component of a path
 * @param path the path
 * @return the file name
 */
char *file_name( char *path )
{
    char *slashpos = strrchr( path, '/' );
    if ( slashpos != NULL )
        return slashpos+1;
    else
        return path;
}
/**
 * Create an mvd name from a percent-prefix dir name
 * @param fname
 * @return 
 */
char *mvd_name( char *dir_name )
{
    if ( strlen(dir_name)>0&&dir_name[0]=='%' )
        return &dir_name[1];
    else
        return dir_name;
}
/**
 * Is the given file uploadable?
 * @param name the bare file name (not a path)
 * @return 1 if it is else 0
 */
int is_uploadable( char *name )
{
    int i = 0;
    char **temp = uploadable;
    while ( temp[i] != NULL )
    {
        int tlen = strlen(temp[i]);
        int nlen = strlen(name);
        if ( nlen>tlen && strcmp(&name[nlen-tlen],temp[i])==0 )
            return 1;
        i++;
    }
    return 0;
}
/**
 * Count the number of a particular char in a string
 * @param str the string possibly NULL or empty
 * @param token the char to count
 * @return the number of times token occurs in str
 */
int count_chars( char *str, char token )
{
    int count = 0;
    if ( str != NULL )
    {
        char *temp = str;
        while ( temp != NULL )
        {
            temp = strchr( temp, token );
            if ( temp != NULL )
            {
                count++;
                if ( strlen(temp)>0 )
                    temp++;
            }
        }
    }
    return count;
}
/**
 * Get the current time in microseconds
 * @return the time in microseconds since the epoch
 */
long epoch_time()
{
    struct timeval tv;
    gettimeofday( &tv, NULL );
    return tv.tv_sec*1000000+tv.tv_usec;
}
/**
 * Split a path into its parent relative path and its name. 
 * @param rel_path on entry the path including the name, on exit maybe ""
 * @param name on exit just the name
 * @return 1 if it worked (caller MUST deallocate *name and *rel_path!), else 0
 */
int split_path( char **rel_path, char **name )
{
    int res = 0;
    int name_n,path_n;
    char *name_part,*path_part;
    char *slash = strrchr(*rel_path,'/');
    if ( slash == NULL )
    {
        name_part = *rel_path;
        name_n = strlen( name_part );
        path_part = "";
        path_n = 0;
    }
    else
    {
        name_part = &slash[1];
        name_n = strlen( name_part );
        path_part = *rel_path;
        path_n = strlen(path_part)-(name_n+1);
    }
    *name = malloc( name_n+1 );
    if ( *name != NULL )
    {
        strncpy( *name, name_part, name_n );
        (*name)[name_n] = 0;
        char *new_path = malloc( path_n+1 );
        if ( new_path != NULL )
        {
            strncpy( new_path, *rel_path, path_n );
            new_path[path_n] = 0;
            *rel_path = new_path;
            res = 1;
        }
        else
        {
            free( *name );
            fprintf(stderr,"utils: failed to allocate path\n");
        }
    }
    else
    {
        fprintf(stderr,"utils: failed to allocate name\n");
    }
    return res;
}
#ifdef UTILS_DEBUG
int main( int argc, char **argv )
{
    char *path1 = "banana/apple/guava";
    char *path2 = "fruit";
    char *name;
    if ( split_path(&path1,&name) )
    {
        printf("path1=%s name=%s\n",path1,name);
        free( path1 );
        free( name );
    }
    if ( split_path(&path2,&name) )
    {
        printf("path2=%s name=%s\n",path2,name);
        free( path2 );
        free( name );
    }
}
#endif