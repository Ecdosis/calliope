/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
