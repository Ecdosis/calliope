/*
 * Handle a raw upload request for any kind of file. No special processing 
 * other than to replace an existing document in the database.
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
 