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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.exception.AeseException;
import calliope.constants.Services;
import calliope.handler.AeseVersion;
import calliope.constants.Params;
import calliope.path.Path;
import calliope.Utils;
import calliope.constants.Database;

/**
 * Handle a call to get a specific cortex (whole)
 * @author desmond
 */
public class AeseGetCorTexHandler extends AeseGetHandler
{
    /**
     * Get a CorTex
     * @param request the servlet request
     * @param response the servlet response
     * @param urn the docID, stripped of its prefix
     * @throws AeseException 
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws AeseException
    {
        String prefix = Path.first( urn );
        if ( prefix != null && prefix.equals(Services.VERSION2) )
            new AeseNextVersionHandler().handle(request,response,urn);
        else
        {
            try
            {
                String version1 = request.getParameter(Params.VERSION1 );
                String docID = request.getParameter(Params.DOC_ID);
                AeseVersion hv = doGetResourceVersion( Database.CORTEX, docID, 
                    version1 );
                response.getWriter().println(hv.getVersionString()); 
            }
            catch ( Exception e )
            {
                throw new AeseException( e );
            }
        }
    }
}
