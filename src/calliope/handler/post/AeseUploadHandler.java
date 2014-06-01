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

import calliope.Connector;
import calliope.exception.AeseException;
import calliope.handler.post.importer.JDocWrapper;
import calliope.handler.post.importer.ImageFile;
import calliope.handler.post.importer.File;
import calliope.constants.Database;
import calliope.path.Path;
import calliope.Utils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
/**
 * Handler for raw files
 * @author desmond 28/7/2012
 */
public class AeseUploadHandler extends AeseImportHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            database = Path.first(urn);
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                for ( int i=0;i<images.size();i++ )
                {
                    ImageFile iFile = images.get(i);
                    Connector.getConnection().putImageToDb( 
                        database, docID.get(), 
                        iFile.getData() );
                }
                for ( int i=0;i<files.size();i++ )
                {
                    String resp = "";
                    File file = files.get(i);
                    if ( file instanceof File )
                    {
                        // wrap cortex andkill -9 8220 corcodes with kosher params
                        String json = file.data;
                        if ( database.equals(Database.CORTEX)
                        || database.equals(Database.CORCODE) 
                        || database.equals(Database.MISC) )
                        {
                            JDocWrapper wrapper = new JDocWrapper( 
                                json, jsonKeys );
                            json = wrapper.toString();
                        }
                        else if ( database.equals(Database.CORFORM)
                            || database.equals(Database.CONFIG)
                            || database.equals(Database.PARATEXT))
                            json = Utils.cleanCR( json, true );
                        resp = Connector.getConnection().putToDb( 
                            database, docID.get(), json );
                    }
                    log.append( resp );
                }
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println( wrapLog() );
            } 
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
 