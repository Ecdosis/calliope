/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get;

import calliope.constants.Database;
import calliope.constants.Params;
import calliope.constants.Formats;
import calliope.exception.AeseException;
import calliope.handler.AeseVersion;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get a file in the misc collection. No versions, but maybe links.
 * @author desmond
 */
public class AeseGetMiscHandler extends AeseGetHandler
{
    /**
     * Get a miscellaneous paratextual document, image (binary) or text
     * @param request the servlet request
     * @param response the servlet response
     * @param urn the docID, stripped of its prefix
     * @throws AeseException 
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            String docID = urn;
            AeseVersion hv = doGetResourceVersion( Database.MISC, docID, "" );
            // write binary data: could be an image
            String contentFormat = hv.getContentFormat();
            if ( contentFormat.equals(Formats.TEXT)
                ||contentFormat.equals(Formats.HTML)
                ||contentFormat.equals(Formats.JSON)
                ||contentFormat.equals(Formats.XML) )
                response.getWriter().print(hv.getVersionString());
            else
                response.getOutputStream().write(hv.getVersion()); 
        }
        catch ( IOException ioe )
        {
            throw new AeseException( ioe );
        }
    }
}
