/* 
 * File:   HRIT.h
 * Author: desmond
 *
 * Created on 18 April 2011, 8:08 AM
 */

#ifndef HRIT_H
#define	HRIT_H
#ifdef	__cplusplus
extern "C" {
#endif
int load_hrit_markup( const char *data, int len, range_array *ranges, hashset *props );
#ifdef	__cplusplus
}
#endif
#endif	/* HRIT_H */

