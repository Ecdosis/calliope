/*
 * This file is part of formatter.
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
 *  (c) copyright Desmond Schmidt 2011
 */
/**
 * Define which HTML elements may appear inside which other HTML elements
 */
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <limits.h>
#include <unicode/uchar.h>
#include <unicode/ustring.h>
#include <unicode/ustdio.h>
#include "HTML.h"
#include "error.h"
#include "memwatch.h"
/* this has to be so, because of the bit vectors below */
#if UINT_MAX > 4294967295
	#error "sizeof unsigned int not equal to 4!"
#endif

#define PHRASING_PLUS_FLOW {0xdf66eeff,0xb8ff77fd,0x9adfcecf,0x788}
#define PHRASING {0xc566e6cb,0x98ff0000,0x8adbcc46,0x708}
#define VOID {0x0,0x0,0x0,0x0}
#define FLOW {0xdf66eeff,0xb8ff77fd,0x9adfcecf,0x788}
#define STYLE_PLUS_FLOW {0xdf66eeff,0xb8ff77fd,0x9bdfcecf,0x788}
/* all 107 HTML5 tags */
static const UChar *tags[107] = {
(UChar[]){'a'},
(UChar[]){'a','b','b','r'},
(UChar[]){'a','d','d','r','e','s','s'},
(UChar[]){'a','r','e','a'},
(UChar[]){'a','r','t','i','c','l','e'},
(UChar[]){'a','s','i','d','e'},
(UChar[]){'a','u','d','i','o'},
(UChar[]){'b'},
(UChar[]){'b','a','s','e'},
(UChar[]){'b','d','i'},
(UChar[]){'b','d','o'},
(UChar[]){'b','l','o','c','k','q','u','o','t','e'},
(UChar[]){'b','o','d','y'},
(UChar[]){'b','r'},
(UChar[]){'b','u','t','t','o','n'},
(UChar[]){'c','a','n','v','a','s'},
(UChar[]){'c','a','p','t','i','o','n'},
(UChar[]){'c','i','t','e'},
(UChar[]){'c','o','d','e'},
(UChar[]){'c','o','l'},
(UChar[]){'c','o','l','g','r','o','u','p'},
(UChar[]){'c','o','m','m','a','n','d'},
(UChar[]){'d','a','t','a','l','i','s','t'},
(UChar[]){'d','d'},
(UChar[]){'d','e','l'},
(UChar[]){'d','e','t','a','i','l','s'},
(UChar[]){'d','f','n'},
(UChar[]){'d','i','v'},
(UChar[]){'d','l'},
(UChar[]){'d','t'},
(UChar[]){'e','m'},
(UChar[]){'e','m','b','e','d'},
(UChar[]){'f','i','e','l','d','s','e','t'},
(UChar[]){'f','i','g','c','a','p','t','i','o','n'},
(UChar[]){'f','i','g','u','r','e'},
(UChar[]){'f','o','o','t','e','r'},
(UChar[]){'f','o','r','m'},
(UChar[]){'h','1'},
(UChar[]){'h','2'},
(UChar[]){'h','3'},
(UChar[]){'h','4'},
(UChar[]){'h','5'},
(UChar[]){'h','6'},
(UChar[]){'h','e','a','d'},
(UChar[]){'h','e','a','d','e','r'},
(UChar[]){'h','g','r','o','u','p'},
(UChar[]){'h','r'},
(UChar[]){'h','t','m','l'},
(UChar[]){'i'},
(UChar[]){'i','f','r','a','m','e'},
(UChar[]){'i','m','g'},
(UChar[]){'i','n','p','u','t'},
(UChar[]){'i','n','s'},
(UChar[]){'k','b','d'},
(UChar[]){'k','e','y','g','e','n'},
(UChar[]){'l','a','b','e','l'},
(UChar[]){'l','e','g','e','n','d'},
(UChar[]){'l','i'},
(UChar[]){'l','i','n','k'},
(UChar[]){'m','a','p'},
(UChar[]){'m','a','r','k'},
(UChar[]){'m','e','n','u'},
(UChar[]){'m','e','t','a'},
(UChar[]){'m','e','t','e','r'},
(UChar[]){'n','a','v'},
(UChar[]){'n','o','s','c','r','i','p','t'},
(UChar[]){'o','b','j','e','c','t'},
(UChar[]){'o','l'},
(UChar[]){'o','p','t','g','r','o','u','p'},
(UChar[]){'o','p','t','i','o','n'},
(UChar[]){'o','u','t','p','u','t'},
(UChar[]){'p'},
(UChar[]){'p','a','r','a','m'},
(UChar[]){'p','r','e'},
(UChar[]){'p','r','o','g','r','e','s','s'},
(UChar[]){'q'},
(UChar[]){'r','p'},
(UChar[]){'r','t'},
(UChar[]){'r','u','b','y'},
(UChar[]){'s'},
(UChar[]){'s','a','m','p'},
(UChar[]){'s','c','r','i','p','t'},
(UChar[]){'s','e','c','t','i','o','n'},
(UChar[]){'s','e','l','e','c','t'},
(UChar[]){'s','m','a','l','l'},
(UChar[]){'s','o','u','r','c','e'},
(UChar[]){'s','p','a','n'},
(UChar[]){'s','t','r','o','n','g'},
(UChar[]){'s','t','y','l','e'},
(UChar[]){'s','u','b'},
(UChar[]){'s','u','m','m','a','r','y'},
(UChar[]){'s','u','p'},
(UChar[]){'t','a','b','l','e'},
(UChar[]){'t','b','o','d','y'},
(UChar[]){'t','d'},
(UChar[]){'t','e','x','t','a','r','e','a'},
(UChar[]){'t','f','o','o','t'},
(UChar[]){'t','h'},
(UChar[]){'t','h','e','a','d'},
(UChar[]){'t','i','m','e'},
(UChar[]){'t','i','t','l','e'},
(UChar[]){'t','r'},
(UChar[]){'t','r','a','c','k'},
(UChar[]){'u','l'},
(UChar[]){'v','a','r'},
(UChar[]){'v','i','d','e','o'},
(UChar[]){'w','b','r'}
};

