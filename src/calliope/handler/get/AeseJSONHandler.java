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
package calliope.handler.get;
import calliope.handler.get.commands.AeseJSONTimelineHandler;
import calliope.handler.get.commands.AeseJSONTimelineNewHandler;
import calliope.handler.get.commands.AeseJSONBiographyHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.constants.*;
import calliope.exception.*;
import calliope.path.*;
/**
 * Handle requests for JSON formatted data
 * @author desmond
 */
public class AeseJSONHandler extends AeseGetHandler
{
    /**
     * Get the JSON for the given path
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        String first = Path.first(urn);
        if ( first.equals(Services.LIST))
            new AeseJSONListHandler().handle(request,response,Path.pop(urn));
        else if ( first.equals(Services.DICTS) )
            new AeseJSONDictsHandler().handle(request,response,Path.pop(urn));
        else if ( first.equals(Services.TIMELINE) )
            new AeseJSONTimelineHandler().handle(request,response,Path.pop(urn));
        else if ( first.equals(Services.TIMELINENEW) )
            new AeseJSONTimelineNewHandler().handle(request,response,Path.pop(urn));
        else if ( first.equals(Services.BIOGRAPHY) )
            new AeseJSONBiographyHandler().handle(request,response,Path.pop(urn));
        else
            throw new AeseException("Unknown service: "+urn);
    }
}
