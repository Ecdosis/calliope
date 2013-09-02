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
 * File:   url.h
 * Author: desmond
 *
 * Created on May 23, 2012, 4:50 AM
 */

#ifndef URL_H
#define	URL_H

#ifdef	__cplusplus
extern "C" {
#endif
typedef struct url_struct url;
url *url_create( const char *url_spec );
void url_dispose( url *u );
char *url_get_host( url *u );
int url_get_port( url *u );
char *url_get_path( url *u );

#ifdef	__cplusplus
}
#endif

#endif	/* URL_H */

