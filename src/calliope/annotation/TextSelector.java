package calliope.annotation;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import calliope.annotation.Selector;
import calliope.exception.AnnotationException;
import java.util.UUID;

/**
 * Real selector for a text selection
 * @author desmond
 */
public class TextSelector extends Selector
{
    /** absolute offset to start */
    int start;
    /** length of selection */
    int len;
    /**  */
    int newStart;
    int delta;
    boolean startUpdated = false;
    public TextSelector( int start, int end )
    {
        this.start = start;
        this.len = end-start;
    }
    void updateStart( int from ) 
    {
        if ( !this.startUpdated )
        {
            this.newStart = from;
            this.startUpdated = true;
        }
    }
    void updateLen( int newLen )
    {
        this.delta += newLen - len;
    }
    void update() throws AnnotationException
    {
        this.len += delta;
        if ( startUpdated )
            this.start = newStart;
        if ( this.len < 0 )
            throw new AnnotationException("annotation length is negative");
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t\"start\": ");
        sb.append( start );
        sb.append( ",\n");
        sb.append("\t\"@type\": ");
        sb.append( "\"oa:TextPositionSelector\",\n");
        sb.append( "\t\"@id\": ");
        UUID uuid = UUID.randomUUID();
        sb.append("\"urn:uuid:"+uuid.toString()+"\"");
        sb.append(",\n");
        sb.append("\t\"end\": ");
        sb.append( Integer.toString(start+len) );
        sb.append("\n}");
        return sb.toString();
    }
    int end()
    {
        return start+len;
    }
    int start()
    {
        return start;
    }
    public static void main(String[] args )
    {
        TextSelector ts = new TextSelector( 10, 100 );
        String sel = ts.toString();
        System.out.println(sel);
    }
}
