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
