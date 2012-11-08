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
package hritserver.handler.get;
import hritserver.handler.get.compare.HritComparisonHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.HritFormatter;
import hritserver.json.JSONResponse;
import hritserver.Utils;
import hritserver.constants.*;
import hritserver.exception.*;
import hritserver.path.*;
import hritserver.handler.HritVersion;
import hritserver.tests.html.HTMLComment;
import java.util.Map;
import java.util.HashSet;
/**
 *
 * @author desmond
 */
public class HritHTMLHandler extends HritGetHandler
{
    /**
     * Get the HTML for the given path
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        String second = Path.second(urn);
        if ( second.equals(Services.COMPARISON) )
        {
            int pos = urn.indexOf( Services.HTML );
            if ( pos == -1 )
                throw new HritException("invalid urn: "+urn );
            String rest = urn.substring( pos+Services.HTML.length() );
            new HritComparisonHandler().handle(request,response,rest);
        }
        else if ( second.equals(Services.LIST))
        {
            int pos = urn.indexOf(Services.HTML );
            if ( pos == -1 )
                throw new HritException("invalid urn: "+urn );
            String rest = urn.substring( pos+Services.HTML.length() );
            new HritListHandler().handle(request,response,rest);
        }
        else if ( second.equals(Services.TABLE) )
        {
            int pos = urn.indexOf(Services.HTML );
            if ( pos == -1 )
                throw new HritException("invalid urn: "+urn );
            String rest = urn.substring( pos+Services.HTML.length() );
            new HritTableHandler().handle(request,response,rest);
        }
        else
            handleGetVersion( request, response, urn );
    }
    /**
     * Format the requested URN version as HTML
     * @param request the original http request
     * @param urn the original request urn
     * @return the converted HTML
     * @throws HritException 
     */
    protected void handleGetVersion( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws HritException
    {
        Path path = new Path( urn );
        String version1 = request.getParameter( Params.VERSION1 );
        //System.out.println("version1="+version1);
        path.setName( Database.CORTEX );
        HritVersion corTex = doGetMVDVersion( path, version1 );
        // 1. get corcodes and styles
        Map map = request.getParameterMap();
        String[] corCodes = getEnumeratedParams( Params.CORCODE, map, true );
        String[] styles = getEnumeratedParams( Params.STYLE, map, false );
        path.setName( Database.CORCODE );
        String[] formats = new String[corCodes.length];
        HashSet<String> styleSet = new HashSet<String>();
        for ( int i=0;i<styles.length;i++ )
            styleSet.add( styles[i] );
        try
        {
            String resource = path.getResource();
            for ( int i=0;i<corCodes.length;i++ )
            {
                String ccResource = Utils.canonisePath(resource,corCodes[i]);
                Path ccPath = new Path( ccResource );
                HritVersion hv = doGetMVDVersion( ccPath, version1 );
                styleSet.add( hv.getStyle() );
                corCodes[i] = new String(hv.getVersion(),"UTF-8");
                formats[i] = hv.getFormat();
            }
        }
        catch ( Exception e )
        {
            // this won't ever happen because UTF-8 is always supported
            throw new HritException( e );
        }
        // 2. recompute styles array (docids)
        styles = new String[styleSet.size()];
        styleSet.toArray( styles );
        // 3. convert style names to actual corforms
        styles = fetchStyles( styles );
        // 4. call the native library
        JSONResponse html = new JSONResponse();
        byte[] text = corTex.getVersion();
//        // debug
//        try{
//            String textString = new String(text,"UTF-8");
//            System.out.println(textString);
//        }catch(Exception e){}
        // end
        int res = new HritFormatter().format( 
            text, corCodes, styles, formats, html );
        if ( res == 0 )
            throw new NativeException("formatting failed");
        else
        {
            response.setContentType("text/html;charset=UTF-8");
            try
            {
                HTMLComment comment = new HTMLComment();
                for ( int i=0;i<styles.length;i++ )
                    comment.addText( styles[i] );
                response.getWriter().println( comment.toString() );
                response.getWriter().println(html.getBody());   
            }
            catch ( Exception e )
            {
                throw new HritException( e );
            }
        }
    }
}
