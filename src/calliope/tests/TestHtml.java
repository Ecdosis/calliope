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
package calliope.tests;
import calliope.Service;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import calliope.exception.*;
import calliope.tests.html.Element;
import calliope.tests.html.HTMLLiteral;
import calliope.tests.html.Text;
import calliope.tests.html.HTML;
import calliope.constants.*;
import calliope.Utils;
import calliope.URLEncoder;
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
        HttpServletResponse response, String urn ) throws AeseException
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
            String url = "http://localhost:8080"+Service.PREFIX+"/html";
            String urn = Utils.escape( docID );
            url = URLEncoder.append( url, urn );
            url = URLEncoder.addGetParam(url,Params.VERSION1,
                Utils.escape(version1));
            //url = URLEncoder.addGetParam(url,Params.SELECTED_VERSIONS,
            //    "/Base/F1,/Base/Q1,/Base/Q2" );
            String body = URLEncoder.getResponseForUrl(url).trim();
            body = extractCSSFromBody( body );
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
                "http://localhost:8080"+Service.PREFIX+"/html/list/");
            rawUrl.append( docID );
            rawUrl.addParam( Params.NAME, Params.VERSION1 );
            rawUrl.addParam( Params.VERSION1, version1 );
            rawUrl.addParam( Params.FUNCTION, "do_popup1()" );
            String html = URLEncoder.getResponseForUrl( rawUrl.toString() );
            Element form = formElement(Service.PREFIX+"/tests/html" );
            Element content = new HTMLLiteral( html );
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