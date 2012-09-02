/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
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
package hritserver.handler.post;
import hritserver.handler.HritHandler;
import hritserver.path.Path;
import hritserver.exception.*;
import hritserver.constants.Services;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.tests.Test;


    
/**
 * Handle a POST request
 * @author desmond
 */
public class HritPostHandler extends HritHandler
{
public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        String prefix = Path.first( urn );
        if ( prefix != null )
        {
            if ( prefix.equals(Services.STRIP) )
                new HritStripHandler().handle(request,response,urn);
            else if ( prefix.equals(Services.TESTS) )
            {
                try
                {
                    String second = Path.second( urn );
                    if ( second == null || second.length()==0 )
                        second = "Home";
                    second = Character.toUpperCase(second.charAt(0))
                        +second.substring(1);
                    String className = "hritserver.tests.Test"+second;
                    Class tClass = Class.forName( className );
                    Test t = (Test)tClass.newInstance();
                    t.handle( request, response, urn );
                }
                catch ( Exception e )
                {
                    throw new HritException( e );
                }
            }
            else if ( prefix.equals(Services.IMPORT) )
            {
                String second = Path.second( urn );
                if ( second.length() > 0 )
                {
                    if ( second.equals(Services.MVD) )
                        new HritMVDImportHandler().handle(request,response,urn);
                    else if ( second.equals(Services.XML) )
                        new HritXMLImportHandler().handle(request,response,urn);
                    else if ( second.equals(Services.TEXT) )
                        new HritTextImportHandler().handle(request,response,urn);
                    else
                        throw new HritException("Unknown service "+second);
                }
                else
                    new HritMixedImportHandler().handle(request,response,urn);
            }
            else if ( prefix.equals(Services.UPLOAD) )
                new HritUploadHandler().handle(request,response,urn);
        }
        else
            throw new PathException("Invalid urn "+urn );
    }
}
