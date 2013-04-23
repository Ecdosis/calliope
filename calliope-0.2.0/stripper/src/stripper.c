/**
 * Strip XML tags from a file and write them out in HRIT format
 * Created on 22/10/2010
 * (c) Desmond Schmidt 2010
 */
/* This file is part of stripper.
 *
 *  stripper is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  stripper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with stripper.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include <ctype.h>
#include <syslog.h>
#ifdef JNI
#include <jni.h>
#include "calliope_AeseStripper.h"
#include "ramfile.h"
#endif
#include "format.h"
#include "expat.h"
#include "stack.h"
#include "HRIT.h"
#include "STIL.h"
#include "hashset.h"
#include "error.h"
#include "range.h"
#include "attribute.h"
#include "simplification.h"
#include "milestone.h"
#include "layer.h"
#include "recipe.h"
#include "dest_file.h"
#include "hashmap.h"
#include "log.h"
#include "memwatch.h"

#define FILE_NAME_LEN 128
#ifdef XML_LARGE_SIZE
#if defined(XML_USE_MSC_EXTENSIONS) && _MSC_VER < 1400
#define XML_FMT_INT_MOD "I64"
#else
#define XML_FMT_INT_MOD "ll"
#endif
#else
#define XML_FMT_INT_MOD "l"
#endif
#define CHAR_TYPE_TEXT 0
#define CHAR_TYPE_SPACE 1
#define CHAR_TYPE_CR 2
#define CHAR_TYPE_LF 3

struct UserData
{
    /** flag to remove multiple white space */
    int last_char_type;
    /** current offset in the text file */
    int current_text_offset;
};

struct UserData user_data;
/** array of available formats - add more here */
format formats[]={{"STIL",STIL_write_header,STIL_write_tail,
	STIL_write_range,".txt",".json","-stil"},
    {"HRIT",HRIT_write_header,HRIT_write_tail,
	HRIT_write_range,".txt",".xml","-hrit"}};
/** size of formats array */
int num_formats = sizeof(formats)/sizeof(format);
/** source file */
char src[FILE_NAME_LEN];
/** source file minus suffix */
char barefile[FILE_NAME_LEN];
/** name of style */
char *style = "TEI";
/** pointer to output stripped text file */
dest_file *text_dest;
/** array of markup_dest */
dest_file **markup_dest;
/** index into the currently selected format */
int selected_format;
/** if doing help or version info don't process anything */
int doing_help = 0;
/** the parser */
XML_Parser parser;
/** stack of potential ranges being maintained
 * as we parse the file */
stack *range_stack;
/** the recipe */
recipe *rules;
/** if non-empty ignore all tags and text */
stack *ignoring;
/** map of XML names to dest_files */
hashmap *dest_map;

/**
 * Copy an array of attributes as returned by expat
 * @param atts the attributes
 * @return a NULL terminated array, user must free
 */
static char **copy_atts( const char **atts )
{
	int len = 0;
	int i = 0;
	char **new_atts;
	while ( atts[i] != NULL )
	{
		i += 2;
		len+=2;
	}
	new_atts = calloc( len+2, sizeof(char*) );
	i = 0;
	while ( atts[i] != NULL )
	{
		new_atts[i] = strdup(atts[i]);
		if ( new_atts[i] == NULL )
			error( "stripper: failed to allocate store for attribute key" );
		new_atts[i+1] = strdup( atts[i+1] );
		if ( new_atts[i+1] == NULL )
			error( "stripper: failed to allocate store for attribute value" );
		i += 2;
	}
	return new_atts;
}
/**
 * Work out which dest file to send a named range to
 * @param range_name the range name
 * @return the appropriate dest_file object
 */
static dest_file *get_markup_dest( char *range_name )
{
    if ( hashmap_contains(dest_map,range_name) )
    {
        return (dest_file*)hashmap_get( dest_map, range_name );
    }
    else
    {
        int i=1;
        while ( markup_dest[i] != NULL )
        {
            layer *l = dest_file_layer(markup_dest[i]);
            if ( l!=NULL&& layer_has_milestone(l,range_name) )
            {
                // remember for future calls
                hashmap_put( dest_map,range_name,markup_dest[i] );
                return markup_dest[i];
            }
            i++;
        }
        hashmap_put( dest_map, range_name, markup_dest[0] );
        return (dest_file*)markup_dest[0];
    }
}
/**
 * Start element handler for XML file stripping.
 * @param userData the user data (optional)
 * @param name the name of the element
 * @param atts an array of attributes terminated by a NULL
 * pointer
 */
