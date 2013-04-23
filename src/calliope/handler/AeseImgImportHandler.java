/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post;

import calliope.exception.AeseException;
import calliope.handler.post.importer.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle importation of images (copy to disk)
 * @author desmond
 */
public class AeseImgImportHandler  extends AeseImportHandler
{
    /**
     * Handle posted files from the input dialog. Can be mixed TEXT etc files
     * @param request the request object
     * @param response the response object
     * @param urn the urn of the request
     * @throws AeseException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            String webRoot = System.getProperty("user.dir");
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                if ( !demo )
                {
                    for ( int i=0;i<files.size();i++ )
                    {
                        File file = files.get( i );
                        try
                        {
                            java.io.File dir = new java.io.File( webRoot, 
                                docID.toString() );
                            java.io.File imgFile = new java.io.File( dir, 
                                file.simpleName() );
                            dir.mkdirs();
                        }
                        catch ( Exception e )
                        {
                            
                        }
                    }
                }
                else
                {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println( 
                        "<p>Not enabled on public server</p>" );
                }
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
