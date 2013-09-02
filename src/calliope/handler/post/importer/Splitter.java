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
 * <p>Split TEI-XML into versions if it contains any alternatives
 * TEI files contain variant-recording elements such as add, del,
 * expan, abbr, orig, reg, lem, rdg. These follow two patterns:</p>
 * <ol><li>a list of alternatives, often 2 in length</li>
 * <li>nested alternatives</li></ol>
 * <p>In case 1) there may be several alternatives, such as in app,rdg
 * Each alternative except the last is "frozen", it has the declared
 * or implied version and no others. This holds if it is rdg and it has a 
 * multiple-witness list OR if it has no witlist and it is just rdg1, rdg2 
 * in a correction sequence. In this case rdg1 and rdg2 etc have just
 * those versions and NO others. BUT the final version is open to new versions.
 * In the case of del followed by add the add is also visible in rdg2, rdg3.
 * And rdg3 is visible in rdg4 if rdg3 is last in its app structure and 
 * there is a larger app structure elsewhere in the file. BUT that rdg3 may 
 * NOT acquire the rdg2 of the other app, only rdg3 and rdg4. So the 
 * extendible class allows elements to acquire new versions and the frozen
 * elements (not in extendibles) can't.</p>
 * <p>So the algorithm's steps are: 
 * <ol><li>prepare all elements by classifying them as extendible or not</li>
 * <li>assign versions to all elements by discovering "siblings". The last 
 * sibling in each sequence is left open, but prevented from acquiring its 
 * elder sibling's versions. Also a frozen parent freezes its children. For 
 * nested alternatives this means that a sibling can only take on the versions 
 * of its parent.</li>
 * <li>split the file into layers based on the assigned versions</li>
 * <li>print out all the versions one by one</li></ol></p>
 * @author desmond
 */