static unsigned empty[4] = {277091328,143906,8389632,0};
/* bit-vector of permitted contents for each (down index) HTML element's
 * allowed content of other elements, specified by an index into a
 * 107-bit number. This is expressed as a precomputed array of 4x32 bit
 * numbers */
static unsigned int matrix[107][4] = {
/*a*/PHRASING_PLUS_FLOW,
/*abbr*/PHRASING,
/*address*/FLOW,
/*area*/VOID,
/*article*/FLOW,
/*aside*/STYLE_PLUS_FLOW,
/*audio*/{0xdf66eeff,0xb8ff77fd,0x9affcecf,0x7c8},
/*b*/PHRASING,
/*base*/VOID,
/*bdi*/PHRASING,
/*bdo*/PHRASING,
/*blockquote*/FLOW,
/*body*/FLOW,
/*br*/PHRASING, /*fudged: otherwise "lines" may not contain anything*/
/*button*/PHRASING,
/*canvas*/PHRASING_PLUS_FLOW,
/*caption*/FLOW,
/*cite*/PHRASING,
/*code*/PHRASING,
/*col*/VOID,
/*colgroup*/{0x80000,0x0,0x400000,0x0},
/*command*/VOID,
/*datalist*/{0xc566e6cb,0x98ff0000,0x8adbcc66,0x708},
/*dd*/FLOW,
/*del*/PHRASING_PLUS_FLOW,
/*details*/{0xdf66eeff,0xb8ff77fd,0x9edfcecf,0x7c8},
/*dfn*/PHRASING,
/*div*/{0xdf66eeff,0xb8ff77fd,0x9bdfcecf,0x788},
/*dl*/{0x20800000,0x0,0x0,0x0},
/*dt*/PHRASING,
/*em*/PHRASING,
/*embed*/VOID,
/*fieldset*/{0xdf66eeff,0xb9ff77fd,0x9adfcecf,0x788},
/*figcaption*/FLOW,
/*figure*/{0xdf66eeff,0xb8ff77ff,0x9adfcecf,0x788},
/*footer*/FLOW,
/*form*/FLOW,
/*h1*/PHRASING,
/*h2*/PHRASING,
/*h3*/PHRASING,
/*h4*/PHRASING,
/*h5*/PHRASING,
/*h6*/PHRASING,
/*head*/{0x200100,0x44000000,0x1020002,0x10},
/*header*/FLOW,
/*hgroup*/{0x0,0x7e0,0x0,0x0},
/*hr*/VOID,
/*html*/{0x1000,0x800,0x0,0x0},
/*i*/PHRASING,
/*iframe*/VOID,
/*img*/VOID,
/*input*/VOID,
/*ins*/PHRASING_PLUS_FLOW,
/*kbd*/PHRASING,
/*keygen*/VOID,
/*label*/PHRASING,
/*legend*/PHRASING,
/*li*/FLOW,
/*link*/VOID,
/*map*/PHRASING_PLUS_FLOW,
/*mark*/PHRASING,
/*menu*/{0xdf66eeff,0xbaff77fd,0x9adfcecf,0x788},
/*meta*/VOID,
/*meter*/PHRASING,
/*nav*/FLOW,
/*noscript*/{0xdf66eeff,0xfcff77fd,0x9bdfcecf,0x788},
/*object*/{0xdf66eeff,0xb8ff77fd,0x9adfcfcf,0x788},
/*ol*/{0x0,0x2000000,0x0,0x0},
/*optgroup*/{0x0,0x0,0x20,0x0},
/*option*/VOID,
/*output*/PHRASING,
/*p*/PHRASING,
/*param*/VOID,
/*pre*/PHRASING,
/*progress*/PHRASING,
/*q*/PHRASING,
/*rp*/PHRASING,
/*rt*/PHRASING,
/*ruby*/{0xc566e6cb,0x98ff0000,0x8adbfc46,0x708},
/*s*/PHRASING,
/*samp*/PHRASING,
/*script*/VOID,
/*section*/{0xdf66eeff,0xb8ff77fd,0x9bdfcecf,0x788},
/*select*/{0x0,0x0,0x30,0x0},
/*small*/PHRASING,
/*source*/VOID,
/*span*/PHRASING,
/*strong*/PHRASING,
/*style*/VOID,
/*sub*/PHRASING,
/*summary*/PHRASING,
/*sup*/PHRASING,
/*table*/{0x110000,0x0,0x20000000,0x25},
/*tbody*/{0x0,0x0,0x0,0x20},
/*td*/FLOW,
/*textarea*/VOID,
/*tfoot*/{0x0,0x0,0x0,0x20},
/*th*/PHRASING,
/*thead*/{0x0,0x0,0x0,0x20},
/*time*/PHRASING,
/*title*/VOID,
/*tr*/{0x0,0x0,0x40000000,0x2},
/*track*/VOID,
/*ul*/{0x0,0x2000000,0x0,0x0},
/*var*/PHRASING,
/*video*/{0xdf66eeff,0xb8ff77fd,0x9affcecf,0x7c8},
/*wbr*/VOID
};
/**
 * Convert a tag to an index using binary search. First reduce to lowercase.
 * @param tag the tag to get the index of
 * @return the index or -1
 */
