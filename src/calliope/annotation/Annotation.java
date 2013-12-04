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

package calliope.annotation;
import calliope.exception.AnnotationException;
import edu.luc.nmerge.mvd.diff.*;
import java.util.ArrayList;
import java.util.UUID;
import java.awt.Dimension;
import java.net.URL;
import java.net.MalformedURLException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * An annotation is a chunk of data with start offset and length
 * @author desmond
 */
public class Annotation 
{
    Target target;
    String id = "http://www.example.org/annotations/dummyid";
    String author;
    ArrayList<Body> bodies;
    AnnotationKind kind;
    static String PERSON = "http://xmlns.com/foaf/0.1/Person";
    static String ANNOTATION = "http://www.w3.org/ns/oa#Annotation";
    static String SPECIFIC_RESOURCE = "http://www.w3.org/ns/oa#SpecificResource";
    static String CONTENT_AS_TEXT = "http://www.w3.org/2011/content#ContentAsText";
    static String TEXT_POSITION = "http://www.w3.org/ns/oa#TextPositionSelector";
    /**
     * Create an annotation from parsed XML
     * @param doc a DOM document
     */
    public Annotation( Document doc )
    {
        bodies = new ArrayList<Body>();
        try
        {
            XPath xpath = XPathFactory.newInstance().newXPath();
            UniversalNamespaceCache ctx = new UniversalNamespaceCache(doc,false);
            xpath.setNamespaceContext(ctx);
            XPathExpression expr1 = xpath.compile( "//rdf:Description" );
            NodeList nl = (NodeList)expr1.evaluate(doc,XPathConstants.NODESET);
            for ( int i=0;i<nl.getLength();i++ )
            {
                Node n = nl.item( i );
                Node child = n.getFirstChild();
                while ( child != null )
                {
                    // get first element
                    if ( child.getNodeType()==Node.ELEMENT_NODE )
                    {
                        Element e = (Element)child;
                        String type = e.getAttribute("rdf:resource");
                        if ( type != null )
                        {
                            if ( type.equals(PERSON) )
                                readPerson(e);
                            else if ( type.equals(ANNOTATION) )
                                readAnnotation( e );
                            else if ( type.equals(SPECIFIC_RESOURCE) )
                                readSpecificResource( e );
                            else if ( type.equals(CONTENT_AS_TEXT) )
                                readBodyText( e );
                            else if (type.equals(TEXT_POSITION) )
                                readTextPosition( e );
                        }
                        break;
                    }
                    else
                        child = child.getNextSibling();
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
    }
    /**
     * Read the complex annotation object
     * @param e the parent of the annotation itself
     */
    final void readAnnotation( Element e )
    {
        Element p = (Element)e.getParentNode();
        String attr = p.getAttribute("rdf:about");
        if ( attr != null )
            this.id = attr;
        Node child = e;
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                Element elem = (Element) child;
                if ( elem.getNodeName().equals("hasBody") )
                {
                    String attr2 = elem.getAttribute("rdf:resource");
                    if ( attr2 != null && Annotation.isImage(attr2) )
                    {
                        try
                        {
                            ImageBody ib = new ImageBody( new URL(attr2) );
                            bodies.add( ib );
                        }
                        catch ( Exception ex )
                        {
                        }
                    }
                }
            }
            child = child.getNextSibling();
        }
    }
    static boolean isImage( String url )
    {
        if ( url.endsWith(".png") || url.endsWith(".jpg") 
            || url.endsWith(".gif") || url.endsWith(".tif") )
            return true;
        else
            return false;
    }
    final void readTextPosition( Element e ) throws AnnotationException
    {
        int start = -1;
        int end = -1;
        Node child = e;
        try
        {
            while ( child != null )
            {
                if ( child.getNodeType()==Node.ELEMENT_NODE )
                {
                    Element elem = (Element) child;
                    if ( elem.getNodeName().equals("start") )
                    {
                        String value = elem.getTextContent();
                        start = Integer.parseInt( value );
                    }
                    else if ( elem.getNodeName().equals("end") )
                    {
                        String value = elem.getTextContent();
                        end = Integer.parseInt( value );
                    }
                }
                child = child.getNextSibling();
            }
            if ( start != -1 && end != -1 )
            {
                TextSelector ts = new TextSelector( start, end );
                if ( target == null )
                    target = new Target();
                target.setSelector( ts );
            }
            else
                throw new Exception( "start and/or end of selector unset");
        }
        catch ( Exception ex )
        {
            throw new AnnotationException( ex );
        }
    }
    final void readSpecificResource( Element e )
    {
        /*<rdf:Description rdf:about="urn:uuid:2b112adb-909b-4a8a-8199-ef39d3307afc">	
        <rdf:type rdf:resource="http://www.w3.org/ns/oa#SpecificResource"/>	
        <hasSelector xmlns="http://www.w3.org/ns/oa#" rdf:resource="urn:uuid:5991ac71-0311-4654-ac20-ca7068a8c545"/>	
        <hasSource xmlns="http://www.w3.org/ns/oa#" rdf:resource="urn:aese:english/desmond/test"/>
        </rdf:Description>*/
        Node child = e;
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                Element elem = (Element) child;
                if ( elem.getNodeName().equals("hasSource") )
                {
                    String attr = elem.getAttribute("rdf:resource");
                    if ( attr.startsWith("urn:aese:") )
                    {
                        if ( target == null )
                            target = new Target();
                        target.setSrc( attr.substring(9) );
                    }
                }
            }
            child = child.getNextSibling();
        }
    }
    /**
     * Read the textual body of an annotation
     * @param e the parent element
     */
    final void readBodyText( Element e )
    {
        /*<rdf:Description rdf:about="urn:uuid:1eaaf6bc-95de-47d8-83bd-5fc74d42d736">	
        <rdf:type rdf:resource="http://www.w3.org/2011/content#ContentAsText"/>	
        <rdf:type rdf:resource="http://purl.org/dc/dcmitype/Text"/>	
        <chars xmlns="http://www.w3.org/2011/content#">This bit is pretty silly</chars>	
        <characterEncoding xmlns="http://www.w3.org/2011/content#">UTF-8</characterEncoding>
        </rdf:Description>*/
        Element p = (Element)e.getParentNode();
        String uuid = p.getAttribute("rdf:about");
        Node child = e;
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                Element elem = (Element) child;
                if ( elem.getNodeName().equals("chars") )
                {
                    TextBody tb = new TextBody( child.getTextContent() );
                    if ( uuid != null )
                        tb.setUuid( uuid );
                    bodies.add( tb );
                }
            }
            child = child.getNextSibling();
        }
    }
    /**
     * Read a person specification
     * @param e the rdf description node contianing him/her
     */
    final void readPerson( Element e )
    {
        /*<rdf:Description rdf:about="http://austese.net/user/3">
        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
        <name xmlns="http://xmlns.com/foaf/0.1/">desmond</name>
        </rdf:Description>*/
        Node child = e;
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                Element elem = (Element) child;
                if ( elem.getNodeName().equals("name") )
                {
                    this.author = elem.getTextContent();
                    break;
                }
            }
            child = child.getNextSibling();
        }
    }
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
    /**
     * Create an image annotation
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
     * Get the "id" or lorestore url that identifies this annocation
     * @return a url as a string with an embedded id
     */
    String getId()
    {
        return id;
    }
    /**
     * Just print an XML node to the console for debugging
     * @param node the node in question
     */
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
     * Get the end-offset
     * @return an int
     */
    public int end()
    {
        return target.getSelector().end();
    }/**
     * Get the start offset of the annotation in the target
     * @return an int
     */
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
    /**
     * Adjust this annotation's length
     * @param newLen the new length it is to have
     */
    public void updateLen( int newLen )
    {
        target.updateLen( newLen );
    }
    /**
     * Execute the staged changes
     * @throws AnnotationException 
     */
    public void update() throws AnnotationException
    {
        target.update();
    }
    /**
     * Get the range in the target 
     * @return a Dimension as start, end (one after end) pair
     */
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
    /**
     * Indent a paragraph of text by tabs
     * @param text the text to indent
     * @param nTabs the number of tabs to add
     * @return the revised string
     */
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
    /**
     * Print the "grtaph" element in the annotation, JSON-LD format
     * @return the text of the whole @graph element
     */
    private String printGraph()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\"@graph\": [\n");
        sb.append("\t\t{\n");
        sb.append("\t\t\t\"@id\": \"");
        //sb.append("\"http://www.example.org/annotations/dummyid\",\n");
        sb.append(id);
        sb.append("\",\n");
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
    /**
     * Convert the whole annotation object to a string
     * @retun a JSON-LD representation suitable for Lorestore to understand
     */
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
    /**
     * Create some test annotations
     * @param docID their docID
     * @return an array of suitable test annotations
     * @throws MalformedURLException 
     */
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
     * Test annotations
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
//            as.deleteByDocID(docID);
            //Annotation[] anns = Annotation.fakeAnnotations( docID );
            //System.out.println("original annotations:");
            //for ( int i=0;i<anns.length;i++ )
            //    System.out.println(anns[i].toString());
            //as.doStore( anns );
            //doUpdate();
            Annotation[] anns2 = as.getAnnotationsFor( docID );
            System.out.println("retrieved annotations:");
            for ( int i=0;i<anns2.length;i++ )
                System.out.println(anns2[i].toString());
            as.logout();
         }
         catch ( Exception e )
         {
            e.printStackTrace( System.out );
         }
    }
}
