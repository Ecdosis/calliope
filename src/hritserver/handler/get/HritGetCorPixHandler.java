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
import hritserver.HritServer;
import hritserver.exception.HritException;
import javax.servlet.ServletOutputStream;

/**
 *
 * @author desmond
 */
public class HritGetCorPixHandler extends HritGetHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws HritException
    {
        try
        {
            byte[] data = HritServer.getFromDb( urn );
            /*Either remove the setContentType(), or send the response using 
response.getOutputStream() method. That should solve the problem. */
            ServletOutputStream sos = response.getOutputStream();
            sos.write( data );
            sos.close();
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