static void XMLCALL start_element_scan( void *userData,
	const char *name, const char **atts )
{
    range *r;
    struct UserData *u = (struct UserData*)userData;
    char **new_atts;
    char *simple_name = (char*)name;
    if ( recipe_has_removal(rules,(char*)name) )
        stack_push( ignoring, (char*)name );
    new_atts = copy_atts( atts );
    if ( stack_empty(ignoring) && recipe_has_rule(rules,name,atts) )
        simple_name = recipe_simplify( rules, simple_name, new_atts );
    r = range_new( stack_empty(ignoring)?0:1,
        simple_name,
        new_atts,
        u->current_text_offset );
    // stack has to set length when we get to the range end
    stack_push( range_stack, r );
    // queue preserves the order of the start elements
    dest_file *df = get_markup_dest( range_get_name(r) );
    dest_file_enqueue( df, r );
}
/**
 * End element handler for XML split
 * @param userData (optional)
 * @param name name of element
 */
static void XMLCALL end_element_scan(void *userData, const char *name)
{
	range *r = stack_pop( range_stack );
    struct UserData *u = (struct UserData *)userData;
	range_set_len( r, u->current_text_offset-range_get_start(r) );
	if ( !stack_empty(ignoring) && strcmp(stack_peek(ignoring),name)==0 )
		stack_pop( ignoring );
}
/**
 * Is the given string just whitespace?
 * @param s the string (not null-terminated
 * @param len its length
 * @return 1 is only contains whitespace, else 0
 */
static int is_whitespace( const char *s, int len )
{
    int res = 1;
    int i;
    for ( i=0;i<len;i++ )
        if ( s[i]!='\t' && s[i]!='\n' && s[i]!=' ' )
            return 0;
    return res;
}
/**
 * trim leading and trailing white space down to 1 char
 * @param u the userdata struct
 * @param cptr VAR pointer to the string
 * @param len VAR pointer to its length
 */
static void trim( struct UserData *u, char **cptr, int *len )
{
    char *text = *cptr;
    int length = *len;
    int i;
    int state = u->last_char_type;
    // trim front of string
    for ( i=0;i<length;i++ )
    {
        switch ( state )
        {
            case 0: // last char was non-space/non-CR 
                if ( text[i] == ' ' || text[i] == '\t' )
                    state = 1;
                else if ( text[i] == '\r' )
                    state = 2;
                else if ( text[i] == '\n' )
                    state = 3;
                else
                    state = -1;
                break;
            case 1: // last char was a space
                if ( isspace(text[i]) )
                {
                    (*cptr)++;
                    (*len)--;
                    if ( text[i] == '\n' )
                        state = 3;
                    else if ( text[i] == '\r' )
                        state = 2;
                }
                else
                    state = -1;
                break;
            case 2: // last char was a CR
                if ( text[i] == '\n' )
                    state = 3;
                else if ( isspace(text[i]) )
                {
                    (*cptr)++;
                    (*len)--;
                }
                else
                    state = -1;
                break;
            case 3: // last char was a LF
                if ( isspace(text[i]) )
                {
                    (*cptr)++;
                    (*len)--;
                }
                else
                    state = -1;
                break;
            
        }
        if ( state < 0 )
            break;
    }
    // trim rear of string
    length = *len;
    text = *cptr;
    state = 0;
    for ( i=length-1;i>=0;i-- )
    {
        switch ( state )
        {
            case 0: // initial state or TEXT
                if ( text[i] == ' ' || text[i] == '\t' )
                    state = 1;
                else if ( text[i] == '\r' )
                    state = 2;
                else if ( text[i] == '\n' )
                    state = 3;
                else
                {
                    state = -1;
                    u->last_char_type = CHAR_TYPE_TEXT;
                }
                break;
            case 1: // last char was space
                if ( text[i] == ' ' || text[i] == '\t' )
                    (*len)--;
                else if ( text[i] == '\n' )
                {
                    (*len)--;
                    state = 3;
                }
                else if ( text[i] == '\r' )
                {
                    (*len)--;
                    state = 2;
                }
                else
                {
                    state = -1;
                    u->last_char_type = CHAR_TYPE_SPACE;
                }
                break;
            case 2: // last char was CR
                if ( text[i] == '\r' )
                {
                    (*len)--;
                }
                else
                {
                    u->last_char_type = CHAR_TYPE_CR;
                    state = -1;
                }
                break;
            case 3: // last char was LF
                if ( text[i] == '\n' )
                {
                    (*len)--;
                }
                else
                {
                    u->last_char_type = CHAR_TYPE_LF;
                    state = -1;
                }
                break;
        }
        if ( state == -1 )
            break;
    }
    if ( state != -1 && (*len)>0 )
        u->last_char_type = state;
}
/**
 * Handle characters during stripping. We basically write
 * everything to all the files currently identified by
 * current_bitset.
 * @param userData a userdata structure or NULL
 */
