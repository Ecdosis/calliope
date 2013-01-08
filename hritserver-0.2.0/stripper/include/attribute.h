/* 
 * File:   attribute.h
 * Author: desmond
 *
 * Created on 14 April 2011, 3:45 PM
 */

#ifndef ATTRIBUTE_H
#define	ATTRIBUTE_H

typedef struct attribute_struct attribute;
attribute *attribute_new( const char *name, const char *value );
char *attribute_get_name( attribute *a );
char *attribute_get_value( attribute *a );
int attribute_present( attribute *a, char **attrs );
void attribute_remove( attribute *a, char **attrs );
void attribute_delete( attribute *a );
#endif	/* ATTRIBUTE_H */

