/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get;

import calliope.constants.JSONKeys;
import calliope.exception.AeseException;
import calliope.exception.LoginException;
import calliope.json.JSONDocument;
import calliope.login.CookieJar;
import calliope.login.Login;
import calliope.login.LoginFactory;
import calliope.login.LoginType;
import calliope.Utils;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Read a link document pointing to a real document
 * @author desmond
 */
public class AeseLink 
{
    /**
     * Add arguments to a raw GET url
     * @param rawURL the base url
     * @param args an array of arguments, each a name, value pair
     * @return the new URL
     */
    private static String appendArgs( String rawURL, ArrayList args )
    {
        StringBuilder sb = new StringBuilder( rawURL );
        for ( int i=0;i<args.size();i++ )
        {
            JSONDocument arg = (JSONDocument)args.get(i);
            String name = (String)arg.get(JSONKeys.NAME);
            String value = (String)arg.get(JSONKeys.VALUE);
            if ( i == 0 )
                sb.append("?");
            else
                sb.append("&");
            sb.append( name );
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }
    /**
     * Read a general LINK 
     * @param json the link in JSON format
     * @return the data as a byte array. caller should know the format
     */
    private static byte[] readLinkData( JSONDocument json ) throws LoginException
    {
        try
        {
            //long start = System.currentTimeMillis();
            String rawURL = (String)json.get(JSONKeys.URL);
            URL url = new URL( rawURL );
            JSONDocument login = (JSONDocument)json.get(JSONKeys.LOGIN);
            if ( login != null )
            {
                String user = (String)login.get(JSONKeys.USER);
                String password = (String)login.get(JSONKeys.PASSWORD);
                Login  l = null;
                String cookie = null;
                String host = null;
                LoginType lt = LoginType.valueOf((String)login.get(JSONKeys.TYPE));
                l = LoginFactory.createLogin(lt);
                host = url.getHost();
                cookie = CookieJar.cookies.getCookie( user, host );
                if ( cookie == null )
                {
                    cookie = l.login(host, user, password);
                    if ( cookie != null )
                        CookieJar.cookies.setCookie( user, host, cookie );
                }
                //long loginTime = System.currentTimeMillis();
                //System.out.println("login time="+(loginTime-start)+" milliseconds");
                ArrayList args = (ArrayList)json.get( JSONKeys.ARGS );
                if ( args != null )
                    rawURL = appendArgs( rawURL, args );
                URL newUrl = new URL( rawURL );
                HttpURLConnection conn = (HttpURLConnection) newUrl.openConnection();
                conn.setRequestProperty("Cookie", cookie);
                if ( conn.getResponseCode()> 299 )
                {
                    cookie = l.login(host, user, password);
                    if ( cookie != null )
                        CookieJar.cookies.setCookie( user, host, cookie );
                    conn = (HttpURLConnection) newUrl.openConnection();
                    if ( conn.getResponseCode()>299 )
                        throw new Exception( "failed to login "
                            +user+" to "+host);
                }
                InputStream is = conn.getInputStream();
                byte[] bdata = Utils.readStream( is );
                //long end = System.currentTimeMillis();
                //System.out.println("time taken="+(end-start)+" milliseconds");
                return bdata;
            }
            else
                return null;
        }
        catch ( Exception e )
        {
            if ( e instanceof LoginException )
                throw (LoginException)e;
            else
                throw new LoginException( e );
        }
    }
    /**
     * Read a link document
     * @param doc the JSON document representing the link
     * @param cfmt the document content's format
     * @return the document's content in bytes
     * @throws AeseException 
     */
    public static byte[] readLink( JSONDocument doc, String cfmt ) 
        throws AeseException
    {
        byte[] data = null;
        String url = (String)doc.get( JSONKeys.URL );
        if ( url == null )
            throw new AeseException("empty URL");
        try
        {
            data = readLinkData( doc );
            if ( data == null )
                throw new Exception("no data!");
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
        return data;
    }
    /**
     * Make a link document out of some parameters
     * @return 
     */
    public static JSONDocument makeLink( String url, String user, String password, 
        LoginType lt, String[] names, String[] values )
    {
        JSONDocument link = new JSONDocument();
        link.put( JSONKeys.URL, url );
        JSONDocument login = new JSONDocument();
        login.put( JSONKeys.USER, user );
        login.put( JSONKeys.PASSWORD, password );
        login.put( JSONKeys.TYPE, lt.toString() );
        link.put( JSONKeys.LOGIN, login );
        JSONDocument args=new JSONDocument();
        ArrayList<JSONDocument> list = new ArrayList<JSONDocument>();
        for ( int i=0;i<names.length;i++ )
        {
            JSONDocument kvPair = new JSONDocument();
            kvPair.put( JSONKeys.NAME, names[i] );
            kvPair.put( JSONKeys.VALUE, values[i] );
            list.add( kvPair );
        }
        link.put( JSONKeys.ARGS, list );
        return link;
    }
}