/* 
 * File:   config.h
 * Author: desmond
 *
 * Created on July 15, 2012, 4:49 PM
 */

#ifndef CONFIG_H
#define	CONFIG_H

#ifdef	__cplusplus
extern "C" {
#endif
typedef struct config_struct config;
config *config_update( char *path, config *parent );
config *config_create();
config *config_clone( config *cf );
void *config_dispose( config *cf );
void config_print( config *cf );
void *config_get( config *cf, char *key );

#ifdef	__cplusplus
}
#endif

#endif	/* CONFIG_H */

