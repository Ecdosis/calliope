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
import calliope.Connector;
import calliope.constants.Database;
import calliope.exception.AeseException;
import javax.servlet.ServletOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;

/**
 * Fetch an image from the corpix collection
 * @author desmond
 */
public class AeseGetCorPixHandler extends AeseGetHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws AeseException
    {
        try
        {
            byte[] data = Connector.getConnection().getImageFromDb( 
                Database.CORPIX, urn );
            if ( data != null)
            {
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                String mimeType = URLConnection.guessContentTypeFromStream(bis);
                response.setContentType(mimeType);
                ServletOutputStream sos = response.getOutputStream();
                sos.write( data );
                sos.close();
            }
            else
            {
                response.getOutputStream().println("image "+urn+" not found");
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