static void XMLCALL charhndl( void *userData, const XML_Char *s, int len)
{
	if ( stack_empty(ignoring) )
	{
		size_t n;
        struct UserData *u = (struct UserData *)userData;
		if ( len == 1 && s[0] == '&' )
		{
			n = dest_file_write( text_dest, "&amp;", 5 );
			u->current_text_offset += 5;
            u->last_char_type = CHAR_TYPE_TEXT;
		}
        else 
		{
			char *text = (char*)s;
            trim( u, &text, &len );
            if ( len > 0 )
            {
                n = dest_file_write( text_dest, text, len );
                u->current_text_offset += len;
                if ( n != len )
                    error( "stripper: write error on text file" );
            }
		}
	}
    else if ( !is_whitespace(s,len) )
    {
        range *r = stack_peek( range_stack );
        if ( len == 1 && s[0] == '&' )
        {
			range_add_content( r, "&amp;", 5 );
        }
        else
            range_add_content( r, s, len );
    }
    // else it's inter-element white space
}
/**
 * Scan the source file, looking for tags to send to
 * the tags file and text to the text file.
 * @param buf the data to parse
 * @param len its length
 * @return 1 if it succeeded, 0 otherwise
 */
static int scan_source( const char *buf, int len )
{
	int res = 1;
	memset( &user_data, 0, sizeof(struct UserData) );
    user_data.last_char_type = CHAR_TYPE_LF;
    parser = XML_ParserCreate( NULL );
    if ( parser != NULL )
    {
        XML_SetElementHandler( parser, start_element_scan,
            end_element_scan );
        XML_SetCharacterDataHandler( parser, charhndl );
        XML_SetUserData( parser, &user_data );
        if ( XML_Parse(parser,buf,len,1) == XML_STATUS_ERROR )
        {
            error(
                "stripper: %s at line %" XML_FMT_INT_MOD "u\n",
                XML_ErrorString(XML_GetErrorCode(parser)),
                XML_GetCurrentLineNumber(parser));
            return 0;
        }
        XML_ParserFree( parser );
        // now write out the markup files
        //....
    }
    else
    {
        fprintf(stderr,"stripper: failed to create parser\n");
        res = 0;
    }
	return res;
}
/**
 * Look up a format in our list.
 * @param fmt_name the format's name
 * @return its index in the table or -1
 */
static int lookup_format( const char *fmt_name )
{
	int i;
	for ( i=0;i<num_formats;i++ )
	{
		if ( strcmp(formats[i].name,fmt_name)==0 )
			return i;
	}
	return -1;
}
/**
 * Open the dest files
 * @return 1 if it worked else 0
 */
