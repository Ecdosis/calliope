/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.importer.filters;
import hritserver.importer.Archive;
import hritserver.exception.ImportException;
import hritserver.exception.HritException;
import hritserver.json.JSONDocument;

/**
 * Specify how filters interact with the outside world
 * @author desmond
 */
public abstract class Filter 
{
    public Filter()
    {
    }
    /**
     * Get the raw name of this filter e.g. "play"
     * @return the filter name
     * @throws HritException 
     */
    public String getName() throws HritException
    {
        String className = this.getClass().getSimpleName();
        int pos = className.indexOf("Filter");
        if ( pos != -1 )
            return className.substring(0,pos);
        else
            throw new HritException("invalid class name: "+className);
    }
    public abstract void configure( JSONDocument config );
    /**
     * Short description of this filter
     * @return a string
     */
    public abstract String getDescription();
    /**
     * Subclasses should override this
     * @param input the input text for conversion
     * @param cortex the cortex archive to save split text in
     * @param corcode the corcode archive to save split markup in
     * @return the log output
     */
    public abstract String convert( String input, Archive cortex, 
        Archive corcode ) throws ImportException;
}
