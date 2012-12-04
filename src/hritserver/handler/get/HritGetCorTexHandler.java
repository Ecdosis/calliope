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
import hritserver.exception.HritException;
import hritserver.constants.Services;
import hritserver.handler.HritVersion;
import hritserver.constants.Params;
import hritserver.path.Path;
import hritserver.Utils;
import hritserver.constants.Database;

/**
 * Handle a call to get a specific cortex (whole)
 * @author desmond
 */
public class HritGetCorTexHandler extends HritGetHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws HritException
    {
        String prefix = Path.first( urn );
        if ( prefix != null && prefix.equals(Services.VERSION2) )
            new HritNextVersionHandler().handle(request,response,urn);
        else
        {
            try
            {
                String version1 = request.getParameter(Params.VERSION1 );
                Path path = new Path( Utils.canonisePath(Database.CORTEX,urn) );
                HritVersion hv = doGetMVDVersion( path, version1 );
                response.getWriter().println(hv.getVersionString()); 
            }
            catch ( Exception e )
            {
                throw new HritException( e );
            }
        }
    }
}
