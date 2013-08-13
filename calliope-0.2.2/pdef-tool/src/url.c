/*
 * This file is part of mmpupload.

    mmpupload is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mmpupload is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mmpupload.  If not, see <http://www.gnu.org/licenses/>.*/
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include "url.h"
#ifdef MEMWATCH
#include "memwatch.h"
#endif
/*
 * Break up a standard URL into its components
 */
struct url_struct
{
    char *host;
    char *path;
    char *protocol;
    int port;
};
/**
 * Read the url's protocol
 * @param u the url object to store it in
 * @param spec updatable pointer to url text
 * @return 1 if it was successful, else 0
 */
static int url_protocol( url *u, const char **spec )
{
    char *pos = strstr( *spec, "://" );
    if ( pos != NULL )
    {
        int plen = pos-(*spec);
        u->protocol = malloc( plen+1 );
        if ( u->protocol != NULL )
        {
            strncpy( u->protocol, *spec, plen );
            u->protocol[plen] = 0;
            *spec = pos+3;
            return 1;
        }
        else
            fprintf(stderr,"url: failed to allocate protocol\n");
    }
    else    // set sensible defaults
    {
        u->protocol = strdup("http");
        if ( u->protocol == NULL )
            fprintf(stderr,"url: failed to create protocol string\n");
    }
    return 0;
}
/**
 * Read the url's host
 * @param u the url object to store in
 * @param spec updatable string of url starting with the host
 * @return 1 if it worked, else 0
 */
static int url_host( url *u, const char **spec )
{
    int res = 0;
    if ( strlen(*spec)>0&&(*spec)[0] == '/' )
    {
        u->host = strdup("localhost");
        if ( u->protocol == NULL|| u->host == NULL )
            fprintf(stderr,"url: failed to allocate protocol\n");
        else
            res = 1;
    }
    else
    {
        char *pos;
        char *pos1 = strstr( *spec, ":" );
        char *pos2 = strstr( *spec, "/" );
        if ( pos1 != NULL )
            pos = pos1;
        else 
            pos = pos2;
        if ( pos != NULL )
        {
            int hlen = pos-*spec;
            u->host = malloc( hlen+1 );
            if ( u->host != NULL )
            {
                strncpy( u->host, *spec, hlen );
                u->host[hlen] = 0;
                *spec += hlen;
                res = 1;
            }
            else
                fprintf(stderr,"url:failed to allocate host\n");
        }
        else
            fprintf(stderr,"url: invalid url\n");
    }
    return res;
}
/**
 * Read the url's port
 * @param u the url object to store in
 * @param spec updatable string of url starting with the host
 * @return 1 if it worked, else 0
 */
static int url_port( url *u, const char **spec )
{
    char *pos = strstr( *spec, ":" );
    if ( pos != NULL )
    {
        u->port = 0;
        pos++;
        while( *pos != 0 && isdigit(*pos) )
        {
            u->port *= 10;
            u->port += *pos - '0';
            pos++;
        }
        *spec = pos;
    }
    return 1;
}
/**
 * Read the url's path
 * @param u the url object to store in
 * @param spec updatable string of url starting with the host
 * @return 1 if it worked, else 0
 */
static int url_path( url *u, const char **spec )
{
    int plen = strlen(*spec);
    u->path = malloc( plen+1 );
    if ( u->path != NULL )
    {
        strncpy( u->path, *spec, plen );
        u->path[plen] = 0;
        *spec += plen;
        return 1;
    }
    else
        fprintf(stderr,"url: failed to allocate path\n");
    return 0;
}
/**
 * Create a URL object
 * @param spec a full URL
 * @return a valid URL object or NULL on failure
 */
url *url_create( const char *spec )
{
    url *u = calloc( 1, sizeof(url) );
    if ( u != NULL )
    {
        u->port = 80;
        int res = url_protocol( u, &spec );
        if ( res )
            res = url_host( u, &spec );
        if ( res )
            res = url_port( u, &spec );
        if ( res )
            res = url_path( u, &spec );
        if ( !res )
        {
            url_dispose( u );
            u = NULL;
        }
    }
    return u;
}            
/**
 * Dispose of a url's memory
 * @param u the url object
 */
void url_dispose( url *u )
{
    if ( u->host != NULL )
        free( u->host );
    if ( u->path != NULL )
        free( u->path );
    if ( u->protocol != NULL )
        free( u->protocol );
    free( u );
}
char *url_get_host( url *u )
{
    return u->host;
}
int url_get_port( url *u )
{
    return u->port;
}
char *url_get_path( url *u )
{
    return u->path;
}