static int open_dest_files()
{
    int res = 0;
    text_dest = dest_file_create( text_kind, NULL, "", barefile, 
        &formats[selected_format] );
    if ( text_dest != NULL )
    {
        res = dest_file_open( text_dest );
        if ( res )
        {
            // always at least one markup df is needed
            dest_file *df = dest_file_create( markup_kind, NULL, 
                (char*)formats[selected_format].middle_name, 
                barefile, &formats[selected_format] );
            if ( df != NULL && rules != NULL )
            {
                // ask the recipe which markup files to create
                int i,n_layers = recipe_num_layers( rules );
                markup_dest = calloc( n_layers+2, sizeof(dest_file*));
                if ( markup_dest != NULL )
                {
                    markup_dest[0] = df;
                    for ( i=1;i<=n_layers;i++ )
                    {
                        layer *l = recipe_layer( rules, i-1 );
                        char *name = layer_name( l );
                        int mlen = strlen(formats[selected_format].middle_name)
                            +strlen(name)+2;
                        char *mid_name = malloc( mlen );
                        if ( mid_name != NULL )
                        {
                            snprintf( mid_name, mlen, "%s-%s", 
                                formats[selected_format].middle_name, name );
                            //dest_kind kind, layer *l, char *midname, 
                            //char *name, format *f
                            markup_dest[i] = dest_file_create( markup_kind,
                                l, mid_name, barefile, 
                                &formats[selected_format] );
                            free( mid_name );
                        }
                        else
                        {
                            fprintf(stderr,
                                "stripper: failed to allocate name\n");
                            break;
                        }
                    }
                    if ( i == n_layers )
                        res = 1;
                }
                else
                    fprintf(stderr,
                        "stripper: failed to allocate markup array\n");
                // now open the markup dest files 
                if ( res )
                {
                    i = 0;
                    while ( markup_dest[i] != NULL && res )
                        res = dest_file_open( markup_dest[i++] );
                }
            }
        }
    }
    if ( res )
        dest_map = hashmap_create();
    return res&&(dest_map!=NULL);
}
#ifdef JNI
static int set_string_field( JNIEnv *env, jobject obj, 
    const char *field_name, char *value )
{
    int res = 0;
    jfieldID fid;
    jstring jstr;
    initlog();
    //printf("setting string field\n");
    jclass cls = (*env)->GetObjectClass(env, obj);
    fid = (*env)->GetFieldID(env, cls, field_name, "Ljava/lang/String;");
    if (fid != NULL) 
    {
        jstr = (*env)->NewStringUTF( env, value );
        if (jstr != NULL) 
        {
            (*env)->SetObjectField(env, obj, fid, jstr);
            res = 1;
        }
    }
    return res;
}
static int add_layer( JNIEnv *env, jobject obj, char *value )
{
    int res = 0;
    jstring jstr;
    jstr = (*env)->NewStringUTF( env, value );
    if (jstr != NULL) 
    {
        jclass cls = (*env)->GetObjectClass( env, obj );
        jmethodID mid = (*env)->GetMethodID( env, cls,"addLayer",
            "(Ljava/lang/String;)V");
        if ( mid == 0 )
        {
            tmplog("stripper: failed to find method addLayer\n");
            res = 0;
        }
        else
        {
            (*env)->ExceptionClear( env );
            (*env)->CallVoidMethod( env, obj, mid, jstr);
            if((*env)->ExceptionOccurred(env)) 
            {
                fprintf(stderr,"stripper: couldn't add layer\n");
                (*env)->ExceptionDescribe( env );
                (*env)->ExceptionClear( env );
            }
            else
                res = 1;
        }
    }
    return res;
}
/*
 * Class:     calliope_AeseStripper
 * Method:    strip
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcalliope/JSONResponse;Lcalliope/JSONResponse;)I
 */
JNIEXPORT jint JNICALL Java_calliope_AeseStripper_strip
  (JNIEnv *env, jobject obj, jstring xml, jstring recipe, jstring format, 
    jstring style, jobject text, jobject markup)
{
	int res = 0;
    jboolean iscopy;
    const char *c_format;
    // read recipe -- now needed by open_dest_files
    if ( recipe != NULL )
    {
        const char* rstr = (*env)->GetStringUTFChars(env, recipe, &iscopy);
        rules = recipe_load( rstr, strlen(rstr) );
        if ( iscopy )
            (*env)->ReleaseStringUTFChars( env, recipe, rstr );
    }
    else
        rules = recipe_new();
    res = open_dest_files();
    if ( res )
    {
        c_format = (*env)->GetStringUTFChars(env, format, &iscopy);
        selected_format = lookup_format( c_format );
        if ( iscopy )
            (*env)->ReleaseStringUTFChars( env, format, c_format ); 
        range_stack = stack_create();
        if ( range_stack == NULL )
            tmplog( "stripper: failed to allocate range stack" );
        ignoring = stack_create();
        if ( ignoring == NULL )
            tmplog( "stripper: failed to allocate ignore stack" );
        if ( ignoring != NULL && range_stack != NULL )
        {
            // write header
            const char* cstr = (*env)->GetStringUTFChars(env, style, &iscopy);
            int i=0;
            while ( markup_dest[i] )
            {
                res = formats[selected_format].hfunc( NULL, 
                    dest_file_dst(markup_dest[i]), cstr );
                i++;
            }
            if ( iscopy )
                (*env)->ReleaseStringUTFChars( env, style, cstr ); 
            // parse XML
            if ( res )
            {
                const char *xmlText = (*env)->GetStringUTFChars(env,xml,0);
                if ( xmlText != NULL )
                {
                    int xlen = strlen(xmlText);
                    res = scan_source( xmlText, xlen );
                    (*env)->ReleaseStringUTFChars( env, xml, xmlText );
                }
                else
                {
                    res = 0;
                    tmplog("failed to get xml text\n");
                }
                // we're finished with these
                stack_delete( range_stack );
                stack_delete( ignoring );
            }
            else
            {
                tmplog("write header failed\n");
                res = 0;
            }
            // write out text
            dest_file_close( text_dest, 0 );
            int tlen = dest_file_len( text_dest );
            // save result to text and markup objects
            if ( res )
            {
                //printf("about to set text field\n");
                res = set_string_field( env, text, "body", 
                    ramfile_get_buf(dest_file_dst(text_dest)) );
                //tmplog("set_string_field returned %d\n",res);
            }
            dest_file_dispose( text_dest );
            i=0;
            while ( markup_dest[i] != NULL && res )
            {
                dest_file_close( markup_dest[i], tlen );
                if ( res ) 
                {
                    if ( i == 0 )
                    {
                        res = set_string_field( env, markup, "body", 
                        ramfile_get_buf(dest_file_dst(markup_dest[i])) );
                    }
                    else
                    {
                        res = add_layer( env, markup, 
                            ramfile_get_buf(dest_file_dst(markup_dest[i])) );
                    }
                }
                dest_file_dispose( markup_dest[i++] );
            }
            free( markup_dest );
            if ( dest_map != NULL )
                hashmap_dispose( dest_map );    
        }
    }
    return res;
}

