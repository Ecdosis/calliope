/* 
 * File:   annotation.h
 * Author: desmond
 *
 * Created on June 17, 2011, 9:30 AM
 */

#ifndef ANNOTATION_H
#define	ANNOTATION_H
#ifdef	__cplusplus
extern "C" {
#endif
typedef struct annotation_struct annotation;
annotation *annotation_create_simple( UChar *name, UChar *value );
annotation *annotation_create( const UChar **atts );
annotation *annotation_clone( annotation *a );
void annotation_dispose( annotation *a );
UChar *annotation_get_name( annotation *a );
UChar *annotation_get_value( annotation *a );
annotation *annotation_get_next( annotation *a );
void annotation_print( annotation *a );
void annotation_append( annotation *a, annotation *b );
attribute *annotation_to_attribute( annotation *a, UChar *xml_name, hashmap *css_rules );
#ifdef	__cplusplus
}
#endif
#endif	/* ANNOTATION_H */

