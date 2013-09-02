/* 
 * File:   main.c
 * Author: desmond
 *
 * Created on July 15, 2012, 8:37 AM
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "upload.h"
#include "download.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
/* NULL-terminated array of formats */
static char **formats = NULL;
/** docid to query for download */
static char *docid = ".*";
/** name for downloaded archive */
static char *name = "archive";
/** type of zipping for downloaded archive */
static char *zip_type = "TAR_GZ";
/** host to upload to/download from */
static char *host = "http://localhost:8080/";
/** the folder to upload */
static char *folder = NULL;
/** if set to 1 add required configs and corforms */
int add_required = 0;
/**
 * Read the comma-separated list of formats
 * @param fmts a list of TEXT,MVD,XML,MIXED
 * @return 
 */
static char **read_formats( char *fmts )
{
    char **array = calloc( 5, sizeof(char*) );
    if ( array != NULL )
    {
        int i = 0;
        char *text = strdup( fmts );
        if ( text != NULL )
        {
            char *token = strtok( text, "," );
            while ( token != NULL )
            {
                array[i++] = strdup(token);
                token = strtok( NULL, "/" );
            }
            free( text );
        }
    }
    return array;
}
/**
 * Test the first char of a commandline parameter
 * @param str the param
 * @param c the char
 * @return 1 if str starts with c else 0
 */
static int starts_with( char *str, char c )
{
    if ( strlen(str)>0 && str[0] == c )
        return 1;
    else
        return 0;
}
/**
 * Check arguments
 * @param argc the number of arguments
 * @param argv the array of commandline arguments
 * @return 1 if they were sane
 */
static int check_args( int argc, char **argv )
{
    int i,sane = argc>1;
    int prev_dot = 0;
    if ( sane )
    {
        for ( i=1;i<argc;i++ )
        {
            int arglen = strlen(argv[i]);
            // user may have typed ".*"
            if ( arglen>0 && argv[i][0]=='.' )
            {
                if ( prev_dot )
                {
                    fprintf(stderr,"Warning: use double-quotes around .* "
                        "(shell expansion)\n");
                    exit(0);
                    sane = 0;
                    break;
                }
                else
                    prev_dot = 1;
            }
            else if ( arglen>1 && argv[i][0]=='-' )
            {
                switch ( argv[i][1] )
                {
                    case 'h':
                        if ( argc >i+1 )
                            host = argv[i+1];
                        break;
                    case 'f':
                        if ( argc >i+1 )
                            formats = read_formats( argv[i+1] );
                        break;
                    case 'd':
                        if ( argc >i+1 )
                            docid = argv[i+1];
                        break;
                    case 'n':
                        if ( argc > i+1 )
                            name = argv[i+1];
                        break;
                    case 'z':
                        if ( argc > i+1 )
                        {
                            if ( strcmp(argv[i+1],"tar_gz")==0 )
                                zip_type = "TAR_GZ";
                            else if ( strcmp(argv[i+1],"zip")==0 )
                                zip_type = "ZIP";
                            else
                                sane = 0;
                        }
                        break;
                    case 'r':
                        add_required = 1;
                        break;
                    default:
                        sane = 0;
                        break;
                }
            }
        }
        if ( sane )
        {  
            // decide if we are downloading or uploading
            if ( argc>=2 && !starts_with(argv[argc-1],'-') 
                && !starts_with(argv[argc-2],'-') )
            {
                folder = argv[argc-1];
            }
            else if ( formats == NULL )
            {
                formats = calloc( 2, sizeof(char*));
                if ( formats != NULL )
                    formats[0] = "MVD";
            }
        }
    }
    return sane;
}
/**
 * Tell people how this things should be invoked. This is not a man page.
 */
static void usage()
{
    printf( 
    "\npdef-tool [-h host] [-f formats] [-d docid] [-n name] "
    "[-z zip-type] [-r] [folder]\n\n"
    "Download parameters:\n"
    "  host: url for download (defaults to http://localhost:8080/)\n"
    "  formats: a comma-separated list of TEXT,XML,MVD,MIXED (defaults to MVD)\n"
    "  docid: wildcard prefix docid, e.g. english/poetry.* (defaults to \".*\")\n"
    "  name: name of archive to download (defaults to archive)\n"
    "  zip-type: type of zip archive, either tar_gz or zip (defaults to tar_gz)\n"
    "  -r: download required corforms and all configs on server\n\n"
    "Upload parameter:\n"
    "  folder: relative path to folder for uploading\n\n"
    );
}
/*
 * Main entry point
 */
int main( int argc, char** argv ) 
{
    if ( check_args(argc,argv) )
    {
        int res = 1;
        if ( folder == NULL )
            res = download( host, formats, docid, name, zip_type, add_required );
        else
            res = upload( folder );
    }
    else
        usage();
}