static int tag2index( UChar *tag )
{
	int top=0,bottom=106;
	UChar lctag[12];
	int i,taglen = u_strlen(tag);
	if ( taglen >= 12 )
	{
		u_printf( "html: tag %s too long!\n",tag );
        return -1;
	}
	for ( i=0;i<taglen;i++ )
		lctag[i] = tolower( tag[i] );
	lctag[taglen] = 0;
	while ( top <= bottom )
	{
		int middle = (top+bottom)/2;
		int res = u_strcmp(lctag,tags[middle]);
		if ( res > 0 )
			top = middle+1;
		else if ( res < 0 )
			bottom = middle-1;
		else
			return middle;
	}
	return -1;
}
/**
 * Is tag1 inside tag2? or tag2 inside tag1? or neither? or either?
 * An empty tag belongs to the root node inside which is everything.
 * @param tag1 the first tag
 * @param tag2 the second tag
 * @return 1 if tag1 is inside tag2, -1 if tag2 is inside tag1,
 * 0 if either, -2 if neither
 */
int html_is_inside( UChar *tag1, UChar *tag2 )
{
    if ( tag2==NULL )
        return 1;
    else if ( tag1==NULL )
        return -1;
    else
    {
        int index1 = tag2index( tag1 );
        int index2 = tag2index( tag2 );
        unsigned int word = index2/32;
        unsigned int bit = index2%32;
        unsigned int mask = 1<<bit;
        int ans1,ans2 = (mask & matrix[index1][word]);
        word = index1/32;
        bit = index1%32;
        mask = 1<<bit;
        ans1 = (mask & matrix[index2][word]);
        if ( ans1 != 0 && ans2 != 0 )
            return 0;
        else if ( ans1 == 0 && ans2 == 0 )
            return -2;
        else if ( ans1 )
            return 1;
        else
            return -1;
    }
}
/**
 * Is this HTML tag empty?
 * @param tag the tag in question
 * @return 1 if it is empty, else 0
 */
int html_is_empty( UChar *tag )
{
    if ( tag == NULL )
        printf("tag is NULL!\n");
    int bit = tag2index( tag );
	unsigned flag = 1;
	int word = bit / 32;
	int res = empty[word] & (flag<<(31-(bit%32)));
	return res > 0;
}