public class Splitter 
{
    private static String REMOVALS = "removals";
    private static String SIBLINGS = "siblings";
    private static String FROZEN = "frozen";
    private static String EQUATES = "equates";
    private static String FIRST = "first";
    private static String SECOND = "second";
    private static String VERSIONS = "_versions";
    private static String WITS = "wits";
    private static String BASE = "base";
    private HashMap<String,Sibling> siblings;
    private HashSet<String> removals;
    private HashMap<String,String> equates;
    private HashMap<Element,Extendible> extendibles;
    private HashSet<String> versions;
    /** list of freezable element names */
    ArrayList<String> freezes;
    ArrayList<String> exts;
    static String defaultConf = 
    "{ \"siblings\": [ {:first\":\"add\", \"second\": \"del\"}, {\"first\": "
    +"\"abbr\", \"second\": \"expan\"}, {\"first\": \"orig\", \"second\": "
    +"\"reg\"}, {\"first\": \"sic\", \"second\": \"corr\"}, {\"first\": "
    +"\"lem\", \"second\": \"rdg\", \"wits\": \"wits\"},], \"removals\": "
    +"[\"app\",\"rdgGrp\",\"subst\",\"mod\",\"choice\",\"rdg\",\"lem\"],"
    +"\"frozen\": [\"rdg\",\"lem\",\"del\"] }";
    Element root;
    /**
     * Initialise a splitter from its JSON config file
     * @param config a JSON document from the config database
     */
    public Splitter( JSONDocument config ) throws ConfigException
    {
        ArrayList sets = (ArrayList)config.get( REMOVALS );
        removals = new HashSet<String>();
        if ( sets != null )
        {
            for ( int i=0;i<sets.size();i++ )
            {
                String r = (String)sets.get( i );
                removals.add( r );
            }
        }
        versions = new HashSet<String>();
        freezes = (ArrayList)config.get( FROZEN );
        extendibles = new HashMap<Element,Extendible>();
        ArrayList brothers = (ArrayList)config.get( SIBLINGS );
        if ( brothers == null )
            throw new ConfigException("invalid splitter config file");
        siblings = new HashMap<String,Sibling>();
        for ( int i=0;i<brothers.size();i++ )
        {
            JSONDocument doc = (JSONDocument)brothers.get( i );
            String first = (String)doc.get( FIRST );
            String second = (String)doc.get( SECOND );
            String wits = (String)doc.get( WITS );
            if ( first!=null&&second!=null )
            {
                siblings.put( first,new Sibling(first,second,wits) );
                siblings.put( second,new Sibling(second,first,wits) );
            }
        }
        ArrayList equivalents = (ArrayList)config.get( EQUATES );
        equates = new HashMap<String,String>();
        if ( equivalents != null )
        {
            for ( int i=0;i<equivalents.size();i++ )
            {
                JSONDocument doc = (JSONDocument)equivalents.get( i );
                String first = (String)doc.get( FIRST );
                String second = (String)doc.get( SECOND );
                if ( first!=null&&second!=null )
                {
                    equates.put( first,second );
                    equates.put( second, first );
                }
            }
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
        if ( oldV.length()==0 )
            return newV;
        else if ( newV.length()==0 )
            return oldV;
        else
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
    }
    /**
     * Is the next element at this level the same or a sibling type?
     * @param elem the element in a series of siblings
     * @return true if it is the last of its kind
     */
    boolean isLastSibling( Element elem )
    {
        Element sibling = nextSibling( elem, false );
        if ( sibling != null )
        {
            String sName = sibling.getNodeName();
            String elemName = elem.getNodeName();
            if ( elemName.equals(sName) )
                return false;
            else if ( siblings.containsKey(elemName) )
            {
                String brother = siblings.get(elemName).getSibling();
                return !brother.equals(sName);
            }
        }
        else
        {
            Element prev = prevSibling( elem, false );
            if ( prev != null )
            {
                String prevName = prev.getNodeName();
                String brother = siblings.get(prevName).getName();
                return brother.equals(prevName)||prevName.equals(brother);
            }
        }
        return false;
    }
    /**
     * Exclude the co-wits of co-witnesses in the equates set
     * @param excluded a list of already excluded wits
     * @return an expanded excluded set
     */
    private String excludeCoWits( String excluded )
    {
        StringBuilder sb = new StringBuilder();
        String[] exts = excluded.split(" ");
        for ( int i=0;i<exts.length;i++ )
        {
            if ( equates.containsKey(exts[i]) )
            {
                String coWit = equates.get(exts[i]);
                if ( excluded.indexOf(coWit)==-1 )
                {
                    if ( sb.length()>0 )
                        sb.append( " ");
                    sb.append( coWit );
                    sb.append(" ");
                    sb.append( exts[i] );
                }
            }
            else
            {
                if ( sb.length()>0 )
                    sb.append( " ");
                sb.append( exts[i] );
            }
        }
        return sb.toString();
    }
    /**
     * Add all non-frozen elements to the extendibles set
     * @param elem the element to start from
     * @param pos the index of the freezable element
     */
    private void prepare( Element elem, int pos )
    {
        String name = elem.getNodeName();
        Node parent = elem.getParentNode();
        Element pElem = null;
        if ( parent instanceof Element )
            pElem = (Element)parent;
        if ( !freezes.contains(name)
            &&(pElem==null||extendibles.containsKey(pElem)) )
        {
            extendibles.put( elem, new Extendible() );
        }
        else 
        {
            if ( isLastSibling(elem) )
            {
                Extendible ext = new Extendible();
                String siblingWits = siblingWits(elem);
                String newWits = excludeCoWits( siblingWits );
                ext.exclude(newWits);
                extendibles.put( elem, ext );
            }
            else
            {
                String wit = name+pos;
                if ( equates.containsKey(wit) )
                {
                    Extendible ext = new Extendible();
                    ext.include( equates.get(wit) );
                    ext.include( wit );
                    extendibles.put( elem, ext );
                }
            }
        }
        Element child = firstChild( elem );
        if ( child != null )
            prepare( child, elem.getNodeName().equals(child.getNodeName())?pos+1:0 );
        Element sibling = nextSibling(elem,true);
        if ( sibling != null )
        {
            Node prev = prevSibling(sibling,false);
            boolean inc = false;
            if ( prev != null )
            {
                inc = prev.getNodeType()==Node.ELEMENT_NODE
                &&prev.getNodeName().equals(sibling.getNodeName());
            }
            prepare( sibling, inc?pos+1:0 );
        }
    }
    /**
     * Assign new versions to all extendible elements
     * @param versions the versions of the start node
     */
    private void percolate( Element child, String vstring )
    {
        Set<Element> keys = extendibles.keySet();
        Iterator<Element> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            Element elem = iter.next();
            addVersion( elem, vstring );
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
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                //if ( child.getNodeName().equals("emph") )
                //    System.out.println("emph");
                String vAttr = ((Element)child).getAttribute(VERSIONS);
                if ( vAttr.length()>0 )
                {
                    String[] cVersions = vAttr.split(" ");
                    printAll( child, cVersions, map );
                }
                else    // shouldn't happen
                    printAll( child, versions, map );
            }
            else
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
     * Get the next sibling that is an element
     * @param elem the element
     * @param skipText if true skip text nodes to next element node
     * @return its next sibling of elem or null
     */
    Element nextSibling( Element elem, boolean skipText )
    {
        Node n = elem.getNextSibling();
        while ( n != null )
        {
            if ( !skipText 
                && n.getNodeType() == Node.TEXT_NODE 
                && !isWhitespace(n.getTextContent()) )
                    return null;
            else if ( n.getNodeType()==Node.ELEMENT_NODE )
                return (Element)n;
            n = n.getNextSibling();
        }
        return null;
    }
    /**
     * Is a string nothing by white space?
     * @param str the string in question
     * @return true if that is all it is
     */
    private boolean isWhitespace( String str )
    {
        for ( int i=0;i<str.length();i++ )
            if ( !Character.isWhitespace(str.charAt(i)) )
                return false;
        return true;
    }
    /**
     * Get the previous sibling that is an element
     * @param elem the element
     * @param skipText if true skip text nodes to next element node
     * @return its next sibling of elem or null
     */
    Element prevSibling( Element elem, boolean skipText )
    {
        // this should return null if elem is preceded by a non-whitespace text node
        Node n = elem.getPreviousSibling();
        while ( n != null )
        {
            if ( !skipText 
                && n.getNodeType() == Node.TEXT_NODE 
                && !isWhitespace(n.getTextContent()) )
                    return null;
            else if ( n.getNodeType()==Node.ELEMENT_NODE )
                return (Element)n;
            n = n.getPreviousSibling();
        }
        return null;
    }
    /**
     * Get the next child that is an element
     * @param elem the element
     * @return its first child of elem or null
     */
    Element firstChild( Element elem )
    {
        Node n = elem.getFirstChild();
        //if ( n.getNodeName().equals("emph") )
        //    System.out.println("emph");
        while ( n != null && n.getNodeType()!=Node.ELEMENT_NODE )
            n = n.getNextSibling();
        return (Element) n;
    }
    /**
     * Add a new version to the current element
     * @param elem the element to add the new version to
     * @param wits the new version
     */
    private void addVersion( Element elem, String wits )
    {
        Extendible ext = extendibles.get( elem );
        if ( ext != null )
            wits = ext.allows(wits);
        if ( elem.hasAttribute(VERSIONS) )
            elem.setAttribute( VERSIONS, 
                mergeVersions(elem.getAttribute(VERSIONS),wits));
        else
            elem.setAttribute( VERSIONS, wits );
    }
    /**
     * Get the combined witnesses of the previous siblings
     * @param elem the element to get the sibling witlist from
     * @return the witlist of the preceding siblings of elem or empty string
     */
    String siblingWits( Element elem )
    {
        StringBuilder sb = new StringBuilder();
        Element prev = elem;
        // count previous siblings
        int nSibs = 0;
        do
        {
            prev = prevSibling( prev, false );
            if ( prev !=null )
            {
                String prevName = prev.getNodeName();
                if ( prevName.equals(elem.getNodeName()) )
                    nSibs++;
                else
                    break;
            }
            else
                break;
        } while (true);
        // compose wit list
        String elemName = elem.getNodeName();
        for ( int i=nSibs-1;i>0;i-- )
        {
            String wit = elemName+i;
            if ( sb.length()>0 )
                sb.append(" ");
            sb.append( wit );
        }
        return sb.toString();
    }
    /**
     * Assign versions to an element by recursing ACROSS and DOWN the tree
     * @param elem the element in question
     * @param versions the versions we start with
     * @param pos position in a sequence of siblings, starting with 0
     */
    void assignVersions( Element elem, String vstr, int pos )
    {
        String name = elem.getNodeName();
        if ( siblings.containsKey(name) )
        {
            Sibling s = siblings.get(name);
            String witsAttr = s.getWits();
            String wits = name+pos;
            if (witsAttr!=null
                &&elem.getAttribute(witsAttr)!=null
                &&elem.getAttribute(witsAttr).length()>0)
                wits = elem.getAttribute(witsAttr);
            if ( extendibles.containsKey(elem)|| pos > 0 )
            {              
                addVersion( elem, wits );
                // ensure all other elements who can have this, do
                if ( !versions.contains(wits) )
                {
                    percolate( elem, wits );
                    versions.add( wits );
                }
            }
            else if ( equates.containsKey(wits) )
            {
                // if it is equated to something else define 
                // an extendible with an INCLUDE set equal to that
                String coWit = equates.get(wits);
                Extendible ext = new Extendible();
                ext.include( coWit );
                extendibles.put( elem, ext );
            }
            else
                addVersion( elem, vstr );
            // pass on control to an elementary sibling
            Element n = nextSibling( elem, true );
            if ( n != null )
            {
                String nName = n.getNodeName();
                if ( s.getName().equals(nName)
                    ||name.equals(nName) )
                    assignVersions( n, vstr, pos+1 );
                else
                    assignVersions( n, vstr, 0 );
            }
        }
        else
        {
            if ( extendibles.containsKey(elem) )
                addVersion( elem, vstr );
            // recurse to an elementary child
            Node n = firstChild( elem );
            if ( n != null )
                assignVersions( (Element)n, vstr, 0 );
            // pass on control to an elementary sibling
            n = nextSibling( elem, true );
            if ( n != null )
                assignVersions( (Element)n, vstr, 0 );
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
                if ( content.equals("grimly") )
                    System.out.println("grimly");
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
            String str = sb.toString();
            System.out.println(str);
            versionMap.put( parts[i], str );
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
            prepare( root, 0 );
            assignVersions( root, BASE, 0 );
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
