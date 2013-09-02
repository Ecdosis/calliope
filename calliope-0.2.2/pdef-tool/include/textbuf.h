/* 
 * File:   textbuf.h
 * Author: desmond
 *
 * Created on July 22, 2012, 7:57 AM
 */

#ifndef TEXTBUF_H
#define	TEXTBUF_H

#ifdef	__cplusplus
extern "C" {
#endif 
typedef struct textbuf_struct textbuf;
textbuf *textbuf_create();
void textbuf_dispose( textbuf *tb );
char *textbuf_get( textbuf *tb );



#ifdef	__cplusplus
}
#endif

#endif	/* TEXTBUF_H */

