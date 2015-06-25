/* 
 * File:   css_selector.h
 * Author: desmond
 *
 * Created on 3 June 2011, 6:43 AM
 */

#ifndef CSS_SELECTOR_H
#define	CSS_SELECTOR_H
typedef struct css_selector_struct css_selector;
css_selector *css_selector_create( UChar *html_name, UChar *xml_name );
int css_selector_is_empty( css_selector *s );
void css_selector_dispose( css_selector *s );
css_selector *css_selector_clone( css_selector *s );
UChar *css_selector_get_element( css_selector *s );
UChar *css_selector_get_class( css_selector *s );
css_selector *css_selector_parse( const UChar *data, int len );
#endif	/* CSS_SELECTOR_H */

