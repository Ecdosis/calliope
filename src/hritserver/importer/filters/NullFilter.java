/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.importer.filters;
import hritserver.exception.HritException;
import hritserver.exception.ImportException;
import hritserver.json.JSONDocument;
import hritserver.importer.Archive;
/**
 * An empty filter that does nothing
 * @author desmond
 */
public class NullFilter extends Filter
{
    public NullFilter()
    {
    }
    /**
     * Get the raw name of this filter e.g. "play"
     * @return the filter name
     * @throws HritException 
     */
    public String getName() throws HritException
    {
        return "Null";
    }
    public void configure( JSONDocument config )
    {
    }
    public String getDescription()
    {
        return "An empty filter that does nothing";
    }
    /**
     * Subclasses should override this
     * @param input the input text for conversion
     * @param cortex the cortex archive to save split text in
     * @param corcode the corcode archive to save split markup in
     * @return the log output
     */
    public String convert( String input, Archive cortex, 
        Archive corcode ) throws ImportException
    {
        return "";
    }
}
