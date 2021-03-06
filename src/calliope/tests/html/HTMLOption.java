/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.tests.html;
import calliope.constants.HTMLNames;

/**
 * Represent an HTML option element
 * @author desmond
 */
public class HTMLOption extends Element
{
    public HTMLOption( String value, String text )
    {
        super(HTMLNames.OPTION );
        this.addAttribute( HTMLNames.VALUE, value );
        this.addText( text );
    }
}
