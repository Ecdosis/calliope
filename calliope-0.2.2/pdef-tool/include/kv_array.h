/* 
 * File:   kv_array.h
 * Author: desmond
 *
 * Created on July 18, 2012, 8:21 AM
 */

#ifndef KV_ARRAY_H
#define	KV_ARRAY_H

#ifdef	__cplusplus
extern "C" {
#endif

char **kv_array_create( cJSON *first );
void kv_array_dispose( char **kva );
char **kv_array_clone( char **kva );


#ifdef	__cplusplus
}
#endif

#endif	/* KV_ARRAY_H */

