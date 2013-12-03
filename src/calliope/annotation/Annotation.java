/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.annotation;
import calliope.exception.AnnotationException;
import edu.luc.nmerge.mvd.diff.*;
import java.util.ArrayList;
import java.awt.Dimension;
import java.net.URL;
import java.net.MalformedURLException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;

/**
 * An annotation is a chunk of data with start offset and length
 * @author desmond
 */
public class Annotation 
{
    Target target;
    String id;
    ArrayList<Body> bodies;
    AnnotationKind kind;
    /**
     * Create a text annotation
     * @param src the id/url of the document being encoded
     * @param body the body of the annotation
     * @param start the start offset
     * @param end one past the end offset in the target
     */
    public Annotation( String src, String body, int start, int end )
    {
        Body b = new TextBody(body);
        if ( bodies == null )
            bodies = new ArrayList<Body>();
        bodies.add( b );
        this.kind = AnnotationKind.NOTE;
        this.target = new Target( new TextSelector(start,end), src );
    }
    String getId()
    {
        return id;
    }
    static void printNode( Node node )
    {
        StringWriter sw = new StringWriter();
        try 
        {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        }
        catch (TransformerException te) 
        {
            System.out.println("nodeToString Transformer Exception");
        }
        System.out.println(sw.toString());
    }
    /**
     * Create an annotation from an XML document
     * @param doc the doc parsed from the annotation server's response
     */
    public Annotation( Document doc ) throws AnnotationException
    {
        
    }
    /**
     * Create a text annotation
     * @param src the id/url of the document being encoded
     * @param caption the caption being added to the document
     * @param body the body of the annotation
     * @param start the start offset
     * @param len its one past the end offset in the target
     */
    public Annotation( String src, String caption, URL body, int start, int end )
    {
        Body b = new ImageBody(body);
        if ( bodies == null )
            bodies = new ArrayList<Body>();
        bodies.add( b );
        if ( caption != null && caption.length()>0 )
        {
            Body c = new TextBody( caption );
            bodies.add( c );
        }
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
    private String printGraph()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\"@graph\": [\n");
        sb.append("\t\t{\n");
        sb.append("\t\t\t\"@id\": ");
        sb.append("\"http://www.example.org/annotations/dummyid\",\n");
        sb.append("\t\t\t\"@type\": ");
        sb.append("\"http://www.w3.org/ns/oa#Annotation\",\n");
        sb.append("\t\t\t\"oa:annotatedBy\": {\n");
        sb.append("\t\t\t\t\"@id\": ");
        sb.append("\"http://austese.net/calliope\"\n"); 
        sb.append("\t\t\t},\n");
        for ( int i=0;i<bodies.size();i++ )
        {
            sb.append("\t\t\t\"oa:hasBody\": {\n");
            sb.append("\t\t\t\t");
            sb.append( bodies.get(i).getId() );
            sb.append("\n\t\t\t},\n"); 
        }
        sb.append("\t\t\t\"oa:hasTarget\": {\n\t\t\t\t");
        sb.append( this.target.getId() );
        sb.append("\n\t\t\t},\n");
        sb.append("\t\t\t\"oa:motivatedBy\": ");
        sb.append( "\"http://www.w3.org/ns/oa#linking\"\n");
        sb.append("\t\t},\n");
        for ( int i=0;i<bodies.size();i++ )
        {
            String b = bodies.get(i).toString();
            if ( b.length()>0 )
            {
                sb.append("\t\t{\n\t\t\t");
                sb.append( Annotation.indent(b,3) );
                sb.append("\n\t\t}"); 
                sb.append(",");
                sb.append("\n");
            }
        }
        sb.append("\t\t{\n\t\t\t");
        sb.append(Annotation.indent(target.toString(),3));
        String sel = target.getSelector().toString();
        sb.append("\n\t\t},\n\t\t{\n\t\t\t");
        sb.append( Annotation.indent(sel,3));
        sb.append("\n\t\t}");
        sb.append("\n\t]\n");
        return sb.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append( "\t\"@context\": {\n");
        sb.append( "\t\t\"oa\": \"http://www.w3.org/ns/oa#\",\n");
        sb.append( "\t\t\"dc\": \"http://purl.org/dc/elements/1.1/\",\n");
        sb.append( "\t\t\"cnt\": \"http://www.w3.org/2011/content#\",\n");
        sb.append( "\t\t\"lorestore\": \"http://auselit.metadata.net/lorestore/\",\n");
        sb.append( "\t\t\"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n");
        sb.append( "\t\t\"austese\": \"http://austese.net/ns/oa/\"\n\t},\n");
        sb.append( printGraph() );
        sb.append("}");
        return sb.toString();
    }
    /**
     * Update an array of annotations
     * @param anns an array of annotations sorted on start offset
     * @param text1 the original version to which the anns point
     * @param text2 the updated version of the text
     */
    static  void updateAnnotations( Annotation[] anns, String text1, 
        String text2 ) throws AnnotationException
    {
        Diff[] diffs = Matrix.computeBasicDiffs( text2.getBytes(), 
            text1.getBytes() );
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
        for ( int i=0;i<anns.length;i++ )
        {
//          Dimension d1 = anns[i].getRange();
//          System.out.println(text1.substring(d1.width,d1.height));
            anns[i].update();
//          System.out.println(anns[i].toString());
//          Dimension d2 = anns[i].getRange();
//          System.out.println(text2.substring(d2.width,d2.height));
        }
    }
    static Annotation[] fakeAnnotations( String docID ) 
        throws MalformedURLException
    {
        Annotation[] anns = new Annotation[5];
        anns[0] = new Annotation( docID,
            "This bit is pretty silly", 0, 26 );
        anns[1] = new Annotation( docID, 
            "Correction by Desmond", 73, 110 );
        anns[2] = new Annotation( docID, 
            "Hey look at this!", 
            new URL("http://localhost/images/pic.png"), 233, 244 );
        anns[3] = new Annotation( "english/desmond/test", 
            "Or 'my fault'", 389, 398 );
        anns[4] = new Annotation( docID, null, 
            new URL("http://images/all.png"), 
            438, 445 );
        return anns;
    }
        /**
     * Test annotation updating
     * @param args the arguments - ignored
     */
    public static void main(String[] args )
    {
        String docID = "english/desmond/test";
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
        try
        {
            AnnotationService as = new AnnotationService("austese.net", 
                "desmond", "P1nkz3bra", "/lorestore/oa/");
            as.login();
            Annotation[] anns = Annotation.fakeAnnotations( docID );
            as.doStore( anns );
            //as.deleteByDocID("english/desmond/test");
            //doUpdate();
            Annotation[] anns2 = as.getAnnotationsFor( docID );
            System.out.println("Number of annotations="+anns2.length);
            as.logout();
         }
         catch ( Exception e )
         {
            e.printStackTrace( System.out );
         }
    }
}
