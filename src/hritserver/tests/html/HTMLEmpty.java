/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;

/**
 * An empty element like br
 * @author desmond
 */
public class HTMLEmpty extends Element
{
    public HTMLEmpty( String name )
    {
        super( name );
    }
    /**
     * Convert this empty element to a String
     * @return a String
     */
    @Override
    public String toString()
    {
        return "<"+name+">";
    }
}
