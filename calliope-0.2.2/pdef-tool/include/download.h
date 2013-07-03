/* 
 * File:   download.h
 * Author: desmond
 *
 * Created on May 9, 2013, 12:36 PM
 */

#ifndef DOWNLOAD_H
#define	DOWNLOAD_H

#ifdef	__cplusplus
extern "C" {
#endif
int download( char *host, char **formats, char *docid, char *name, 
        char *zip_type, int add_required );

#ifdef	__cplusplus
}
#endif

#endif	/* DOWNLOAD_H */

