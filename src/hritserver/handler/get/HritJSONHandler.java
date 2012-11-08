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
package hritserver.handler.get;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.constants.*;
import hritserver.exception.*;
import hritserver.path.*;
/**
 * Handle requests for JSON formatted data
 * @author desmond
 */
public class HritJSONHandler extends HritGetHandler
{
    /**
     * Get the JSON for the given path
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        String second = Path.second(urn);
        if ( second.equals(Services.LIST))
        {
            int pos = urn.indexOf(Services.JSON );
            if ( pos == -1 )
                throw new HritException("invalid urn: "+urn );
            String rest = urn.substring( pos+Services.HTML.length() );
            new HritJSONListHandler().handle(request,response,rest);
        }
        else
            throw new HritException("Unknown service: "+urn);
    }
    
}
