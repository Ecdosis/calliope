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
import calliope.AeseStripper;
import calliope.constants.Formats;
import calliope.constants.JSONKeys;
import calliope.constants.MIMETypes;
import calliope.handler.AeseHandler;
import calliope.path.Path;
import calliope.json.JSONResponse;
import calliope.json.JSONDocument;
import calliope.exception.*;
import calliope.constants.Params;
import calliope.constants.Commands;
import calliope.mime.Multipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
/**
 * Handle a STRIP request
 * @author desmond
 */
public class AeseStripHandler extends AeseHandler
{
    /**
     * Strip an xml document into markup and plain text
     * @param request the original request object
     * @return a MIM multipart encoded response
     * @throws AeseException 
     */
    String stripDocument( HttpServletRequest request ) throws AeseException
    {
        try
        {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items = upload.parseRequest( request );
            Iterator iter = items.iterator();
            String format = Formats.STIL;
            String style = Formats.DEFAULT;
            String recipe = null;
            String xml = null;
            while (iter.hasNext()) 
            {
                FileItem item = (FileItem) iter.next();
                String name = item.getFieldName();
                if ( name.equals(Params.STYLE) )
                    style = item.getString();
                else if ( name.equals(Params.FORMAT) )
                    format = item.getString();
                else if ( name.equals(Params.RECIPE) )
                    recipe = item.getString();
                else if ( name.equals(Params.XML) )
                    xml = item.getString();
            }
            if ( xml != null )
            {
                JSONResponse markup = new JSONResponse();
                JSONResponse text = new JSONResponse();
                AeseStripper stripper = new AeseStripper();
                int res = stripper.strip(xml,recipe,format, style, text, 
                    markup );
                if ( res == 1 )
                {
                    Multipart mime = new Multipart();
                    mime.putTextParam( format, markup.getBody(), 
                        MIMETypes.JSON );
                    mime.putTextParam( Formats.TEXT, text.getBody(),
                        MIMETypes.TEXT );
                    return mime.toString();
                }
            }
            throw new ParamException("invalid parameters for strip");
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * List available formats by calling the native AeseStripper library
     * @return a JSONDocument as a String
     */
    private String listFormats()
    {
        AeseStripper stripper = new AeseStripper();
        String[] formats = stripper.formats();
        JSONDocument jDoc = new JSONDocument();
        ArrayList<Object> array = new ArrayList<Object>();
        for ( int i=0;i<formats.length;i++ )
            array.add( formats[i] );
        jDoc.put( JSONKeys.FORMATS, array );
        return jDoc.toString();
    }
    /**
     * Handle a general strip request
     * @param request the raw request with parameters
     * @param urn the urn of the request
     * @return a String representing the response
     * @throws AeseException if there was an error
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        Path servicePath = new Path( urn );
        String service = servicePath.getResourcePath(false);
        try
        {
            if ( service != null && service.equals(Commands.LIST) )
            {
                response.setContentType("application/json");
                response.getWriter().println(listFormats());
            }
            else
            {
                response.setContentType(MIMETypes.MULTIPART);
                response.getWriter().println( stripDocument(request) );
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
     }
}