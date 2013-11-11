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
package calliope.handler.post;
import calliope.handler.AeseHandler;
import calliope.path.Path;
import calliope.exception.*;
import calliope.constants.Services;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.tests.Test;

/**
 * Handle a POST request
 * @author desmond
 */
public class AesePostHandler extends AeseHandler
{
public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        String prefix = Path.first( urn );
        if ( prefix != null )
        {
            if ( prefix.equals(Services.STRIP) )
                new AeseStripHandler().handle(request,response,urn);
            else if ( prefix.equals(Services.TESTS) )
            {
                try
                {
                    String second = Path.second( urn );
                    if ( second == null || second.length()==0 )
                        second = "Home";
                    second = Character.toUpperCase(second.charAt(0))
                        +second.substring(1);
                    String className = "calliope.tests.Test"+second;
                    Class tClass = Class.forName( className );
                    Test t = (Test)tClass.newInstance();
                    t.handle( request, response, urn );
                }
                catch ( Exception e )
                {
                    throw new AeseException( e );
                }
            }
            else if ( prefix.equals(Services.IMPORT) )
            {
                String second = Path.second( urn );
                if ( second.length() > 0 )
                {
                    int pos = urn.indexOf(second);
                    urn = urn.substring(pos+second.length()+1);
                    if ( second.equals(Services.LITERAL) )
                        new AeseUploadHandler().handle(request,response,urn);
                    else if ( second.equals(Services.XML) )
                        new AeseXMLImportHandler().handle(request,response,urn);
                    else if ( second.equals(Services.HTML) )
                        new AeseHTMLImportHandler().handle(request,response,urn);
                    else if ( second.equals(Services.TEXT) )
                        new AeseTextImportHandler().handle(request,response,urn);
                    else if ( second.equals(Services.MIXED) )
                        new AeseMixedImportHandler().handle(request,response,urn);
                    else
                        throw new AeseException("Unknown service "+second);
                }
                else
                    new AeseMixedImportHandler().handle(request,response,urn);
            }
            else if ( prefix.equals(Services.UPLOAD) )
                new AeseUploadHandler().handle(request,response,urn);
        }
        else
            throw new PathException("Invalid urn "+urn );
    }
}
