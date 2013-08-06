/* This file is part of calliope.
 *
 *  calliope is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  calliope is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
 */

package calliope.importer.filters;
import calliope.exception.AeseException;
import calliope.exception.ImportException;
import calliope.json.JSONDocument;
import calliope.importer.Archive;
/**
 * An empty filter that simply merges its input into a cortex without 
 * using the corcode
 * @author desmond
 */
public class EmptyFilter extends Filter
{
    public static String EMPTY_CORCODE = "{\"style\":\"TEI\",\"ranges\":[]}";
    public EmptyFilter()
    {
        super();
    }
    /**
     * Get the raw name of this filter e.g. "play"
     * @return the filter name
     * @throws AeseException 
     */
    public String getName() throws AeseException
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
     * @param name the name of this version
     * @param cortex the cortex archive to save split text in
     * @param corcode the corcode archive to save split markup in
     * @return the log output
     */
    public String convert( String input, String name, Archive cortex, 
        Archive corcode ) throws ImportException
    {
        try
        {
            cortex.put( name,input.getBytes("UTF-8") );
            // corcode should have an entry for this version
            corcode.put( name, EMPTY_CORCODE.getBytes("UTF-8") );
            return "added "+name+" to CorTex\n";
        }
        catch ( Exception e )
        {
            // shouldn't happen
        }
        return "";
    }
}
