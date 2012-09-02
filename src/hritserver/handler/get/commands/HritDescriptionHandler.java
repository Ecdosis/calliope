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
package hritserver.handler.get.commands;
import hritserver.handler.HritMVD;
import hritserver.exception.HritException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.path.Path;
import hritserver.handler.get.HritGetHandler;
/**
 * Handle a request to get an MVD's description
 * @author desmond
 */
public class HritDescriptionHandler extends HritGetHandler 
{
    /**
     * Get a cortex description string
     * @param urn the raw urn passed into the application
     * @return the MVD description or an error
     */
    @Override
    public void handle( HttpServletRequest request,   
        HttpServletResponse response, String urn )
        throws HritException
    {
        Path path = new Path( urn );
        HritMVD mvd = loadMVD( path.getResource(true) );
        if ( mvd != null )
        {
            try
            {
                response.setContentType(mvd.format);
                response.getWriter().println(mvd.mvd.getDescription());
            }
            catch ( Exception e )
            {
                throw new HritException( e );
            }
        }
        else
            throw new HritException( "path "+path+" not found") ;
    }

}
