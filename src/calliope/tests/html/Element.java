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
import java.util.ArrayList;
import calliope.constants.HTMLNames;
import calliope.exception.AeseException;
/**
 *
 * @author desmond
 */
public class Element 
{
    protected String name;
    ArrayList<Attribute> attrs;
    ArrayList<Element> children;
    public Element()
    {
    }
    /**
     * Create a HTML element
     * @param name its name
     */
    public Element( String name )
    {
        this.name = name;
    }
    /**
     * Add a textual attribute
     * @param key the attibute key
     * @param value its value
     */
    public void addAttribute( String key, String value )
    {
        Attribute attr = new Attribute( key, value);
        addAttribute( attr );
    }
    /**
     * Get a named attribute
     * @param name the name to get
     * @return the attribute value or null if not found
     */
    public String getAttribute( String name )
    {
        if ( attrs != null )
        {
            for ( int i=0;i<attrs.size();i++ )
            {
                Attribute a = attrs.get(i);
                if ( a.key.equals(name) )
                    return a.value;
            }
        }
        return null;
    }
    /**
     * Add another value to the end of an existing attribute
     * @param key the name of the attribute to extend
     * @param extension the extension separated by a space
     */
    public void extendAttribute( String key, String extension )
    {
        for ( int i=0;i<attrs.size();i++ )
        {
            Attribute attr = attrs.get(i);
            if ( attr.key.equals(key) )
            {
                attr.value += " "+extension;
                break;
            }
        }
    }
    /**
     * Add a textual string to the elements
     * @param text the raw text to add as one element
     */
    public void addText( String text )
    {
        if ( children == null )
            children = new ArrayList<Element>();
        children.add( new Text(text) );
    }
    /**
     * add an ordinary child element
     * @param child the child to add
     */
    public void addChild( Element child )
    {
        if ( children == null )
            children = new ArrayList<Element>();
        children.add( child );
    }
    /**
     * Count the number of child elements including text
     * @return a count 
     */
    public int numChildren()
    {
        if ( children == null )
            return 0;
        else
            return children.size();
    }
    /**
     * get a particular child
     * @param i index of the desired child element
     * @return the child
     * @throws AeseException if the index was out of bounds
     */
    public Element getChild( int i ) throws AeseException
    {
        if ( i < children.size() )
            return children.get( i );
        else
            throw new AeseException("invalid child index "+i);
    }
    /**
     * Convert this element to a String
     * @return a String
     */
    @Override
    public String toString()
    {
        String res = "";
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        if ( attrs != null )
        {
            for ( int i=0;i<attrs.size();i++ )
                sb1.append( attrs.get(i).toString() );
        }
        if ( children != null )
        {
            for ( int i=0;i<children.size();i++ )
                sb2.append( children.get(i).toString() );
            return "<"+name+sb1.toString()+">"+sb2.toString()+"</"+name+">\n";
        }
        else
            return "<"+name+sb1.toString()+">"+sb2.toString()+"</"+name+">";
    }
    /**
     * Ask all children to build but do nothing else
     */
    public void build()
    {
        if ( children != null )
        {
            for ( int i=0;i<children.size();i++ )
                children.get(i).build();
        }
    }
    /**
     * Add an ordinary attribute
     * @param attr an Attribute object already created
     */
    public void addAttribute( Attribute attr )
    {
        if ( attrs == null )
            attrs = new ArrayList<Attribute>();
        attrs.add( attr );
    }
    /**
     * Get an element by its id attribute
     * @param id the id value to search for
     * @return the element or null
     */
    public Element getElementById( String id )
    {
        if ( attrs != null )
        {
            for ( int i=0;i<attrs.size();i++ )
            {
                Attribute a = attrs.get( i );
                if ( a.key.equals(HTMLNames.ID)&&a.value.equals(id) )
                    return this;
            }
        }
        // not here: recurse
        if ( children != null )
        {
            for ( int i=0;i<children.size();i++ )
            {
                Element child = children.get(i);
                Element e = child.getElementById(id);
                if ( e != null )
                    return e;
            }
        }
        return null;
    }
    /**
     * Get an element by its id attribute
     * @param tag the tag name to search for
     * @return the element or null
     */
    public Element getElementByTagName( String tag )
    {
        if ( this.name != null && this.name.equals(tag) )
            return this;
        else if ( children != null )
        {
            for ( int i=0;i<children.size();i++ )
            {
                Element child = children.get(i);
                Element e = child.getElementByTagName(tag);
                if ( e != null )
                    return e;
            }
        }
        return null;
    }
    /**
     * Try to set the default option that has the given value
     * @param value the default 
     */
    public boolean setDefaultValue( String value )
    {
        boolean result = false;
        for ( int i=0;i<children.size();i++ )
        {
            Element child = children.get(i);
            String cValue = child.getAttribute(HTMLNames.VALUE);
            if ( cValue !=null && cValue.equals(value) )
            {
                child.addAttribute(HTMLNames.SELECTED,HTMLNames.SELECTED);
                result = true;
            }
            else if ( child.children != null && child.children.size()>0 )
                result = child.setDefaultValue( value );
            if ( result )
                break;
        }
        return result;
    }
}
