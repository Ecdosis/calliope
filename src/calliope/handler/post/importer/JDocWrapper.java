/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import calliope.Utils;
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
    /**
     * Construct the wrapper
     * @param body the raw data 
     * @param params a map of key-value pairs
     */
    public JDocWrapper( String body, Map<String,String> params )
    {
        jdoc = new JSONDocument();
        jdoc.put( JSONKeys.BODY, Utils.cleanCR(body,false) );
        Set<String> keys = params.keySet();
        Iterator<String> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            String key = iter.next();
            String value = params.get(key);
            if ( isValidKey(key) )
            {
                jdoc.put( key, Utils.cleanCR(value,false) );
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
