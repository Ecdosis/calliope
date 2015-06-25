/* 
 * File:   master.h
 * Author: desmond
 *
 * Created on 19 November 2011, 9:20 AM
 */
#ifndef MASTER_H
#define	MASTER
#ifdef	__cplusplus
extern "C" {
#endif
typedef struct master_struct master;

master *master_create( UChar *text, int len );
void master_dispose( master *hf );
int master_load_markup( master *hf, const UChar *markup, int len ); 
int master_get_html_len( master *hf );
int master_load_css( master *hf, const UChar *css, int len );
UChar *master_convert( master *hf );
UChar *master_list();
#ifdef	__cplusplus
}
#endif
#endif	/* MASTER_H */
