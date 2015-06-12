/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.annotate;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.InputSource;

/**
 * Strip notes from a TEI-XML file 
 * @author desmond
 */
public class NoteStripper 
{
   /*
    * filter all elements whose tag name = note
    */
   private void filterElements( Node parent, String filter )
   {
        NodeList children = parent.getChildNodes();
        for ( int i=0; i<children.getLength(); i++ )
        {
            Node child = children.item( i );
            if ( child.getNodeType() == Node.ELEMENT_NODE )
            {
                 if ( child.getNodeName().equals(filter) )
                     parent.removeChild( child );
                 else 
                     filterElements( child, filter );
            }
        }
    }
    //method to convert Document to String
    private String getStringFromDocument(Document doc) throws Exception
    {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    } 
    public String strip( String xml ) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        document = builder.parse( is );
        filterElements( document, "note" );
        return getStringFromDocument(document);
    }
}