/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests;

import hritserver.constants.HTMLNames;
import hritserver.constants.Globals;
import hritserver.constants.Params;
import hritserver.exception.HritException;
import hritserver.tests.html.Element;
import hritserver.tests.html.HTML;
import hritserver.tests.html.HTMLLiteral;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author desmond
 */
public class TestImage extends Test
{
    public TestImage()
    {
        description = "Parallel facsimile/formated text view";
        version1 = "/introduction/add0";
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
        Element script1 = new Element("script");
        script1.addAttribute(HTMLNames.TYPE, "text/javascript");
        script1.addAttribute( "src", 
            "http://"+Globals.JQUERY_SITE+"/jquery-latest.js");
        Element script2 = new Element("script");
        script2.addAttribute(HTMLNames.TYPE, "text/javascript");
        script2.addText( TestImageStrings.SCALE_IMAGE_SCRIPT );
        Element script3 = new Element("script");
        script3.addAttribute(HTMLNames.TYPE, "text/javascript");
        script3.addText( TestImageStrings.MAPHILITE );
        Element script4 = new Element("script");
        script4.addAttribute(HTMLNames.TYPE, "text/javascript");
        script4.addText( TestImageStrings.IMAGE_JS );
        Element script5 = new Element("script");
        script5.addAttribute(HTMLNames.TYPE, "text/javascript");
        script5.addText( TestImageStrings.MODERNIZR );
        doc.getHeader().addChild( script1 );
        doc.getHeader().addChild( script2 );
        doc.getHeader().addChild( script3 );
        doc.getHeader().addChild( script4 );
        doc.getHeader().addChild( script5 );
        doc.getHeader().addCSS( TestImageStrings.IMAGE_CSS );
        // get parameter version1 and use it to select the 
        // version of the imagemap and the version of the text
        String v1param = request.getParameter( Params.VERSION1 );
        if ( v1param != null )
            version1 = v1param;
        super.handle( request, response, urn );
    }
    /**
     * Get the content of this test: an imagemap on the left and HTML on the right
     * @return a div containing the whole thing
     */
    @Override
    public Element getContent()
    {
        Element form = formElement( "/tests/image" );
        form.addChild( docIDHidden(docID) );
        Element divCentre = new Element( HTMLNames.DIV );
        form.addChild( divCentre );
        divCentre.addAttribute( HTMLNames.ID, "twinCentreColumn" );
        Element divLeft = new Element( HTMLNames.DIV );
        Element divRight = new Element( HTMLNames.DIV );
        divLeft.addAttribute( HTMLNames.ID, "leftColumn" );
        divRight.addAttribute( HTMLNames.ID, "rightColumn" );
        if ( version1.equals("/introduction/add0") )
            divLeft.addChild( new HTMLLiteral(TestImageStrings.IMAGEMAP_A) );
        else
            divLeft.addChild( new HTMLLiteral(TestImageStrings.IMAGEMAP_D) );
        divLeft.addChild( new HTMLLiteral(TestImageStrings.BUTTONS));
        divRight.addChild( new HTMLLiteral("<span class=\"description\">"
            +"aristofanunculos by capuana</span> ") );
        if ( version1.equals("/introduction/add0") )
        {
            divRight.addChild( new HTMLLiteral(TestImageStrings.SELECT_A) );
            divRight.addChild( new HTMLLiteral(TestImageStrings.f1a) );
        }
        else
        {
            divRight.addChild( new HTMLLiteral(TestImageStrings.SELECT_D) );
            divRight.addChild( new HTMLLiteral(TestImageStrings.f1d) );
        }
        divCentre.addChild( divLeft );
        divCentre.addChild( divRight );
        return form;
    }
}
