/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.tests;

import calliope.Service;
import calliope.constants.HTMLNames;
import calliope.constants.Globals;
import calliope.exception.AeseException;
import calliope.tests.html.Element;
import calliope.tests.html.HTML;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Text Image Linking Tool
 * @author desmond
 */
public class TestTilt extends Test
{
    String host="localhost";
    static String TILT_CSS = "div.centre { margin-left: auto; marg"
        +"in-right: auto; text-align: left }";
    static String TILT_JS = 
    "$(function()\n{\n\tvar applet = document.getElementById(\"TIL"
    +"T\");\n\tvar width = $(window).width() - $('#TILT').parent()"
    +".offset().left;\n\tvar height = $(window).height() - $('#TIL"
    +"T').parent().offset().top;\n\tapplet.setSize( width, height "
    +");\n\twidth = applet.getWidth();\n\theight = applet.getHeigh"
    +"t();\n\tapplet.width = width;\n\tapplet.height = height;\n})"
    +";";
    public TestTilt()
    {
        description = "Text Image Linking Tool";
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
        host = request.getServerName();
        doc = new HTML();
        Element script = new Element(HTMLNames.SCRIPT );
        script.addAttribute(HTMLNames.SRC, 
            "http://"+Globals.JQUERY_SITE+"/jquery-latest.js" );
        script.addAttribute( HTMLNames.TYPE, "text/javascript" );
        doc.getHeader().addChild( script );
        doc.getHeader().addScript( TILT_JS );
        doc.getHeader().addCSS( TILT_CSS );
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
        Element form = formElement( Service.PREFIX+"/tests/tilt" );
        form.addChild( docIDHidden(docID) );
        Element outer = new Element( "div" );
        outer.addAttribute( HTMLNames.ID, "centre" );
        Element noScript = new Element(HTMLNames.NOSCRIPT);
        outer.addChild( noScript );
        noScript.addText("A browser with JavaScript enabled is required for "
            +"this page to operate properly.");
        Element script1 = new Element(HTMLNames.SCRIPT);
        script1.addAttribute(HTMLNames.SRC,
            "http://www.java.com/js/deployJava.js");
        outer.addChild( script1 );
        Element script2 = new Element(HTMLNames.SCRIPT);
        script2.addText("var attributes = {code:'tilt.applet.TILTApplet',\n"
            +"archive:'TILT.jar',"
            +"id: 'TILT',"
            +"codebase:'http://"+host+Service.PREFIX+"/',"
            +"width:810, height:640} ;\n"
            +"var parameters = {"+"host: '"+host+"',mayscript:'true'};\n"
            +"deployJava.runApplet(attributes, parameters, '1.6');");
        outer.addChild( script2 );
        form.addChild( outer );
        return form;
    }
}
