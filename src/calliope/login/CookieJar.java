/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.login;
import java.util.HashMap;

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
    public void setCookie( String host, String user, String cookie )
    {
        put( user+"@"+host, cookie );
    }
    public String getCookie( String host, String user )
    {
        return get( user+"@"+host );
    }
}
