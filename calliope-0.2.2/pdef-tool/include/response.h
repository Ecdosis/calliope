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
 * File:   response.h
 * Author: desmond
 *
 * Created on May 24, 2012, 8:10 PM
 */

#ifndef RESPONSE_H
#define	RESPONSE_H

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct response_struct response;
response *response_create();
int response_append( response *r, char *line, int n );
int response_ok( response *r );
void response_dispose( response *r );
int response_get_len( response *r );
void response_dump( response *r );
#ifdef	__cplusplus
}
#endif

#endif	/* RESPONSE_H */