/*
 * Class:     AeseStripper
 * Method:    version
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_calliope_AeseStripper_version
  (JNIEnv *env, jobject obj )
{
	return (*env)->NewStringUTF(env, 
		"stripper version 1.1 (c) Desmond Schmidt 2011" );
}

/*
 * Class:     AeseStripper
 * Method:    formats
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_calliope_AeseStripper_formats
  (JNIEnv *env, jobject obj )
{
	jobjectArray ret = (jobjectArray)(*env)->NewObjectArray(
		env,
        num_formats,
 		(*env)->FindClass(env,"java/lang/String"),
		(*env)->NewStringUTF(env,""));
	if ( ret != NULL )
	{
		int i;
        for( i=0;i<num_formats;i++ ) 
		{
		    (*env)->SetObjectArrayElement(env,
				ret,i,(*env)->NewStringUTF(env, formats[i].name));
		}
	}
	return ret;
}
#else
/**
 * Get the file length of the src file
 * @return its length as an int
 */
static int file_size( const char *file_name )
{
    FILE *fp = fopen( file_name, "r" );
    long sz = -1;
    if ( fp != NULL )
    {
        fseek(fp, 0L, SEEK_END);
        sz = ftell(fp);
        fclose( fp );
    }
    return (int) sz;
}
/**
 * Read a file contents 
 * @param file_name the name of the file to read
 * @param len its length once entirely read
 * @return an allocated buffer. caller MUST dispose
 */
static const char *read_file( const char *file_name, int *len )
{
    char *buf = NULL;
    *len = file_size(file_name);
    if ( *len > 0 )
    {
        FILE *src_file = fopen( file_name, "r" );
        int flen;
        if ( src_file != NULL )
        {
            buf = calloc( 1, (*len)+1 );
            if ( buf != NULL )
            {
                flen = (int)fread( buf, 1, *len, src_file );
                if ( flen != *len )
                    error("couldn't read %s\n",file_name);
            }
            else
                error("couldn't allocate buf\n");
            fclose( src_file );
            
        }
        else
            error("failed to open %s\n",file_name );
    }
    return buf;
}
/**
 * Print a simple help message. If we get time we can
 * make a man page later.
 */
static void print_help()
{
	printf(
		"usage: stripper [-h] [-v] [-s style] [-l] [-f format] "
        "[-r recipe] XML-file\n"
		"stripper removes tags from an XML file and saves "
			"them to a separate file\n"
		"in a standoff markup format. The original text is "
			"also preserved and is\n"
		"written to another file. Options are: \n"
		"-h print this help message\n"
		"-v print the version information\n"
        "-s style. Specify a style name (default \"TEI\")\n"
		"-f format. Specify a supported format\n"
		"-l list supported formats\n"
		"-r recipe-file specifying removals and simplifications in XML or JSON\n"
		"XML-file the only real argument is the name of an XML "
			"file to split.\n");
}
/**
 * Check whether a file exists
 * @param file the file to test
 * @return 1 if it does, 0 otherwise
 */
