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
package calliope.handler.get.compare;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.exception.*;
import calliope.path.*;
import calliope.constants.Params;
import calliope.constants.Database;
import calliope.handler.AeseMVD;
import java.util.Map;
import calliope.handler.get.AeseGetHandler;
/**
 * A handler for comparing all versions. Output in JSON, HTML.
 * @author desmond
 */
public class CompareAllHandler extends AeseGetHandler
{
    /**
     * Get the HTML of two versions, comparing both the corcode and the 
     * cortex for differences
     * @param request the request to read from
     * @param path the parsed URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        Path path = new Path( urn );
        Map map = request.getParameterMap();
        String[] corCodes = getEnumeratedParams( Params.CORCODE, map, true );
        String[] styles = getEnumeratedParams( Params.STYLE, map, true );
        String[] base = (String[])map.get( Params.SHORTNAME ); 
        AeseMVD text = loadMVD(Database.CORTEX, path.getResource());
        // read all the pairs, creating a merged block where all the versions agree, and split blocks for the stuff in-between
        // at the same time, for each version in the corcode, split blocks if there are further differences
        // do all this using a corcode
        // preserve transposed bits of text
        // finally, format the blocks using the corcode for each version, plus the generated corcode for blocks.
        // output the blocks as HTML
            
    }
}
