/* 
 * File:   utils.h
 * Author: desmond
 *
 * Created on July 15, 2012, 4:44 PM
 */

#ifndef UTILS_H
#define	UTILS_H

#ifdef	__cplusplus
extern "C" {
#endif
int file_size( FILE *fp );
int contains_file( char *dir, char *name );
int is_directory( char *relpath, char *dirname );
int is_mvd_name( char *fname );
int is_docid_name( char *fname );
int is_literal_name( char *fname );
int ends_with( char *string, char *suffix );
int starts_with( char *string, char *prefix );
char *allocate_path( char *parent, char *file );
char *allocate_docid( char *parent, char *name );
int compare_to_file( const char *file, char *text, int *line, int *pos );
int max( int a, int b );
char *swap_file(const char *path, const char *name );
char *file_name( char *path );
char *mvd_name( char *dir_name );
int is_uploadable( char *name );
int count_chars( char *str, char token );
long epoch_time();
int split_path( char **rel_path, char **name );

#ifdef	__cplusplus
}
#endif

#endif	/* UTILS_H */

