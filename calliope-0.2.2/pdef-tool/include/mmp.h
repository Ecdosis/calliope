/*
 * This file is part of mmpupload.

    mmpupload is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mmpupload is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mmpupload.  If not, see <http://www.gnu.org/licenses/>.*/
/* 
 * File:   mmp.h
 * Author: desmond
 *
 * Created on May 22, 2012, 10:01 PM
 */

#ifndef MMP_H
#define	MMP_H

#ifdef	__cplusplus
extern "C" {
#endif
typedef struct mmp_struct mmp;
mmp *mmp_create();
void mmp_dispose( mmp *m );
int mmp_add_field( mmp *m, char *name, char *value );
int mmp_add_file( mmp *m, char *name, char *path );
unsigned char *mmp_get( mmp *m, int *len, char *method, char *host, 
    char *path );

#ifdef	__cplusplus
}
#endif

#endif	/* MMP_H */

