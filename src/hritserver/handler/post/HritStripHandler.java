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
import hritserver.HritStripper;
import hritserver.constants.Formats;
import hritserver.constants.JSONKeys;
import hritserver.constants.MIMETypes;
import hritserver.handler.HritHandler;
import hritserver.path.Path;
import hritserver.json.JSONResponse;
import hritserver.json.JSONDocument;
import hritserver.exception.*;
import hritserver.constants.Params;
import hritserver.constants.Commands;
import hritserver.mime.Multipart;
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
public class HritStripHandler extends HritHandler
{
    /**
     * Strip an xml document into markup and plain text
     * @param request the original request object
     * @return a MIM multipart encoded response
     * @throws HritException 
     */
    String stripDocument( HttpServletRequest request ) throws HritException
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
                HritStripper stripper = new HritStripper();
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
            throw new HritException( e );
        }
    }
    /**
     * List available formats by calling the native HritStripper library
     * @return a JSONDocument as a String
     */
    private String listFormats()
    {
        HritStripper stripper = new HritStripper();
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
     * @throws HritException if there was an error
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
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
            throw new HritException( e );
        }
     }
}