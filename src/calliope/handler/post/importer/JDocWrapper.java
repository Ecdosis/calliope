/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import calliope.json.JSONDocument;
import calliope.constants.JSONKeys;

/**
 * Wrap a raw file in a JSON file with key-value pairs
 * @author desmond
 */
public class JDocWrapper 
{
    String[] okKeys = {JSONKeys.FORMAT,JSONKeys.VERSION1,JSONKeys.AUTHOR,
        JSONKeys.TITLE,JSONKeys.STYLE,JSONKeys.SECTION };
    JSONDocument  jdoc;
    /**
     * Is the json document key acceptable?
     * @param key the key to test
     * @return 1 if it is
     */
    private boolean isValidKey( String key )
    {
        for ( int i=0;i<okKeys.length;i++ )
            if ( okKeys[i].equals(key) )
                return true;
        return false;
    }
    private String cleanCR( String value )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<value.length();i++ )
        {
            if ( value.charAt(i)!='\n'&&value.charAt(i)!='\r' )
                sb.append(value.charAt(i));
        }
        return sb.toString();
    }
    /**
     * Construct the wrapper
     * @param body the raw data 
     * @param params a map of key-value pairs
     */
    public JDocWrapper( String body, Map<String,String> params )
    {
        jdoc = new JSONDocument();
        jdoc.put( JSONKeys.BODY, cleanCR(body) );
        Set<String> keys = params.keySet();
        Iterator<String> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            String key = iter.next();
            String value = params.get(key);
            if ( isValidKey(key) )
            {
                jdoc.put( key, cleanCR(value) );
            }
        }
    }
    /**
     * Convert the document to a string
     * @return a string
     */
    @Override
    public String toString()
    {
        return jdoc.toString();
    }
}
