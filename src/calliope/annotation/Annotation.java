/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.annotation;
import calliope.DrupalLogin;
import calliope.exception.AnnotationException;
import edu.luc.nmerge.mvd.diff.*;
import java.net.URL;
import java.util.ArrayList;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * An annotation is a chunk of data with start offset and length
 * @author desmond
 */
public class Annotation 
{
    Target target;
    ArrayList<Body> bodies;
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
        Body b = new TextBody(body);
        if ( bodies == null )
            bodies = new ArrayList<Body>();
        bodies.add( b );
        this.kind = AnnotationKind.NOTE;
        this.target = new Target( new TextSelector(start,end), src );
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
            sb.append("\t\t\t\"oa:hasBody\": {\n");
            sb.append("\t\t\t\t");
            sb.append( bodies.get(i).toString() );
            sb.append("\n\t\t\t}"); 
            if ( i < bodies.size()-1 )
                sb.append(",");
            sb.append("\n");
        }
        sb.append("\t]\n");
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
    String postToService( String host, String cookie ) throws Exception
    {
        String query = this.toString();
        URL url = new URL(host);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length",  
            String.valueOf(query.getBytes().length));
        System.out.println(query);
        OutputStream os = connection.getOutputStream();
        os.write(query.getBytes());
        os.close();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));
        String line;
        while ( (line = br.readLine()) != null)
            sb.append(line);
        br.close();
        os.close();
        return sb.toString();
    }
    /**
     * Test annotation updating
     * @param args the arguments - ignored
     */
    public static void main(String[] args )
    {
        Annotation[] anns = new Annotation[5];
        /*String text1 = "Lorem ipsum dolor sit amet, consectetur adipisicing "
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
            +"qui officia deserunt mollit anim id est pathetic.";*/
        String text3 = 
            "<p>Lorem ipsum <em>dolor</em> sit amet, consectetur adipisicing " +
            "elit, sed do eiusmod tempor incididunt ut labore et dolore magna " +
            "aliqua. Ut enim ad <a href=\"http://latin.junk.com/minim\">minim " +
            "veniam</a>, quis nostrud exercitation ullamco laboris nisi ut " +
            "aliquip ex ea commodo consequat.</p> <h3>Duis aute irure " +
            "dolor</h3><p> in reprehenderit in voluptate velit esse cillum " +
            "dolore eu fugiat nulla pariatur. Excepteur sint </p><pre>occaecat " +
            "cupidatat non proident\n, sunt in culpa</pre> " +
            "<p>qui officia deserunt</p>";
        String service = "http://austese.net/lorestore/oa/";
        /*Diff[] diffs = Matrix.computeBasicDiffs( text2.getBytes(), 
            text1.getBytes() );*/
        try
        {
//            DrupalLogin dl = new DrupalLogin();
//            String cookie = dl.login( "austese.net", "desmond", "P1nkz3bra" );
//            if ( cookie != null )
//            {
                anns[0] = new Annotation( "english/desmond/test",
                    "This bit is pretty silly", 0, 26 );
                anns[1] = new Annotation( "english/desmond/test", "Correction by Desmond", 73, 110 );
                anns[2] = new Annotation( "english/desmond/test", 
                    "Hey look at this!", 
                    new URL("http://localhost/images/pic.png"), 233,244 );
                anns[3] = new Annotation( "english/desmond/test", 
                    "Or 'my fault'", 389, 398 );
                anns[4] = new Annotation( "english/desmond/test", null, 
                    new URL("http://images/all.png"), 
                    438, 445 );
                for ( int i=0;i<anns.length;i++ )
                {
                    if ( i == 0 )
                    {
                        //String ans = anns[i].postToService( service, cookie );
                        System.out.println(anns[i].toString() );
                        break;
                    }
                }
//                dl.logout( "austese.net", cookie );
//            }
//            else
//                System.out.println("Couldn't login");
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
        /*for ( int i=0;i<diffs.length;i++ )
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
                System.out.println(anns[i].toString());
                Dimension d2 = anns[i].getRange();
                System.out.println(text2.substring(d2.width,d2.height));
            }
        }
        catch ( AnnotationException ae )
        {
            ae.printStackTrace( System.out );
        }*/
    }
}
