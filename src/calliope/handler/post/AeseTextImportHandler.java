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

import calliope.exception.AeseException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle importation of a set of plain text files form a tool like mmpupload.
 * @author desmond 23-7-2012
 */
public class AeseTextImportHandler extends AesePostHandler 
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
         try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                StringBuilder sb = new StringBuilder();
                // Check that we have a file upload request
                // Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                String log = "";
                sb.append("<html><body>");
                // Parse the request
                List items = upload.parseRequest( request );
                for ( int i=0;i<items.size();i++ )
                {
                    FileItem item = (FileItem) items.get( i );
                    if ( item.isFormField() )
                    {
                        String fieldName = item.getFieldName();
                        if ( fieldName != null )
                        {
                            sb.append("<p>form field: " );
                            sb.append( fieldName );
                            sb.append( "</p>");
                        }
                    }
                    else if ( item.getName().length()>0 )
                    {
                        String fieldName = item.getFieldName();
                        if ( fieldName != null )
                        {
                            sb.append("<p>file field: " );
                            sb.append( fieldName );
                            sb.append( "</p>" );
                        }
                    }
                }
                sb.append("</body></html>");
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println( sb.toString() );
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
