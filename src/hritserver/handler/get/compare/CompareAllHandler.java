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
package hritserver.handler.get.compare;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.exception.*;
import hritserver.path.*;
import hritserver.constants.Params;
import hritserver.constants.Database;
import hritserver.handler.HritMVD;
import java.util.Map;
import hritserver.handler.get.HritGetHandler;
/**
 * A handler for comparing all versions. Output in JSON, HTML.
 * @author desmond
 */
public class CompareAllHandler extends HritGetHandler
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
        HttpServletResponse response, String urn ) throws HritException
    {
        Path path = new Path( urn );
        Map map = request.getParameterMap();
        String[] corCodes = getEnumeratedParams( Params.CORCODE, map, true );
        String[] styles = getEnumeratedParams( Params.STYLE, map, true );
        String[] base = (String[])map.get( Params.SHORTNAME ); 
        path.setName( Database.CORTEX );
        HritMVD text = loadMVD(path.getResource());
        // read all the pairs, creating a merged block where all the versions agree, and split blocks for the stuff in-between
        // at the same time, for each version in the corcode, split blocks if there are further differences
        // do all this using a corcode
        // preserve transposed bits of text
        // finally, format the blocks using the corcode for each version, plus the generated corcode for blocks.
        // output the blocks as HTML
            
    }
}
