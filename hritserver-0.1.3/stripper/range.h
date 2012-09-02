/* 
 * File:   range.h
 * Author: desmond
 *
 * Created on 14 April 2011, 5:22 AM
 */

#ifndef RANGE_H
#define	RANGE_H
typedef struct range_struct range;
range *range_new( int removed, char *name, char **atts, int rel_off,
    int offset );
void range_delete( range *r );
void range_add_content( range *r, const char *s, int len );
char *range_get_content( range *r );
int range_get_content_len( range *r );
int range_removed( range *r );
char *range_get_name( range *r );
int range_get_start( range *r );
char **range_get_atts( range *r );
int range_get_reloff( range *r );
int range_get_len( range *r );
void range_set_len( range *r, int len );
range *range_get_next( range *r );
void range_set_next( range *r, range *next );
#endif	/* RANGE_H */

