/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;
import hritserver.constants.HTMLNames;
import hritserver.constants.MIMETypes;
/**
 *
 * @author desmond
 */
public class Header extends Element
{
    Javascript scripts;
    Element css;
    public Header()
    {
        super("head");
    }
    /**
     * Add some css to the header
     * @param the CSS content
     */
    public void addCSS( String content )
    {
        if ( css == null )
        {
            css = new Element("style");
            css.addAttribute(new Attribute(HTMLNames.TYPE,MIMETypes.CSS));
            addChild( css );
        }
        Text child = new Text( content );
        css.addChild( child );
    }
    /**
     * Add a Javascript script to the header
     * @param script a javascript function
     */
    public void addScript( String script )
    {
        if ( scripts == null )
        {
            scripts = new Javascript();
            addChild( scripts );
        }
        scripts.add( script );
    }
}
