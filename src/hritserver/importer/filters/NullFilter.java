/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
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