static int file_exists( const char *file )
{
	FILE *EXISTS = fopen( file,"r" );
	if ( EXISTS )
	{
		fclose( EXISTS );
		return 1;
	}
	return 0;
}
/**
 * List the formats registered with the main program. If the user
 * defines another format he/she must call the register routine to 
 * register it. Then this command returns a list of dynamically
 * registered formats.
*/
static void list_formats()
{
	int i;
	for ( i=0;i<num_formats;i++ )
	{
		printf( "%s\n",formats[i].name );
	}
}
/**
 * Check the commandline arguments
 * @param argc number of commandline args+1
 * @param argv array of arguments, first is program name
 * @return 1 if they were OK, 0 otherwise
 */
static int check_args( int argc, char **argv )
{
	char *dot_pos;
	int sane = 1;
	if ( argc < 2 )
		sane = 0;
	else
	{
		int i,rlen;
        const char *rdata = NULL;
		for ( i=1;i<argc;i++ )
		{
			if ( strlen(argv[i])==2 && argv[i][0]=='-' )
			{
				switch ( argv[i][1] )
				{
					case 'v':
						printf( "stripper version 1.1 (c) "
								"Desmond Schmidt 2011\n");
						doing_help = 1;
						break;
					case 'h':
						print_help();
						doing_help = 1;
						break;
					case 'f':
						if ( i < argc-2 )
						{
							selected_format = lookup_format( argv[i+1] );
							if ( selected_format == -1 )
								error("stripper: format %s not supported.\n",
                                    argv[i+1]);
						}
						else
							sane = 0;
						break;
					case 'l':
						list_formats();
						doing_help = 1;
						break;
                    case 'r':
                        rdata = read_file( argv[i+1], &rlen );
                        if ( rdata != NULL )
                        {
                            rules = recipe_load( rdata, rlen );
                            free( (char*)rdata );
                        }
                        break;
                    case 's':
                        style = argv[i+1];
                        break;
				}
			}
			if ( !sane )
				break;
		}
		if ( !doing_help )
		{
			sscanf( argv[argc-1], "%127s", src );
			sane = file_exists( src );
			if ( !sane )
				fprintf(stderr,"stripper: can't find %s\n",src );
            else
            {
                strncpy( barefile, src, FILE_NAME_LEN );
                dot_pos = strrchr( barefile, '.' );
                if ( dot_pos != NULL )
                    dot_pos[0] = 0;
            }
		}
	}
	return sane;
}
/**
 * Tell them how to use the program.
 */
static void usage()
{
	printf( "usage: stripper [-h] [-v] [-s style] [-l] [-f format] "
        "[-r recipe] XML-file\n" );
}
/**
 * The main entry point
 * @param argc number of commandline args+1
 * @param argv array of arguments, first is program name
 * @return 0 to the system
 */
int main( int argc, char **argv )
{
	if ( check_args(argc,argv) )
	{
		if ( !doing_help )
		{
			int i,res = 0;
            if ( rules == NULL )
                rules = recipe_new();
			if ( !open_dest_files() )
                error("stripper: couldn't open dest files\n");
			range_stack = stack_create();
			if ( range_stack == NULL )
				error( "stripper: failed to allocate store for range stack" );
			ignoring = stack_create();
			if ( ignoring == NULL )
				error( "stripper: failed to allocate store for ignore stack" );
            // write header
            i=0;
			while ( markup_dest[i] )
            {
                res = formats[selected_format].hfunc( NULL, 
                    dest_file_dst(markup_dest[i]), style );
                i++;
            }
            // parse XML, prepare body for writing
            if ( res )
            {
                int len;
                const char *data = read_file( src, &len );
                if ( data != NULL )
                {
                    res = scan_source( data, len );
                    free( (char*)data );
                }
            }
            stack_delete( ignoring );
            stack_delete( range_stack );
            // write out text
            dest_file_close( text_dest, 0 );
            int tlen = dest_file_len( text_dest );
            dest_file_dispose( text_dest );
            // write body for markup files
            i=0;
            while ( markup_dest[i] != NULL )
            {
                dest_file_close( markup_dest[i], tlen );
                dest_file_dispose( markup_dest[i++] );
            }
            free( markup_dest );
            if ( dest_map != NULL )
                hashmap_dispose( dest_map );
		}
        if ( rules != NULL )
            rules = recipe_dispose( rules );
	}
	else
		usage();
	return 0;
}
#endif