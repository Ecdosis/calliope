/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;

/**
 *
 * @author desmond
 */
public class Javascript extends Element
{
    JavascriptContent content;
    public Javascript()
    {
        super("script");
        addAttribute( new Attribute("type","text/javascript") );
        content = new JavascriptContent();
        addChild( content );
    }
    /**
     * Add a javascript as an external link
     * @param link the link
     */
    public Javascript( String link )
    {
        super("script");
        addAttribute( new Attribute("src",link) );
    }
    /**
     * Add a script so it gets printed out with the doc
     * @param script the script to add
     */
    public void add( String script )
    {
        content.add( script );
    }
}
