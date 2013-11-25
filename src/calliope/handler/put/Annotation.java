/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.put;
import calliope.exception.AnnotationException;
import edu.luc.nmerge.mvd.diff.*;

/**
 * An annotation is a chunk of data with start offset and length
 * @author desmond
 */
public class Annotation 
{
    int start;
    int newStart;
    int len;
    String data;
    boolean offsetUpdated;
    AnnotationKind kind;
    int delta;
    public Annotation( AnnotationKind kind, String data, int start, int len )
    {
        this.kind = kind;
        this.data = data;
        this.start = start;
        this.len = len;
    }
    /**
     * Get the end-offset
     * @return an int
     */
    int end()
    {
        return this.start+this.len;
    }
    /**
     * Adjust the annotation start point
     * @param from the new absolute from offset
     */
    void updateOff( int from ) 
    {
        if ( !this.offsetUpdated )
        {
            this.newStart = from;
            this.offsetUpdated = true;
        }
    }
    void updateLen( int newLen )
    {
        this.delta += newLen - this.len;
    }
    void update() throws AnnotationException
    {
        this.len += delta;
        if ( offsetUpdated )
            this.start = newStart;
        if ( this.len < 0 )
            throw new AnnotationException("annotation length is negative");
    }
    /**
     * Return true if the annotation remains after updates
     * @return false if it can be deleted now
     */
    boolean isValid()
    {
        return len > 0;
    }
    private static void printAnns( Annotation[] anns, String text )
    {
        for ( int i=0;i<anns.length;i++ )
        {
            if ( anns[i].isValid() )
            {
                String sub = text.substring( anns[i].start, 
                    anns[i].start+anns[i].len );
                System.out.println( sub + "=" + anns[i].data );
            }
        }
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
        anns[0] = new Annotation(AnnotationKind.NOTE, 
            "This bit is pretty silly", 0, 26 );
        anns[1] = new Annotation( AnnotationKind.NOTE, "Correction by Desmond",
            73, 37 );
        anns[2] = new Annotation( AnnotationKind.IMAGE, 
            "http://localhost/images/pic.png", 233, 21 );
        anns[3] = new Annotation( AnnotationKind.NOTE, "Or 'my fault'", 389, 8 );
        anns[4] = new Annotation( AnnotationKind.IMAGE, "http://images/all.png", 438, 7 );
        for ( int i=0;i<diffs.length;i++ )
        {
            for ( int j=0;j<anns.length;j++ )
            {
                if ( anns[j].start > diffs[i].oldEnd() )
                    break;
                else if ( anns[j].end() < diffs[i].oldOff() )
                {
                    if ( j == 4 )
                        System.out.println("4");
                    int delta = diffs[i].newOff()-diffs[i].oldOff();
                    anns[j].updateOff(anns[j].start+delta);
                }
                else if ( anns[j].end() > diffs[i].oldOff() 
                    && anns[j].start < diffs[i].oldEnd() )
                {
                    // compute the proportion of the overlap that gets 
                    // carried over to the updated annotation
                    int overlap = Math.min(anns[j].end(),diffs[i].oldEnd())
                        -Math.max(anns[j].start,diffs[i].oldOff());
                    int prop;
                    if ( diffs[i].oldLen()==0 )
                        prop = diffs[i].newLen();
                    else
                        prop = (overlap*diffs[i].newLen())/diffs[i].oldLen();
                    // update the position of the annotation start
                    int newOff;
                    // 1) annotation starts before diff
                    if ( anns[j].start < diffs[i].oldOff() )
                    {
                        int dist = diffs[i].oldOff()-anns[j].start;
                        newOff = diffs[i].newOff()-dist;
                    }
                    // 2) diff starts before annotation
                    else if ( diffs[i].oldOff() < anns[j].start )
                    {
                        float ratio = (float)diffs[i].newLen()
                            /(float)diffs[i].oldLen();
                        int dist = anns[j].start-diffs[i].oldOff();
                        newOff = diffs[i].newOff()+(int)(ratio*dist);
                    }
                    // 3) equal
                    else
                        newOff = diffs[i].newOff();
                    // compute new length
                    int newLen = prop;
                    if ( anns[j].end() > diffs[i].oldEnd() )
                        newLen += anns[j].end()-diffs[i].oldEnd();
                    if ( anns[j].start < diffs[i].oldOff() )
                        newLen += diffs[i].oldOff()-anns[j].start;
                    if ( j == 4 )
                        System.out.println("4");
                    anns[j].updateOff( newOff );
                    anns[j].updateLen( newLen );
                }
            }
        }
        int delta = text2.length()-text1.length();
        anns[anns.length-1].updateOff(anns[anns.length-1].start+delta);
        try
        {
            printAnns( anns, text1 );
            for ( int i=0;i<anns.length;i++ )
            {
                anns[i].update();
            }
            printAnns( anns, text2 );
        }
        catch ( AnnotationException ae )
        {
            ae.printStackTrace( System.out );
        }
    }
}
