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
/*
 * Class:     calliope_AeseFormatter
 * Method:    format
 * Signature: ([C[Ljava/lang/String;[Ljava/lang/String;Lcalliope/json/JSONResponse;)I
 */
JNIEXPORT jint JNICALL Java_calliope_AeseFormatter_format
  (JNIEnv *env, jobject obj, jcharArray text, jobjectArray markup,
    jobjectArray css, jobject jsonHtml)
{
    int res=0;
    jsize i,len;
    UChar *html;
    jboolean isCopy=0;
    //jni_report("entered format\n");
    jchar *t_data = (*env)->GetCharArrayElements(env, text, &isCopy);
    int t_len = (*env)->GetArrayLength( env, text );
    if ( t_data != NULL && markup != NULL && css != NULL  )
    {
        jboolean isMarkupCopy;
        master *hf = master_create( t_data, t_len );
        if ( hf != NULL )
        {
            len = (*env)->GetArrayLength(env, markup);
            for ( i=0;i<len;i++ )
            {
                res = 1;
                jstring markup_str = (jstring)(*env)->GetObjectArrayElement(
                    env, markup, i );
                const jchar *markup_data = (*env)->GetStringChars(env,
                    markup_str, &isMarkupCopy);
                if ( markup_data != NULL )
                {
                    res = master_load_markup( hf, markup_data,
                        (int)u_strlen(markup_data) );
                }
                if ( markup_data != NULL && isMarkupCopy==JNI_TRUE )
                    (*env)->ReleaseStringChars( env, markup_str, markup_data );
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
                    const jchar *css_data = (*env)->GetStringChars(env, css_str,
                        &isCssCopy);
                    if ( css_data != NULL )
                    {
                        res = master_load_css( hf, css_data, (int)u_strlen(css_data) );
                        if ( isCssCopy==JNI_TRUE )
                            (*env)->ReleaseStringChars( env, css_str, css_data );
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
                        res = set_string_field( env, jsonHtml, "body", html );
                }
            }
            master_dispose( hf );
        }
    }
    if ( t_data != NULL )
        (*env)->ReleaseCharArrayElements( env, text, t_data, JNI_ABORT );
#ifdef DEBUG_MEMORY
        memory_print();
#endif
    return res;
}
#endif
