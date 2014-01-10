/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.login;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

/**
 * A cache of stored cookies
 * @author desmond
 */
public class CookieJar extends HashMap<String,String> 
{
    public static CookieJar cookies;
    static 
    {
        cookies = new CookieJar();
    }
    public static void setCookie( String host, String user, String cookie )
    {
        cookies.put( user+"@"+host, cookie );
    }
    private static Date readGMTDate( String gmtTime )
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss z");
        return sdf.parse(gmtTime,new ParsePosition(0));
    }
    /**
     * Get a cookie but check that it is valid
     * @param host the host name
     * @param user the user name
     * @return a valid cookie or null
     */
    public static String getCookie( String host, String user )
    {
       String key = user+"@"+host;
       String cookie = cookies.get( key );
       if ( cookie != null )
       {
           int index = cookie.indexOf("Expires=");
           if ( index != -1 )
           {
               String expr = cookie.substring(index+8);
               int index2 = expr.indexOf(";");
               if ( index2 != -1 )
                   expr = expr.substring(0,index2);
               Date d = readGMTDate( expr );
               Date now = Calendar.getInstance().getTime();
               if ( d.compareTo(now) < 0 )
               {
                   cookies.remove( key );
                   cookie = null;
               }
           }
       }
       return cookie;
    }
}
