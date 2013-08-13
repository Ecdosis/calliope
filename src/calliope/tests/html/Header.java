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
package calliope.tests.html;
import calliope.constants.HTMLNames;
import calliope.constants.MIMETypes;
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
     * Add an encoding statement
     * @param encoding 
     */
    public void addEncoding( String encoding )
    {
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
    /**
     * Add a Javascript source to the header
     * @param src a javascript source
     */
    public void addScriptSrc( String src )
    {
        addChild( new Javascript(src) );
    }
}
