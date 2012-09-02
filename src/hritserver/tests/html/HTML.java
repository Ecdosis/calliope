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
import java.util.Stack;
import hritserver.constants.HTMLNames;
/**
 * Represent a HTML document
 * @author desmond
 */
public class HTML extends Element
{
    Header head;
    Element body;
    public HTML()
    {
        super("html");
        this.body = new Element( "body" );
        this.head = new Header();
        addChild( this.head );
        addChild( this.body );
    }
    /**
     * Add a simple form wrapper around the body's content
     * @param name the name and ID of the form
     */
    public void addForm( String name )
    {
        Element f = body.getElementByTagName(HTMLNames.FORM);
        // already got a form?
        if ( f != null )
            f.addAttribute( HTMLNames.NAME,name);
        else
        {
            Element form = new Element( HTMLNames.FORM );
            form.addAttribute(HTMLNames.METHOD,HTMLNames.POST);
            form.addAttribute(HTMLNames.NAME,name);
            form.addAttribute(HTMLNames.ID,name);
            for ( int i=0;i<body.children.size();i++ )
                form.addChild( body.children.get(i) );
            body.children.clear();
            body.addChild( form );
        }
    }        
    /**
     * Add an element to the body
     * @param elem the element to add
     */
    public void add( Element elem )
    {
        body.addChild( elem );
    }
    /**
     * Get the header object
     * @return a Header
     */
    public Header getHeader()
    {
        return head;
    }
    /**
     * Convert an external string of HTML to a single HTML element. No XML. 
     * HTML need not be a complete document. (This is incomplete because we 
     * can just use HTMLLiteral, but in case it's later needed I left it in. 
     * It can otherwise be deleted.)
     * @param html a HTML string
     * @return a single Element containing the text (if more than 1 
     * element return first only)
     */
    public Element internalise( String html )
    {
        char[] chars = html.toCharArray();
        int state = 0;
        Element root = null;
        Element current = null;
        StringBuilder attrKey = new StringBuilder();
        StringBuilder attrValue = new StringBuilder();
        StringBuilder elemName = new StringBuilder();
        Stack<Element> stack = new Stack<Element>();
        for ( int i=0;i<chars.length&&state>=0;i++ )
        {
            switch ( state )
            {
                case 0: // looking for start tag
                    if ( chars[i] == '<' )
                        state = 1;
                    break;
                case 1: // looking at first non-space char after '<'
                    if ( !Character.isWhitespace(chars[i]) )
                    {
                        if ( chars[i] == '/' )
                            state = 6;
                        else
                            elemName.append( chars[i] );
                            state = 2;
                    }
                    break;
                case 2: // build up start-element name
                    if ( Character.isWhitespace(chars[i]) )
                    {
                        if ( elemName.length() > 0 )
                        {
                            Element newElem = new Element( name.trim() );
                            if ( root == null )
                                root = current = newElem;
                            else
                                current = newElem;
                            stack.push( newElem );
                            state = 3;
                            elemName.setLength( 0 );
                        }
                        // else ignore
                    }
                    else
                        elemName.append( chars[i] );
                    break;
                case 3: // read an attribute key
                    if ( !Character.isWhitespace(chars[i]) )
                    {
                        if ( chars[i] == '=' )
                            state = 4;
                        else if ( chars[i] == '>' )
                            state = 5;
                        else
                            attrKey.append( chars[i] );
                    }
                    break;
                case 4:
                    if ( !Character.isWhitespace(chars[i]) )
                    {
                        if ( chars[i] != '"' )
                            attrValue.append(chars[i] );
                    }
                    else if ( attrValue.length() > 0 )
                    {
                        current.addAttribute( attrKey.toString(),
                            attrValue.toString() );
                        attrKey.setLength(0);
                        attrValue.setLength(0);
                        state = 3;
                    }
                    // else we ignore leading spaces
                    break;
                case 5: // reading content
                    
                    break;
                case 6: // building up end-element name
                    if ( Character.isWhitespace(chars[i]) )
                    {
                        if ( elemName.length() > 0 )
                        {
                            Element elem = stack.pop();
                            if ( elem == root )
                                state = -1;
                            state = 3;
                            elemName.setLength( 0 );
                        }
                        // else ignore
                    }
                    else
                        elemName.append( chars[i] );
                    break;
            }
        }
        return root;
    }
}
