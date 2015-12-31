#ifdef JNI
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <unicode/uchar.h>
#include <unicode/ustring.h>
#include <unicode/ustdio.h>
#include "calliope_AeseFormatter.h"
#include "master.h"
#include "memwatch.h"

static int set_string_field( JNIEnv *env, jobject obj, 
    const char *field_name, UChar *value )
{
    int res = 0;
    jfieldID fid;
    jstring jstr;
    //printf("setting string field\n");
    jclass cls = (*env)->GetObjectClass(env, obj);
    fid = (*env)->GetFieldID(env, cls, field_name, "Ljava/lang/String;");
    if (fid != NULL) 
    {
        jstr = (*env)->NewString( env, value, u_strlen(value) );
        if (jstr != NULL) 
        {
            (*env)->SetObjectField(env, obj, fid, jstr);
            res = 1;
        }
    }
    return res;
}
/**
 * Change this to report differently when a library or commandline tool
 */
void jni_report( const char *fmt, ... )
{
    va_list ap;
    UChar message[128];
    va_start( ap, fmt );
    u_vsnprintf( message, 128, fmt, ap );
    UFILE *db = u_fopen("/tmp/formatter-debug.txt","a+",NULL,NULL);
    if ( db != NULL )
    {
        u_fprintf( db, "%s", message );
        u_fclose( db );
    }
    va_end( ap );
}
static void unload_string_c( JNIEnv *env, jstring jstr, const char *cstr, 
    jboolean copied )
{
    if ( copied )
        (*env)->ReleaseStringUTFChars( env, jstr, cstr );
}
static const char *load_string_c( JNIEnv *env, jstring jstr, jboolean *copied )
{
    return (*env)->GetStringUTFChars(env, jstr, copied);  
}
static const jchar *load_string( JNIEnv *env, jstring jstr, jboolean *copied )
{
    return (*env)->GetStringChars(env, jstr, copied);  
}
static void unload_string( JNIEnv *env, jstring jstr, const jchar *ustr, 
    jboolean copied )
{
    if ( copied )
        (*env)->ReleaseStringChars( env, jstr, ustr );
}
/*
 * Class:     calliope_AeseFormatter
 * Method:    format
 * Signature: (Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;Lcalliope/json/JSONResponse;)I
 */
JNIEXPORT jint JNICALL Java_calliope_AeseFormatter_format
  (JNIEnv *env, jobject obj, jstring text, jobjectArray markup,
    jobjectArray css, jobject jsonHtml)
{
    int res=0;
    jsize i,len;
    UChar *html;
    jboolean isTextCopy=0;
    jchar *t_data = (jchar*)load_string(env, text, &isTextCopy);
    int t_len = u_strlen( t_data );
    if ( t_data != NULL && markup != NULL && css != NULL  )
    {
        jboolean isMarkupCopy;
        jni_report( "about to call master_create\n" );
        master *hf = master_create( t_data, t_len );
        if ( hf != NULL )
        {
            jni_report( "about to get markup array length\n" );
            len = (*env)->GetArrayLength(env, markup);
            for ( i=0;i<len;i++ )
            {
                res = 1;
                jni_report( "about to get markup str\n" );
                jstring markup_str = (jstring)(*env)->GetObjectArrayElement(
                    env, markup, i );
                jni_report( "about to load c string\n" );
                const char *markup_data = load_string_c(env, markup_str, 
                    &isMarkupCopy);
                if ( markup_data != NULL )
                {
                    res = master_load_markup( hf, markup_data,
                        (int)strlen(markup_data) );
                    unload_string_c( env, markup_str, markup_data,isMarkupCopy);
                }
                if ( !res )
                    break;
            }
            if ( res )
            {
                len = (*env)->GetArrayLength(env, css);
                for ( i=0;i<len;i++ )
                {
                    jboolean isCssCopy;
                    jstring css_str = (jstring)(*env)->GetObjectArrayElement(
                        env, css, i);
                    const char *css_data = load_string_c(env,css_str,&isCssCopy);
                    if ( css_data != NULL )
                    {
                        res = master_load_css( hf, css_data, (int)strlen(css_data) );
                        unload_string_c( env, css_str, css_data, isCssCopy );
                        if ( !res )
                            break;
                    }
                }
                if ( res )
                {
                    //jni_report( "about to call master_convert\n" );
                    html = master_convert( hf );
                    //jni_report( "finished calling master_convert\n" );
                    if ( html != NULL )
                    {
                        res = set_string_field( env, jsonHtml, "body", html );
                        if ( res == 0 )
                            jni_report("set string failed tried to write %d chars\n",u_strlen(html));
                    }
                }
            }
            master_dispose( hf );
        }
        unload_string(env,text,t_data,isTextCopy);
    }
#ifdef DEBUG_MEMORY
        memory_print();
#endif
    return res;
}
#endif
