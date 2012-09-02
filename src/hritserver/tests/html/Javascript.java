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
