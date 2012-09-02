/*
 * HRIT.h
 *
 *  Created on: 23/10/2010
 *      Author: desmond
 */

#ifndef HRIT_H_
#define HRIT_H_

int HRIT_write_header(void *arg, DST_FILE *dst, const char *style );
int HRIT_write_tail(void *arg, DST_FILE *dst);
int HRIT_write_range( char *name, char **atts, int removed,
	int offset, int len, char *content, int content_len, int final, 
    DST_FILE *dst );
#endif /* HRIT_H_ */
