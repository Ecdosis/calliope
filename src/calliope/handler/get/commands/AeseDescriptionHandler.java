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
package calliope.handler.get.commands;
import calliope.constants.Database;
import calliope.handler.AeseMVD;
import calliope.exception.AeseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.path.Path;
import calliope.handler.get.AeseGetHandler;
/**
 * Handle a request to get an MVD's description
 * @author desmond
 */
public class AeseDescriptionHandler extends AeseGetHandler 
{
    /**
     * Get a cortex description string
     * @param urn the raw urn passed into the application
     * @return the MVD description or an error
     */
    @Override
    public void handle( HttpServletRequest request,   
        HttpServletResponse response, String urn )
        throws AeseException
    {
        AeseMVD mvd = loadMVD( Database.CORTEX, urn );
        if ( mvd != null )
        {
            try
            {
                response.setContentType(mvd.format);
                response.getWriter().println(mvd.mvd.getDescription());
            }
            catch ( Exception e )
            {
                throw new AeseException( e );
            }
        }
        else
            throw new AeseException( "path "+urn+" not found") ;
    }

}
