/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.annotation;
import calliope.exception.AnnotationException;
import edu.luc.nmerge.mvd.diff.*;
import java.net.URL;
import java.util.ArrayList;
import java.awt.Dimension;

/**
 * An annotation is a chunk of data with start offset and length
 * @author desmond
 */
public class Annotation 
{
    Target target;
    /** body of annotation */
    Body body;
    
    ArrayList<String> bodies;
    AnnotationKind kind;
    /**
     * Create a text annotation
     * @param src the id/url of the document being encoded
     * @param body the body of the annotation
     * @param start the start offset
     * @param len its one past the end offset in the target
     */
    public Annotation( String src, String body, int start, int end )
    {
        this.body = new TextBody(body);
        this.kind = AnnotationKind.NOTE;
        this.target = new Target( new TextSelector(start,end), src );
    }
    /**
     * Create a text annotation
     * @param src the id/url of the document being encoded
     * @param body the body of the annotation
     * @param start the start offset
     * @param len its one past the end offset in the target
     */
    public Annotation( String src, URL body, int start, int end )
    {
        this.body = new ImageBody(body);
        this.kind = AnnotationKind.IMAGE;
        this.target = new Target( new TextSelector(start,end), src );
    }
    /**
     * Get the end-offset
     * @return an int
     */
    public int end()
    {
        return target.getSelector().end();
    }
    public int start()
    {
        return target.getSelector().start();
    }
    /**
     * Adjust the annotation start point
     * @param from the new absolute from offset
     */
    public void updateOff( int from ) 
    {
        target.updateStart( from ); 
    }
    public void updateLen( int newLen )
    {
        target.updateLen( newLen );
    }
    public void update() throws AnnotationException
    {
        target.update();
    }
    public Dimension getRange()
    {
        return target.getRange();
    }
    /**
     * Return true if the annotation remains after updates
     * @return false if it can be deleted now
     */
    public boolean isValid()
    {
        return target.isValid();
    }
    public static String indent( String text, int nTabs )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<text.length();i++ )
        {
            sb.append( text.charAt(i) );
            if ( text.charAt(i) == '\n' )
                for ( int j=0;j<nTabs;j++ )
                    sb.append("\t");
        }
        return sb.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t\"@context\": ");
        sb.append("\"http://www.w3.org/ns/oa-context-20130208.json\",\n"); 
        sb.append("\t\"@id\": ");
        sb.append("\"http://www.example.org/annotations/dummyid\",\n");
        sb.append("\t\"@type\": ");
        sb.append("\"oa:Annotation\",\n");
        sb.append("\t\"annotatedBy\": ");
        sb.append("{\n");
        sb.append("\t\t\"@id\": ");
        sb.append("http://austese.net/calliope\"\n"); 
        sb.append("\t},\n");
        sb.append("\t\"hasBody\": ");
        sb.append(indent(body.toString(),1));
        sb.append(",\n"); 
        sb.append("\t\"hasTarget\": ");
        sb.append( indent(this.target.toString(),1));
        sb.append(",\n");
        sb.append("\t\"motivatedBy\": ");
        sb.append( "\"oa:linking\"\n");
        sb.append("}");
        return sb.toString();
    }
    /**
     * Test annotation updating
     * @param args the arguments - ignored
     */
    public static void main(String[] args )
    {
        Annotation[] anns = new Annotation[5];
        String text1 = "Lorem ipsum dolor sit amet, consectetur adipisicing "
            +"elit, sed do eiusmod tempor incididunt ut labore et dolore "
            +"magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation"
            +" ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis "
            +"aute irure dolor in reprehenderit in voluptate velit esse "
            +"cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat"
            +" cupidatat non proident, sunt in culpa qui officia deserunt "
            +"mollit anim id est laborum.";
        String text2 = "Lorem ipsum happiness sit amet, consectetur adipisicing"
            +" elit, sed do eiusmod time flies ut labore et dolore magna "
            +"aliqua. Ut enim ad minim veniam, quis nostrud exercitation "
            +"ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis "
            +"aute irure dolor in reprehenderit in desire, sunt in your fault "
            +"qui officia deserunt mollit anim id est pathetic.";
        Diff[] diffs = Matrix.computeBasicDiffs( text2.getBytes(), 
            text1.getBytes() );
        anns[0] = new Annotation( "test",
            "This bit is pretty silly", 0, 26 );
        anns[1] = new Annotation( "test", "Correction by Desmond", 73, 110 );
        try
        {
            anns[2] = new Annotation( "test", 
                new URL("http://localhost/images/pic.png"), 233,244 );
            anns[3] = new Annotation( "test", "Or 'my fault'", 389, 398 );
            anns[4] = new Annotation( "test", new URL("http://images/all.png"), 
                438, 445 );
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
        for ( int i=0;i<diffs.length;i++ )
        {
            for ( int j=0;j<anns.length;j++ )
            {
                int start = anns[j].target.getSelector().start();
                if ( start > diffs[i].oldEnd() )
                    break;
                else if ( anns[j].end() < diffs[i].oldOff() )
                {
                    // only applies IF the offset wasn't already set
                    int delta = diffs[i].newOff()-diffs[i].oldOff();
                    anns[j].updateOff(start+delta);
                }
                // else check for some kind of overlap
                else if ( anns[j].end() > diffs[i].oldOff() 
                    && start < diffs[i].oldEnd() )
                {
                    // compute the proportion of the overlap that gets 
                    // carried over to the updated annotation
                    int overlap = Math.min(anns[j].end(),diffs[i].oldEnd())
                        -Math.max(start,diffs[i].oldOff());
                    int prop;
                    if ( overlap == diffs[i].oldLen() || diffs[i].oldLen()==0 )
                        prop = diffs[i].newLen();
                    else
                    {
                        float ratio = (float)diffs[i].newLen()
                            /(float)diffs[i].oldLen();
                        prop = Math.round(overlap*ratio);
                    }
                    // update the position of the annotation start
                    int newOff;
                    // 1) annotation starts before diff
                    if ( start < diffs[i].oldOff() )
                    {
                        int dist = diffs[i].oldOff()-start;
                        // adjust for new offset in text2
                        newOff = diffs[i].newOff()-dist;
                    }
                    // 2) diff starts before annotation
                    else if ( diffs[i].oldOff() < start )
                    {
                        float ratio = (float)diffs[i].newLen()
                            /(float)diffs[i].oldLen();
                        int dist = start-diffs[i].oldOff();
                        newOff = diffs[i].newOff()+(int)(ratio*dist);
                    }
                    // 3) equal
                    else
                        newOff = diffs[i].newOff();
                    // compute new end
                    int newLen = prop;
                    if ( anns[j].end() > diffs[i].oldEnd() )
                        newLen += anns[j].end()-diffs[i].oldEnd();
                    if ( newOff < diffs[i].newOff() )
                        newLen += diffs[i].newOff()-newOff;
                    anns[j].updateOff( newOff );
                    anns[j].updateLen( newLen );
                }
            }
        }
        int lastStart = anns[anns.length-1].target.getSelector().start();
        int delta = text2.length()-text1.length();
        anns[anns.length-1].updateOff(lastStart+delta);
        try
        {
            for ( int i=0;i<anns.length;i++ )
            {
                Dimension d1 = anns[i].getRange();
                System.out.println(text1.substring(d1.width,d1.height));
                anns[i].update();
                Dimension d2 = anns[i].getRange();
                System.out.println(text2.substring(d2.width,d2.height));
            }
        }
        catch ( AnnotationException ae )
        {
            ae.printStackTrace( System.out );
        }
    }
}
