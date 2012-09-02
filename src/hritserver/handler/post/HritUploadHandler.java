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

package hritserver.handler.post;

import hritserver.HritServer;
import hritserver.exception.HritException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
/**
 * Handler for raw files
 * @author desmond 28/7/2012
 */
public class HritUploadHandler extends HritImportHandler
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                for ( int i=0;i<files.size();i++ )
                {
                    String url = "/"+database+"/"+docID.get(false);
                    String resp = HritServer.putToDb( url, files.get(i).data );
                    log.append( resp );
                }
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println( wrapLog() );
            } 
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
 