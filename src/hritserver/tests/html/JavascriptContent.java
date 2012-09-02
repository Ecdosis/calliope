/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;
import java.util.ArrayList;
/**
 * Represent the content of a script element flexibly
 * @author desmond
 */
public class JavascriptContent extends Text
{
    ArrayList<String> scripts;
    public void add( String script)
    {
        if ( scripts == null )
            scripts = new ArrayList<String>();
        scripts.add( script );
    }
    /**
     * Convert the content to a series of Javascript functions
     * @return a String
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<scripts.size();i++ )
        {
            sb.append( scripts.get(i) );
            //sb.append( "\n" );
        }
        return sb.toString();
    }
}
