#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <assert.h>
#include "unicode/utypes.h"
#include "unicode/ucnv.h"
#include "unicode/ustring.h"
#include "unicode/uchar.h"
#include "unicode/uloc.h"
#include "unicode/ustring.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif

/**
 * Convert from UTF-16 to another encoding
 * @param src the data read from the file
 * @param srclen the length of inbuf in UChars
 * @param dst the destination buffer
 * @param dstlen the length of the destination in chars
 * @param charset the charset of the src
 * @return the number of bytes written
 */
int convert_to_encoding( UChar *src, int srclen, char *dst, 
    int dstlen, char *charset )
{
    UConverter *conv = NULL;
  	UErrorCode status = U_ZERO_ERROR;
  	int32_t len=0;
  	
	conv = ucnv_open( charset, &status );
  	if ( status == U_ZERO_ERROR )
	{
	  	len = ucnv_fromUChars( conv, dst, dstlen, src, srclen, &status );
	  	if ( status != U_ZERO_ERROR 
            && status != U_STRING_NOT_TERMINATED_WARNING)
            fprintf(stderr,"encoding: %s\n",u_errorName(status));
        ucnv_close(conv);
	}
    return len;
}
/**
 * Convert from an existing encoding to UTF-16
 * @param src the data read from the file
 * @param srclen the length of src in bytes
 * @param dst the destination buffer
 * @param dstlen the length of the destination in UChars
 * @param charset the charset of the src
 * @return the number of BYTES written
 */
int convert_from_encoding( char *src, int srclen, UChar *dst, 
    int dstlen, char *charset )
{
    UConverter *conv = NULL;
    UErrorCode status = U_ZERO_ERROR;
    int32_t len=0;

    conv = ucnv_open( charset, &status );
    if ( status == U_ZERO_ERROR )
    {
        len = ucnv_toUChars( conv, dst, dstlen, src, srclen, &status );
        if ( status != U_ZERO_ERROR )
        fprintf(stderr,"encoding: %s\n",u_errorName(status));
        len *= sizeof(UChar);
            ucnv_close(conv);
    }
    return len;
}
/**
 * How many bytes are needed to convert from utf16 to an encoding?
 * @param src the source in utf16
 * @param srclen its length in UChars
 * @param encoding the destination encoding
 * @return the number of BYTES needed
 */
int measure_to_encoding( UChar *src, size_t srclen, char *encoding )
{
    UConverter *conv = NULL;
  	UErrorCode status = U_ZERO_ERROR;
  	int32_t len=0;
  	
	conv = ucnv_open( encoding, &status );
  	if ( status == U_ZERO_ERROR )
	{	
	  	len = ucnv_fromUChars( conv, NULL, 0, src, srclen, &status );
	  	if ( status != U_BUFFER_OVERFLOW_ERROR )
        {
            printf("encoding: %s\n",u_errorName(status));
            len = 0;
        }
        ucnv_close(conv);
	}
    return len;
}
/**
 * How many bytes are needed to convert from an encoding to utf16?
 * @param src the source in the encoding
 * @param srclen its length in bytes
 * @param encoding the src's encoding
 * @return the number of UCHARS needed
 */
int measure_from_encoding( char *src, size_t srclen, char *encoding )
{
    UConverter *conv = NULL;
  	UErrorCode status = U_ZERO_ERROR;
  	int32_t len=0;
  	
	conv = ucnv_open( encoding, &status );
  	if ( status == U_ZERO_ERROR )
	{	
	  	len = ucnv_toUChars( conv, NULL, 0, src, srclen, &status );
	  	if ( status != U_BUFFER_OVERFLOW_ERROR )
        {
            printf("encoding: %s\n",u_errorName(status));
            len = 0;
        }
        ucnv_close(conv);
	}
    return len;
}
#ifdef MVD_TEST
static int file_length( FILE *fp )
{
	int length = 0;
    int res = fseek( fp, 0, SEEK_END );
	if ( res == 0 )
	{
		long long_len = ftell( fp );
		if ( long_len > INT_MAX )
        {
			fprintf( stderr,"mvdfile: file too long: %ld", long_len );
            length = res = 0;
        }
		else
        {
            length = (int) long_len;
            if ( length != -1 )
                res = fseek( fp, 0, SEEK_SET );
            else
                res = 1;
        }
	}
	if ( res != 0 )
    {
		fprintf(stderr, "encoding: failed to read file. error %s\n",
            strerror(errno) );
        length = 0;
    }
	return length;
}
/**
 * Read a file
 * @param file the path to the file
 * @param flen update with the length of the file
 * @return NULL on failure else the allocated text content
 */
static char *read_file( char *file, size_t *flen )
{
    char *data = NULL;
    FILE *fp = fopen( file, "r" );
    if ( fp != NULL )
    {
        int len = file_length( fp );
        data = (char*)malloc( len+1 );
        if ( data != NULL )
        {
            int read = fread( data, 1, len, fp );
            if ( read != len )
            {
                fprintf(stderr,"failed to read %s\n",file);
                free( data );
                data = NULL;
                *flen = 0;
            }
            else
            {
                data[len] = 0;
                *flen = len;
            }
        }
        else
            fprintf(stderr,"failed to allocate file buffer\n");
        fclose( fp );
    }
    return data;
}
static int data_is_same( char *out, int out_len, char *in, int in_len )
{
    int i,res = 0;
    if ( in_len==out_len)
    {
        res = 1;
        for ( i=0;i<in_len;i++ )
        {
            if ( in[i]!=out[i] )
            {
                res = 0;
                break;
            }
        }
    }
    return res;
}
void test_encoding( int *passed, int *failed )
{
    size_t srclen;
    char *charset="utf-8";
    char *src = read_file( "tests/can_1316_01.txt", &srclen );
    if ( srclen > 0 )
    {
        size_t dstlen = measure_from_encoding( src, srclen, "utf-8" );
        if ( dstlen > 0 )
        {
            UChar *dst = (UChar*)calloc( dstlen+1, sizeof(UChar) );
            if ( dst != NULL )
            {
                int res = convert_from_encoding( src, srclen, 
                    dst, dstlen+1, charset );
                if ( res )
                {
                    (*passed)++;
                    size_t dst2len = measure_to_encoding( dst, dstlen, "utf-8" );
                    char *dst2 = calloc( dst2len+1, sizeof(char) );
                    if ( dst2 != NULL )
                    {
                        int res = convert_to_encoding( dst, dstlen, 
                            dst2, dst2len+1, "utf-8" );
                        if ( res ) 
                            (*passed)++;
                        else
                            (*failed)++;
                        if ( !data_is_same(dst2,dst2len,src,srclen) )
                            (*failed)++;
                        else
                            (*passed)++;
                        free( dst2 );
                    }
                }
                else
                {
                    printf("conversion failed\n");
                    (*failed)++;
                }
                free( dst );
            }
        }
        free( src );
    }
}
#endif
