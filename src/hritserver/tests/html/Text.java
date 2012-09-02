/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;

/**
 * Simple class to represent a Text node
 * @author desmond
 */
public class Text extends Element
{
    String content;
    public Text()
    {
        // empty constructor
    }
    public Text( String content )
    {
        this.content = content;
    }
    /**
     * Express ourselves as a String for outputting
     * @return a String
     */
    @Override
    public String toString()
    {
        return content;
    }
}
