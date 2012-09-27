/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */
package hritserver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author desmond
 */
/**
 * simple uri encoder, made from the spec at:
 * http://www.ietf.org/rfc/rfc2396.txt
 * Feel free to copy this. I take no responsibility for anything, ever.
 * @author Daniel Murphy
 */
public class URLEncoder 
{
	private static String mark = "-_.!~*'()\"";
    /**
     * Add a get param to a get URL
     * @param url the url as a string
     * @param key the key of the argument
     * @param value its value
     * @return the encoded url with the param
     */
 	public static String addGetParam( String url, String key, String value )
	{
		String sep = (url.lastIndexOf('?')==-1)?"?":"&";
		return url+sep+encodeParam(key)+"="+value;//encodeParam(value);
	}
    public static String append( String url, String component )
    {
        StringBuilder baseURL = new StringBuilder( url );
        if ( baseURL.length()>0&&component.length()!=0 )
        {
            char last = baseURL.charAt(baseURL.length()-1);
            char first = component.charAt(0);
            if ( last !='/' && first !='/')
                baseURL.append( "/" );
            else if ( last=='/'&&first=='/' )
                component = component.substring(1);
            baseURL.append( component.replace(" ","%20") );
        }
        else 
            baseURL.append( component );
        return baseURL.toString();
    }
    static String encodeParam( String arg ) 
	{
        StringBuilder url = new StringBuilder(); // Encoded URL
        // thanks Marco!
 
        char[] chars = arg.toCharArray();
        for(int i = 0; i<chars.length; i++) {
            char c = chars[i];
            if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
                url.append(c);
            }
            else {
                url.append("%");
                url.append(Integer.toHexString((int)c));
            }
        }
        return url.toString();
    }
    /**
     * Get an internal response to a crafted URL
     * @param rawUrl the raw url with encoded GET params
     */
    public static String getResponseForUrl( String rawUrl ) throws Exception
    {
        URL url = new URL( rawUrl );
        long smallTimeout = 50;// milliseconds
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long timeTaken=0,start = System.currentTimeMillis();
        while ( is.available()>0 ||timeTaken<smallTimeout )
        {
            if ( is.available()>0 )
            {
                byte[] data = new byte[is.available()];
                is.read( data );
                bos.write( data );
                // restart timeout
                timeTaken = 0;
            }
            else
                timeTaken = System.currentTimeMillis()-start;
        }
        is.close();
        return bos.toString();
    }
}
