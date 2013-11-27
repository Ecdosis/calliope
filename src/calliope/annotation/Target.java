/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.annotation;

import java.util.UUID;
import calliope.exception.AnnotationException;
import java.awt.Dimension;
/**
 * The target for an annotation
 * @author desmond
 */
public class Target 
{
    String id;
    Selector sel;
    String src;
    public Target( Selector sel, String source )
    {
        this.sel = sel;
        this.src = source;
    }
    Selector getSelector()
    {
        return sel;
    }
    void updateStart( int from )
    {
        sel.updateStart( from );
    }
    void updateLen( int len )
    {
        sel.updateLen( len );
    }
    void update() throws AnnotationException
    {
        sel.update();
    }
    boolean isValid()
    {
        return sel.end()>sel.start();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t\"@id\": ");
        sb.append("\"urn:uuid:"+UUID.randomUUID()+"\",\n");
        sb.append("\t\"@type\": ");
        sb.append("\"oa:SpecificResource\",\n");
        sb.append("\t\"hasSelector\": ");
        sb.append(Annotation.indent(sel.toString(),1));
        sb.append(",\n");
        sb.append("\t\"hasSource\": {\n");
        sb.append("\t\t\"@id\": ");
        sb.append("\""+src+"\"\n");
        sb.append("\t}");
        sb.append("\n}");
        return sb.toString();
    }
    Dimension getRange()
    {
        return new Dimension( sel.start(), sel.end());
    }
    public static void main( String[] args )
    {
        TextSelector ts = new TextSelector( 10, 20 );
        Target t = new Target( ts, "The quick brown fox jumps over the lazy dog");
        System.out.println( t );
    }
}
