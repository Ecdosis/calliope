/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
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
package hritserver.tests;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import hritserver.exception.*;
import hritserver.tests.html.Element;
import hritserver.tests.html.HTMLLiteral;
import hritserver.tests.html.Text;
import hritserver.tests.html.HTML;
import hritserver.constants.*;
import hritserver.json.JSONDocument;
import hritserver.HritServer;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
/**
 * Test that the conversion of CorTex+CorCode into HTML works
 * @author desmond
 */
public class TestHtml extends Test
{
    static String VERSION_POPUP_SCRIPT = 
    "function do_popup1(){document.forms.default.submit();}";
    static String CENTRE_DIV_STYLE = "div#centre { width: 600px; "
           + "margin-left: auto; margin-right: auto; "
           +"background-color: white; padding: 10px }";
    public TestHtml()
    {
        description = "Retrieves a formatted HTML file";
    }
    /**
     * Display the test GUI
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request,
        HttpServletResponse response, String urn ) throws HritException
    {
        setDocID( request );
        doc = new HTML();
        doc.getHeader().addScript( VERSION_POPUP_SCRIPT );
        doc.getHeader().addCSS( CENTRE_DIV_STYLE );
        super.handle( request, response, urn );
    }
    /**
     * Get the content of the version
     * @return an Element (div) containing the content
     */
    private Element getVersionContent()
    {
        try
        {
            URL url = new URL("http://localhost:8080/html/"
                +docIDCanonise(docID)+"/"+version1 );
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream( 
                is.available() );
            while ( is.available() != 0 )
            {
                byte[] data = new byte[is.available()];
                is.read( data );
                bos.write( data, 0, data.length );
            }
            String body = bos.toString("UTF-8");
            String css = null;
            int pos1 = body.indexOf("<!--");
            int pos2 = body.indexOf("-->");
            if ( pos1 >= 0 && pos2 > 0 && pos1 < pos2 )
            {
                // skip "<!--"
                css = body.substring( 4, pos2 );
                // header must NOT already be committed
                doc.getHeader().addCSS( css );
                body = body.substring( pos2+3 );
            }
            // it's always safe to return the body as is
            return new HTMLLiteral( body );
        }
        catch ( Exception e )
        {
            return new Text( "Failed CorCode+CorTex to HTML test: "
                +e.getMessage());
        }
    }
    /**
     * Get the version dropdown menu in its own div
     * @return an Element containing the dropdown menu
     */
    private Element getVersionSelector()
    {
        try
        {
            TestGetURL rawUrl = new TestGetURL(
                "http://localhost:8080/html/list/");
            rawUrl.append( docID );
            rawUrl.addParam( Params.NAME, Params.VERSION1 );
            rawUrl.addParam( Params.VERSION1, version1 );
            rawUrl.addParam( Params.FUNCTION, "do_popup1()" );
            URL url = new URL( rawUrl.toString() );
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(
                is.available());
            while ( is.available() != 0 )
            {
                byte[] data = new byte[is.available()];
                is.read( data );
                bos.write( data, 0, data.length );
            }
            Element form = new Element(HTMLNames.FORM);
            form.addAttribute(HTMLNames.ACTION, "/tests/Html");
            form.addAttribute(HTMLNames.METHOD, HTMLNames.POST );
            form.addAttribute(HTMLNames.NAME, HTMLNames.DEFAULT );
            Element content = new HTMLLiteral( bos.toString() );
            form.addChild( content );
            if ( version1 != null )
                rememberParam( form, Params.VERSION1, version1 );
            return form;
        }
        catch ( Exception e )
        {
            return new Text( "Failed List (HTML) test: "
                +e.getMessage());
        }
    }
    /**
     * Get the content of this test: a simple dropdown menu plus the 
     * html version's content
     * @return a select element object with appropriate attributes and children
     */
    @Override
    public Element getContent()
    {
        Element content = getVersionContent();
        Element selector = getVersionSelector();
        Element outer = new Element( "div" );
        outer.addAttribute( HTMLNames.ID, "centre" );
        outer.addChild( selector );
        outer.addChild( content );
        return outer;
    }
}