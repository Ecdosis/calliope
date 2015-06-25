/* 
 * File:   attribute.h
 * Author: desmond
 *
 * Created on 10 July 2011, 7:39 AM
 */

#ifndef ATTRIBUTE_H
#define	ATTRIBUTE_H
#ifdef	__cplusplus
extern "C" {
#endif
typedef struct attribute_struct attribute;
void attribute_dispose( attribute *attr );
attribute *attribute_create( UChar *name, UChar *prop_name, UChar *value );
attribute *attribute_clone( attribute *attr );
void attribute_append( attribute *attrs, attribute *attr );
UChar *attribute_get_name( attribute *attr );
UChar *attribute_prop_name( attribute *attr );
UChar *attribute_get_value( attribute *attr );
attribute *attribute_get_next( attribute *attr );
int attribute_count( attribute *attr );
#ifdef	__cplusplus
}
#endif
#endif	/* ATTRIBUTE_H */

