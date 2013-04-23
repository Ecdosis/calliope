/* 
 * File:   AUSTESE.h
 * Author: desmond
 *
 * Created on 18 April 2011, 8:08 AM
 */

#ifndef AUSTESE_H
#define	AUSTESE_H
#ifdef	__cplusplus
extern "C" {
#endif
int load_austese_markup( const char *data, int len, range_array *ranges, hashset *props );
#ifdef	__cplusplus
}
#endif
#endif	/* AUSTESE_H */

