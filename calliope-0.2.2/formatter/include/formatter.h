/*
 * formatter.h
 *
 *  Created on: 26/10/2010
 * Converted to UTF-16 22/6/2015
 *  (c) Desmond Schmidt 2010,2015
 */
/* This file is part of formatter.
 *
 *  formatter is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  formatter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with formatter.  If not, see <http://www.gnu.org/licenses/>.
 */
#ifndef FORMATTER_H_
#define FORMATTER_H_
#define NAME_LEN 4
#define FILE_NAME_LEN 64
#ifdef	__cplusplus
extern "C" {
#endif

typedef int (*load_markup_func)(const UChar *data, int len, range_array *ranges,
    hashset *props );
typedef struct
{
    UChar *name;
    load_markup_func lm;
} format;
typedef struct formatter_struct formatter;
formatter *formatter_create( int len );
void formatter_dispose( formatter *f );
int formatter_css_parse( formatter *f, const UChar *data, int len );
int formatter_load_markup( formatter *f, load_markup_func mfunc, 
    const UChar *data, int len );
int formatter_make_html( formatter *f, const UChar *text, int len );
int formatter_save_html( formatter *f, char *file );
UChar *formatter_get_html( formatter *f, int *len );
int formatter_cull_ranges( formatter *f, UChar *text, int *len );
#ifdef	__cplusplus
}
#endif
#endif /* FORMATTER_H_ */
