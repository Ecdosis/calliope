/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.tests;


import calliope.constants.Params;
import calliope.exception.AeseException;
import calliope.tests.html.Element;
import calliope.tests.html.HTML;
import calliope.constants.HTMLNames;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test panel for boring internal tests
 * @author desmond
 */
public class TestInternal extends Test
{
    static String CENTRE_DIV_STYLE = "div#centre { width: 600px; "
           + "margin-left: auto; margin-right: auto; "
           + "background-color: white; padding: 10px }"
           + "textarea#panel { position: relative; width: 100%; height: 250px }";
    public TestInternal()
    {
        description = "Runs some tests in a console";
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
        //setDocID( request );
        doc = new HTML();
        doc.getHeader().addCSS( CENTRE_DIV_STYLE );
        super.handle( request, response, urn );
    }
    /**
     * Get the content of this test: a simple dropdown menu plus the 
     * html version's content
     * @return a select element object with appropriate attributes and children
     */
    @Override
    public Element getContent()
    {
        Element form = formElement("/tests/internal" );
        Element outer = new Element( HTMLNames.DIV );
        form.addChild( outer );
        outer.addAttribute( HTMLNames.ID, "centre" );
        Element textArea = new Element( "textarea" );
        textArea.addAttribute( HTMLNames.ID, "panel" );
        textArea.addAttribute( "readonly", "readonly" );
        try
        {
            TestGetURL basicUrl = new TestGetURL(
                "http://localhost:8080/test/basic/");
            String basic = calliope.URLEncoder.getResponseForUrl( 
                basicUrl.toString() );
            textArea.addText( basic );
        }
        catch ( Exception e )
        {
            textArea.addText( e.getMessage() );
        }   
        outer.addChild( textArea );
        return form;
    }
}
