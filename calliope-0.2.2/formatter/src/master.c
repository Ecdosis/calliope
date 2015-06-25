#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <unicode/uchar.h>
#include <unicode/ustring.h>
#include <jni.h>
#include "attribute.h"
#include "hashmap.h"
#include "annotation.h"
#include "range.h"
#include "range_array.h"
#include "hashset.h"
#include "formatter.h"
#include "master.h"
#include "STIL/STIL.h"
#include "error.h"
#include "utils.h"

#include "memwatch.h"
static format stil_format;
static UChar U_STIL[4] = {'S','T','I','L'};
static char error_string[128] = "";
static UChar u_error_string[128];
struct master_struct
{
    UChar *text;
    int tlen;
    int html_len;
    int has_css;
    int has_markup;
    int has_text;
    formatter *f;
};
/**
 * Create a aese formatter
 * @param text the text to format
 * @param len the length of the text
 * @return an initialised master instance
 */
master *master_create( UChar *text, int len )
{
    master *hf = calloc( 1, sizeof(master) );
    if ( hf != NULL )
    {
        stil_format.lm = load_stil_markup;
        stil_format.name = U_STIL;
        hf->has_text = 0;
        hf->has_css = 0;
        hf->has_markup = 0;
        if ( text != NULL )
        {
            hf->tlen = len;
            if ( hf->tlen > 0 )
            {
                hf->f = formatter_create( hf->tlen );
                hf->text = text;
                hf->has_text = 1;
            }
        }
    }
    else
        error("master: failed to allocate instance\n");
    return hf;
}
/**
 * Dispose of a formatter
 */
void master_dispose( master *hf )
{
    if ( hf->f != NULL )
        formatter_dispose( hf->f );
    free( hf );
}
/**
 * Load the markup file (possibly one of several)
 * @param hf the master in question
 * @param markup a markup string
 * @param mlen the length of the markup
 * @param fmt the format
 * return 1 if successful, else 0
 */
int master_load_markup( master *hf, const UChar *markup, int mlen )
{
    int res = 0;
    res = formatter_load_markup( hf->f, 
        stil_format.lm, markup, mlen );
    if ( res && !hf->has_markup )
        hf->has_markup = 1;
    return res;
}
/**
 * Load a css file
 * @param hf the master in question
 * @param css the css data
 * @param len its length
 * return 1 if successful, else 0
 */
int master_load_css( master *hf, const UChar *css, int len )
{
    int res = formatter_css_parse( hf->f, css, len );
    if ( res && !hf->has_css )
        hf->has_css = 1;
    return res;
}
/**
 * Convert the specified text to HTML
 * @param hf the master in question
 * @return a HTML string
 */
UChar *master_convert( master *hf )
{
    UChar *str = NULL;
    if ( hf->has_text && hf->has_css && hf->has_markup )
    {
        if ( formatter_cull_ranges(hf->f,hf->text,&hf->tlen) )
        {
            int res = formatter_make_html( hf->f, hf->text, hf->tlen );
            if ( res )
                str = formatter_get_html( hf->f, &hf->html_len );
            else
            {
                const char *error = "<html><body><p>Error: conversion "
                    "failed</p></body></html>";
                str2ustr( (char*)error, u_error_string, 128 );
                hf->html_len = u_strlen(u_error_string);
                str = u_error_string;
            }
        }
        else
        {
            const char *error = "<html><body><p>Error: failed to remove ranges</p></body></html>";
            str2ustr( (char*)error, u_error_string, 128 );
            str = u_error_string;
        }
    }
    else
    {
        const char *hntext = (hf->has_text)?"":"no text ";
        const char *hnmarkup = (hf->has_markup)?"":"no markup ";
        const char *hncss = (hf->has_css)?"":"no css ";
        snprintf( error_string,128,"<html><body><p>Error: %s%s%s</p></body></html>",
            hntext,hnmarkup,hncss );
        str2ustr( error_string, u_error_string, 128 );
        hf->html_len = u_strlen( u_error_string );
        str = u_error_string;
    }
    return str;
}
/**
 * Get the length of the just processed html
 * @param hf the master in question
 * @return the html text length
 */
int master_get_html_len( master *hf )
{
    return hf->html_len;
}

