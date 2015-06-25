#include <unicode/uchar.h>
#include <unicode/ustring.h>
#include <string.h>
#include <stdlib.h>
#include "utils.h"

/**
 * Convert a standard char* string to a UChar one. Assume ASCII!
 * @param str the cstring to convert
 * @param u_str a UChar string of equal length
 * @param limit the length of either string
 */
UChar *str2ustr( char *str, UChar *u_str, int limit )
{
    int i,len = strlen(str);
    int end = limit-1;
    for ( i=0;i<len&&i<end;i++ )
        u_str[i] = str[i];
    u_str[i] = 0;
    return u_str;
}
/**
 * Convert a UChar* string to a standard char* one. Assume ASCII!
 * @param u_str the ustring to convert
 * @param str a char string of equal length
 * @param limit the length of either string
 */
void ustr2str( UChar *u_str, char *str, int limit )
{
    int i,len = u_strlen(u_str);
    int end = limit-1;
    for ( i=0;i<len&&i<end;i++ )
        str[i] = u_str[i];
    str[i] = 0;
}
/**
 * Copy a UTF-16 string
 * @param ustr the string to copy
 * @return its copy: caller to free
 */
UChar *u_strdup( UChar *ustr )
{
    int i,len = u_strlen(ustr);
    UChar *dup = calloc(len+1,sizeof(UChar));
    if ( dup != NULL )
        for ( i=0;i<len;i++ )
            dup[i] = ustr[i];
    return dup;
}
/**
 * Convert a decimal number as unicode text to an int
 * @param u_str the number as UTF-16 string
 * @return its value
 */
int u_atoi( UChar *u_str )
{
    int res = 0;
    int len = u_strlen(u_str);
    int i;
    for ( i=0;i<len;i++ )
    {
        res *= 10;
        res += u_str[i]-'0';
    }
    return res;
}

