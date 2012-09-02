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
 * A pseudo-Element containing valid unparsed HTML
 * @author desmond
 */
public class HTMLLiteral extends Element
{
    private String content;
    public HTMLLiteral()
    {
        super();
    }
    public HTMLLiteral( String content )
    {
        super();
        this.content = content;
    }
    /**
     * A HTMLLiteral is fixed an immutable
     * @param text ignored
     */
    @Override
    public void addText( String text )
    {
        content = (content== null)?text:content+text;
    }
    @Override
    public void addChild( Element child )
    {
        addText( child.toString() );
    }
    /**
     * We can't add an attribute because we are only text
     * @param attr ignored
     */
    @Override
    public void addAttribute( Attribute attr )
    {
    }
    /**
     * Just return our literal content
     * @return a String
     */
    @Override
    public String toString()
    {
        return content;
    }
}
