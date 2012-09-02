/*
 * Handle requests for importing a set of plain text files
 */
package hritserver.handler.post;

import hritserver.exception.HritException;
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
public class HritTextImportHandler extends HritPostHandler 
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
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
                            sb.append("<p>form field: "+fieldName+"</p>");
                        }
                    }
                    else if ( item.getName().length()>0 )
                    {
                        String fieldName = item.getFieldName();
                        if ( fieldName != null )
                        {
                            sb.append("<p>file field: "+fieldName+"</p>");
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
            throw new HritException( e );
        }
    }
}
