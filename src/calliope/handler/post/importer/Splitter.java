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

package calliope.handler.post.importer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
// these are not part of the JDK
import calliope.json.JSONDocument;
import calliope.exception.ConfigException;
import calliope.exception.ImportException;
/**
 * Split TEI-XML into versions if it contains any alternatives
 * @author desmond
 */
public class Splitter 
{
    private static String REMOVALS = "removals";
    private static String VERSIONS = "_versions";
    private static String BASE = "base";
    private HashMap<String,Removal> removals;
    private HashSet<Element> extendibles;
    private int proxy;
    static String defaultConf = 
    "{\n\"removals\":\n\t[\n\t\t{\n\t\t\t\"name\": \"app\",\n\t\t\t"
    +"\"versions\": \"none\"\n\t\t},\n\t\t{\n\t\t\t\"name\": \"rd"
    +"g\",\n\t\t\t\"wits\": \"wit\",\n\t\t\t\"versions\": \"freeze"
    +"\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"lem\",\n\t\t\t\"w"
    +"its\": \"wit\",\n\t\t\t\"versions\": \"freeze\"\n\t\t}\n\t\t"
    +",\n\t\t{\n\t\t\t\"name\": \"choice\",\n\t\t\t\"versions\": \""
    +"none\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"abbr\",\n\t\t"
    +"\t\"versions\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"na"
    +"me\": \"expan\",\n\t\t\t\"versions\": \"freeze\"\n\t\t}\n\t\t"
    +",\n\t\t{\n\t\t\t\"name\": \"add\",\n\t\t\t\"strip\": false,"
    +"\n\t\t\t\"versions\": \"extend\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t"
    +"\"name\": \"del\",\n\t\t\t\"strip\": false,\n\t\t\t\"versio"
    +"ns\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"rdg"
    +"Grp\",\n\t\t\t\"versions\": \"none\"\n\t\t}\n\t\t,\n\t\t{\n\t"
    +"\t\t\"name\": \"reg\",\n\t\t\t\"versions\": \"freeze\"\n\t\t"
    +"}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"corr\",\n\t\t\t\"version"
    +"s\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"sic\""
    +",\n\t\t\t\"versions\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t"
    +"\t\"name\": \"mod\",\n\t\t\t\"versions\": \"none\"\n\t\t}\n"
    +"\t\t,\n\t\t{\n\t\t\t\"name\": \"subst\",\n\t\t\t\"versions\""
    +": \"none\"\n\t\t}\n\t]\n}";
    Element root;
    /**
     * Initialise a splitter from its JSON config file
     * @param config a JSON document from the config database
     */
    public Splitter( JSONDocument config ) throws ConfigException
    {
        ArrayList sets = (ArrayList)config.get( REMOVALS );
        if ( sets == null )
            throw new ConfigException("invalid splitter config file");
        removals = new HashMap<String,Removal>();
        extendibles = new HashSet<Element>();
        for ( int i=0;i<sets.size();i++ )
        {
            JSONDocument doc = (JSONDocument)sets.get( i );
            Removal r = new Removal( doc );
            removals.put( r.name, r );
        }
    }
    /**
     * Merge two sets of space-delimited short version names
     * @param oldV the old version names
     * @param newV the new version names to add
     * @return the merged versions
     */
    private String mergeVersions( String oldV, String newV )
    {
        String[] oldVersions = oldV.split( " " );
        String[] newVersions = newV.split( " " );
        HashSet<String> all = new HashSet<String>();
        for ( int i=0;i<oldVersions.length;i++ )
            all.add( oldVersions[i] );
        for ( int i=0;i<newVersions.length;i++ )
            all.add( newVersions[i] );
        Iterator<String> iter = all.iterator();
        StringBuilder sb = new StringBuilder();
        while ( iter.hasNext() )
        {
            String version = iter.next();
            sb.append( version );
            if ( iter.hasNext() )
                sb.append( " " );
        }
        return sb.toString();
    }
    /**
     * Recurse to the ancestors of this node
     * @param child the start node
     * @param versions the versions of the start node
     */
    private void percolate( Node child, String versions )
    {
        Node parent = child.getParentNode();
        if ( parent == root )
        {
            // parent is root - must contain all versions in text
            Element elem = (Element)parent;
            if ( elem.hasAttribute(VERSIONS) )
                elem.setAttribute( VERSIONS, 
                    mergeVersions(elem.getAttribute(VERSIONS),versions));
            else
                elem.setAttribute( VERSIONS, versions );
        }
        else if ( parent.getNodeType()==Node.ELEMENT_NODE )
        {
            Element elem = (Element)parent;
            if ( elem.hasAttribute(VERSIONS) )
                elem.setAttribute( VERSIONS, 
                    mergeVersions(elem.getAttribute(VERSIONS),versions));
            percolate( parent, versions );
        }
        else if ( parent != null )
            percolate( parent, versions );
    }
    /**
     * Create a new version short-name by incrementing its numerical suffix
     * @param pVersions a list of space-delimited versions
     * @param suffix the prefix to search for
     * @return 
     */
    String qualify( String pVersions, String prefix )
    {
        int maxId = 0;
        String[] parts = pVersions.split(" ");
        for ( int i=0;i<parts.length;i++ )
        {
            if ( parts[i].startsWith(prefix) )
            {
                if ( parts[i].length()>prefix.length() )
                {
                    String number = parts[i].substring(prefix.length());
                    int id = Integer.parseInt(number);
                    if ( id > maxId )
                        maxId = id;
                }
            }
        }       
        return prefix+maxId;
    }
    /**
     * Add the new extendible version to all those registered as extendible.
     * @param elem the new extendible element
     * @param v its rew version
     */
    void extend( Element elem, String v )
    {
        Iterator<Element> iter = extendibles.iterator();
        while ( iter.hasNext() )
        {
            Element other = iter.next();
            String existing = other.getAttribute(VERSIONS);
            other.setAttribute(VERSIONS,mergeVersions(existing,v));
        }
        // add it to the club
        extendibles.add( elem );
    }
    /**
     * Apply a removal rule to an element but travel further in the tree
     * @param elem the element in question
     * @param r the removal in question
     * @param pVersions the versions as a space-delimited string
     * @return the new pVersions for children of this node
     */
    String applyRemoval( Element elem, Removal r, String pVersions )
    {
        String newPVersions = pVersions;
        switch ( r.versions )
        {
            case none: //app, choice, subst
                break;
            case freeze: // del, abbr, expan
                if ( r.wits != null )
                {
                    String wits = elem.getAttribute( r.wits );
                    if ( wits == null )
                        wits = "#"+proxy++;
                    elem.setAttribute( VERSIONS, wits );
                    percolate( elem.getParentNode(), wits );
                    newPVersions = wits;
                }
                else
                {
                    elem.setAttribute( VERSIONS, pVersions );
                }
                break;
            case extend:
                String v = qualify( pVersions, elem.getNodeName() );
                elem.setAttribute( VERSIONS, v );
                extend( elem, v );
                percolate( elem.getParentNode(), v );
                newPVersions = v;
                break;
        }
        return newPVersions;
    }
    /**
     * Assign versions to the specified element by adding a special attribute 
     * to matching elements specifying the versions it belongs to.
     * @param node the node to examine
     * @param pVersions parent versions
     */
    private void assignVersions( Node node, String pVersions )
    {
        while ( node != null )
        {
            if ( node.getNodeType()==Node.ELEMENT_NODE )
            {
                String newPVersions = pVersions;
                if ( removals.containsKey(node.getNodeName()) )
                {
                    Removal r = removals.get( node.getNodeName() );
                    newPVersions = applyRemoval( (Element) node, r, pVersions );
                }
                Node child = node.getFirstChild();
                assignVersions( child, newPVersions );
            }
            node = node.getNextSibling();
        }
    }
    /**
     * Print a single element node
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to buffers
     */
    void printElementBody( Element elem, String[] versions, 
        HashMap<String,StringBuilder> map )
    {

        // recurse into its children
        Node child = elem.getFirstChild();
        while ( child != null )
        {
            printAll( child, versions, map );
            child = child.getNextSibling();
        }
    }
    /**
     * Print an element's end-code
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to string buffers
     */
    void printElementEnd( Element elem, String[] versions, 
        HashMap<String,StringBuilder> map )
    {
        for ( int i=0;i<versions.length;i++ )
        {
            StringBuilder sb = map.get(versions[i]);
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
    void printElementStart( Element elem, String[] versions, 
        HashMap<String,StringBuilder> map )
    {
        for ( int i=0;i<versions.length;i++ )
        {
            StringBuilder sb = map.get(versions[i]);
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
     */
    void printAll( Node node, String[] versions, 
        HashMap<String,StringBuilder> map )
    {
        if ( node.getNodeType()==Node.TEXT_NODE )
        {
            for ( int i=0;i<versions.length;i++ )
            {
                StringBuilder sb = map.get( versions[i] );
                String content = node.getTextContent();
                if (content.indexOf("&")!= -1 )
                    content = content.replace("&","&amp;");
                sb.append( content );
            }
        }
        else if ( node.getNodeType()==Node.ELEMENT_NODE )
        {
            Removal removal = removals.get(node.getNodeName());
            boolean strip = (removal==null)?false:removal.strip;
            if ( ((Element)node).hasAttribute(VERSIONS) )
            {
                String attr = ((Element)node).getAttribute(VERSIONS);
                versions = attr.split(" ");
            }
            if ( !strip )
                printElementStart( (Element)node, versions, map );
            printElementBody( (Element)node, versions, map );
            if ( !strip )
                printElementEnd( (Element)node, versions, map );
        }
        else if ( node.getNodeType()==Node.COMMENT_NODE )
        {
            for ( int i=0;i<versions.length;i++ )
            {
                StringBuilder sb = map.get(versions[i]);
                sb.append("<!--");
                sb.append(node.getTextContent());
                sb.append("-->");
            }
        }
    }
    /**
     * Split elements labelled with versions into separate versions
     * @param doc the XML version-labelled document to split
     * @return an array of split XML sub-documents
     */
    private Map<String,String> splitAll( Document doc )
    {
        String versions = root.getAttribute( VERSIONS );
        String[] parts = versions.split( " " );
        HashMap<String,StringBuilder>map = new HashMap<String,StringBuilder>();
        for ( int i=0;i<parts.length;i++ )
        {
            map.put( parts[i], new StringBuilder() );
        }
        printAll( root, parts, map );
        // now write out the results
        HashMap<String,String> versionMap = new HashMap<String,String>();
        for ( int i=0;i<parts.length;i++ )
        {
            StringBuilder sb = map.get( parts[i] );
            versionMap.put( parts[i], sb.toString() );
        }
        return versionMap;
    }
    /**
     * Split a TEI-XML file into versions of XML
     * @param tei the TEI file containing versions
     * @return an array of XML files as strings
     * @throws ImportException if something went wrong
     */
    public Map<String,String> split( String tei ) throws ImportException
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            if ( dbf.isExpandEntityReferences() )
                dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader( tei );
            InputSource is = new InputSource( sr );
            Document doc = db.parse( is ); 
            root = doc.getDocumentElement();
            root.setAttribute( VERSIONS, BASE );
            assignVersions( root, BASE );
            return splitAll( doc );
        }
        catch ( Exception e )
        {
            throw new ImportException( e );
        }
    }
    private static String readConfig( String fName ) throws IOException
    {
        File f = new File( fName );
        FileReader fr = new FileReader( f );
        char[] data = new char[(int)f.length()];
        fr.read( data );
        // use platform encoding - pretty simple
        return new String( data );
    }
    /** test and commandline utility */
    public static void main( String[] args )
    {
        if ( args.length>=1 )
        {
            try
            {
                int i=0;
                int fileIndex = 0;
                // see if the user supplied a conf file
                String textConf = defaultConf;
                while ( i<args.length )
                {
                    if ( args[i].equals("-c") && i < args.length-1 )
                    {
                        textConf = readConfig( args[i+1] );
                        i+=2;
                    }
                    else
                    {
                        fileIndex = i;
                        i++;
                    }
                }
                File f = new File( args[fileIndex] );
                char[] data = new char[(int)f.length()];
                FileReader fr = new FileReader( f );
                fr.read( data );
                JSONDocument config = JSONDocument.internalise(textConf);
                Splitter split = new Splitter( config );
                Map<String,String> map = split.split( new String(data) );
                Set<String>keys = map.keySet();
                String rawFileName = args[fileIndex];
                int pos = rawFileName.lastIndexOf(".");
                if ( pos != -1 )
                    rawFileName = rawFileName.substring(0,pos);
                Iterator<String> iter = keys.iterator();
                while ( iter.hasNext() )
                {
                    String key = iter.next();
                    String fName = rawFileName + "-"+key+".xml";
                    File g = new File( fName );
                    if ( g.exists() )
                        g.delete();
                    FileOutputStream fos = new FileOutputStream( g );
                    fos.write( map.get(key).getBytes("UTF-8") );
                    fos.close();
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace(System.out);
            }
        }
        else
            System.out.println(
                "usage: java -jar split.jar [-c json-config] <tei-xml>\n");
    }
}
