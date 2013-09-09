/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;

import static calliope.handler.post.importer.Splitter.VERSIONS;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Print an XML file to multiple split outputs
 * @author desmond
 */
public class XMLPrinter 
{
    /**
     * Print a single element node
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to buffers
     */
    static void printElementBody( Element elem, ArrayList<String> versions, 
        HashMap<String,StringBuilder> map, HashSet<String> removals )
    {
        // recurse into its children
        Node child = elem.getFirstChild();
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                String vAttr = ((Element)child).getAttribute(VERSIONS);
                if ( vAttr.length()>0 )
                {
                    String[] cVersions = vAttr.split(" ");
                    ArrayList<String> attrs = new ArrayList<String>();
                    for ( int i=0;i<cVersions.length;i++ )
                    {
                        if ( versions.contains(cVersions[i]) )
                            attrs.add( cVersions[i] );
                    }
                    printAll( child, attrs, map, removals );
                }
                else    // shouldn't happen
                    printAll( child, versions, map, removals );
            }
            else
                printAll( child, versions, map, removals );
            child = child.getNextSibling();
        }
    }
    /**
     * Print an element's end-code
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to string buffers
     */
    static void printElementEnd( Element elem, ArrayList<String> versions, 
        HashMap<String,StringBuilder> map )
    {
        for ( int i=0;i<versions.size();i++ )
        {
            StringBuilder sb = map.get(versions.get(i));
            if ( elem.getFirstChild()!=null )
            {
                sb.append( "</" );
                sb.append(elem.getNodeName() );
                sb.append( ">" );
            }
        }
    }
    /**
     * Print a single element node
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to buffers
     */
    static void printElementStart( Element elem, ArrayList<String> versions, 
        HashMap<String,StringBuilder> map )
    {
        for ( int i=0;i<versions.size();i++ )
        {
            StringBuilder sb = map.get(versions.get(i));
            sb.append("<");
            sb.append(elem.getNodeName());
            NamedNodeMap attrs = elem.getAttributes();
            for ( int j=0;j<attrs.getLength();j++ )
            {
                Node attr = attrs.item( j );
                if ( !attr.getNodeName().equals(VERSIONS) )
                {
                    sb.append( " ");
                    sb.append( attr.getNodeName() );
                    sb.append( "=\"" );
                    sb.append( attr.getNodeValue() );
                    sb.append( "\"" );
                }
            }
            if ( elem.getFirstChild()==null )
                sb.append("/>");
            else
                sb.append(">");
        }
    }
    /**
     * Write all the versions to the string builders in the map
     * @param node the node to start from
     * @param versions the set of versions to follow
     * @param map the map with ALL the versions
     * @param removals a set of tags to remove
     */
    static void printAll( Node node, ArrayList<String> versions, 
        HashMap<String,StringBuilder> map, HashSet<String> removals )
    {
        if ( node.getNodeType()==Node.TEXT_NODE )
        {
            for ( int i=0;i<versions.size();i++ )
            {
                StringBuilder sb = map.get( versions.get(i) );
                String content = node.getTextContent();
                if (content.indexOf("&")!= -1 )
                    content = content.replace("&","&amp;");
                sb.append( content );
            }
        }
        else if ( node.getNodeType()==Node.ELEMENT_NODE )
        {
            boolean strip = false;
            if ( removals.contains(node.getNodeName()) )
                strip = true;
            if ( !strip )
                printElementStart( (Element)node, versions, map );
            printElementBody( (Element)node, versions, map, removals );
            if ( !strip )
                printElementEnd( (Element)node, versions, map );
        }
        else if ( node.getNodeType()==Node.COMMENT_NODE )
        {
            for ( int i=0;i<versions.size();i++ )
            {
                StringBuilder sb = map.get(versions.get(i));
                sb.append("<!--");
                sb.append(node.getTextContent());
                sb.append("-->");
            }
        }
    }
    /**
     * Split elements labelled with versions into separate versions
     * @param doc the XML version-labelled document to split
     * @param drops an array of versions to drop
     * @param removals a set of element tags to remove (but not contents)
     * @return an array of split XML sub-documents
     */
    static Map<String,String> splitAll( Document doc, String[] drops, 
        HashSet<String> removals )
    {
        Element root = doc.getDocumentElement();
        String versions = root.getAttribute( Splitter.VERSIONS );
        String[] parts = versions.split( " " );
        HashMap<String,StringBuilder>map = new HashMap<String,StringBuilder>();
        ArrayList<String> all = new ArrayList<String>();
        for ( int i=0;i<parts.length;i++ )
        {
            boolean drop = false;
            for ( int j=0;j<drops.length;j++ )
            {
                if ( drops[j].equals(parts[i]) )
                    drop = true;
            }
            if ( !drop )
            {
                all.add( parts[i] );
                map.put( parts[i], new StringBuilder() );
            }
        }
        printAll( root, all, map, removals );
        // now write out the results
        HashMap<String,String> versionMap = new HashMap<String,String>();
        Set<String> keys = map.keySet();
        Iterator<String> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            String key = iter.next();
            StringBuilder sb = map.get( key );
            String str = sb.toString();
            versionMap.put( key, str );
        }
        return versionMap;
    }
}
