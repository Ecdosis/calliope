/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Build a GET url
 * @author desmond
 */
public class TestGetURL 
{
    StringBuilder baseURL;
    HashMap<String,String> params;
    public TestGetURL( String url )
    {
        baseURL = new StringBuilder( url );
    }
    /**
     * Add a bit at the end of the url
     * @param component a bit to add at the end 
     */
    public void append( String component )
    {
        if ( baseURL.length()>0&&component.length()!=0 )
        {
            char last = baseURL.charAt(baseURL.length()-1);
            char first = component.charAt(0);
            if ( last !='/' && first !='/')
                baseURL.append( "/" );
            else if ( last=='/'&&first=='/' )
                baseURL.append( component.substring(1) );
            else
                baseURL.append( component );
        }
        else 
            baseURL.append( component );
    }
    /**
     * Add a GET param
     * @param name the name of the param
     * @param value its value
     */
    public void addParam( String name, String value )
    {
        if ( params == null )
            params = new HashMap<String,String>();
        params.put( name, value );
    }
    /**
     * Convert to a GET url. This changes the url, so don't remake it
     * @return a String
     */
    public String toString()
    {
        Set<String> keys = params.keySet();
        boolean first = true;
        Iterator<String> iter = keys.iterator();
        StringBuilder sb = new StringBuilder(baseURL.toString());
        while( iter.hasNext() )
        {
            String name = iter.next();
            if ( first )
            {
                sb.append("?");
                first = false;
            }
            else
                sb.append("&");
            sb.append( name );
            sb.append("=");
            sb.append( params.get(name) );
        }
        return sb.toString().replace(" ","%20");
    }
}
