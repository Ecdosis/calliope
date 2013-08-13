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
 * File:   part.h
 * Author: desmond
 *
 * Created on May 23, 2012, 7:57 AM
 */

#ifndef PART_H
#define	PART_H

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct part_struct part;
typedef enum { formdata, filedata } part_kind;
part *part_create( const char *name, unsigned char *value, int len, 
    part_kind kind, const char *enc );
int part_size( part *p, char *boundary );
part *part_next( part *p );
void part_set_next( part *q, part *p );
int part_get( part *p, unsigned char *body, char *boundary, int len );

#ifdef	__cplusplus
}
#endif

#endif	/* PART_H */

