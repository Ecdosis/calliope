#include <stdarg.h>
#include <stdlib.h>
#include <stdio.h>
#include "error.h"
#include "memwatch.h"
static char message[256];
/**
 * Report an error. 
 * @param fmt the format of the error message
 * @param l the variable list of arguments to include in the format
 */
static void display_error( const char *fmt, va_list l )
{
    vsnprintf( message, 255, fmt, l );
    fprintf( stderr, "%s", message );
}
/**
 * Report a fatal error. Stop the program.
 * @param fmt the format of the error message
 * @param ... the arguments to include in the format
 */
void error( const char *fmt, ... )
{
    va_list l;
    va_start(l,fmt);
    display_error( fmt, l );
    va_end(l);
    exit( 0 );
}
/**
 * Report a non-fatal error.
 * @param fmt the format of the error message
 * @param ... the arguments to include in the format
 */
void warning( const char *fmt, ... )
{
    va_list l;
    va_start(l,fmt);
    display_error( fmt, l );
    va_end(l);
}
