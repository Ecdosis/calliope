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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
 * expan, abbr, orig, reg, lem, rdg. For documentation see 
 * http://multiversiondocs.blogspot.com.au/2013/09/splitting-tei-versions-reliably.html
 * @author desmond
 */
public class Splitter 
{
    static String VERSIONS = "_versions";
    static String DONE = "_done";
    static String DEFAULT = "_default";
    static String FINAL = "_final";
    static String BASE = "base";
    static String ORIGINAL = "_original";
    Element root;
    Discriminator discriminator;
    /**
     * Initialise a splitter from its JSON config file
     * @param config a JSON document from the config database
     */
    public Splitter( JSONDocument config ) throws ConfigException
    {
        discriminator = new Discriminator( config );    
    }
    /**
     * Locate clusters of siblings and their children
     * @param elem the element to start from, already marked
     * @param pos the registry of the cluster
     */
    private void prepare( Element elem, Cluster pos )
    {
        if ( discriminator.isSibling(elem) )
        {
            String eName = elem.getNodeName();
            pos.inc(eName,elem);
            String pName = pos.getName(eName);
            discriminator.addVersion( elem, pName );
        }
        // descend depth-first
        Element child = Discriminator.firstChild( elem );
        if ( child != null )
        {
            if ( pos.size()>0 )
                pos.descend();
             // recurse down
            prepare( child, pos );
        }
        // try to go sideways
        Element sibling = Discriminator.nextSibling(elem,true);
        if ( sibling != null )
        {
            Element next = discriminator.nextTrueSibling(elem);
            if ( next != null )
            {
                prepare( next, pos );
            }
            else if ( pos.ripe() )
            {
                pos.percolateUp(this);
                prepare( sibling, new Cluster(discriminator) );
            }
            else
                prepare( sibling, pos );
        }
        // there may be no more true siblings here also
        else if ( pos.ripe() )
            pos.percolateUp(this);
        else
            pos.ascend();
    }
    /**
     * Add a custom attribute signalling that this node's versions are "done"
     * @param elem the element to mark thus
     */
    private void addDoneTag( Element elem )
    {
        elem.setAttribute(DONE,DONE);
    }
    /**
     * Test if this element's versions are "done"
     * @param elem the element to test
     * @return true if no more versions can be added else false
     */
    private boolean isDone( Element elem )
    {
        String attr = elem.getAttribute(DONE);
        return attr != null && attr.length()>0;
    }
    /**
     * Convert a hashset of versions to a string
     * @param set the set of version names
     * @return a simple space-delimited string
     */
    private String hashsetToString( HashSet<String> set )
    {
        StringBuilder sb = new StringBuilder();
        if ( set.size()>0 )
        {
            sb.append(" ");
            Iterator<String> iter = set.iterator();
            while ( iter.hasNext() )
            {
                sb.append( iter.next());
                if ( iter.hasNext() )
                    sb.append( " " );
            }
        }
        return  sb.toString();
    }
    /**
     * Percolate the versions accumulated in root to suitable sub-elements
     * @param elem the start node with its versions to percolate
     */
    private void percolateDown( Element elem )
    {
        Node parent = elem.getParentNode();
        if ( parent != null && parent.getNodeType()==Node.ELEMENT_NODE )
        {
            String vers = ((Element)parent).getAttribute(VERSIONS);
            if ( vers !=null&&vers.length()>0 )
            {
                if ( !discriminator.isSibling(elem) )
                {
                    Discriminator.addVersion( elem, vers );
                    addDoneTag( elem );
                }
                else if ( elem.hasAttribute(FINAL) )
                {
                    String fVers = elem.getAttribute(FINAL);
                    if ( fVers != null && fVers.length()>0 )
                    {
                        // find inverse versions
                        HashSet<String> invVers = new HashSet<String>();
                        String[] parts = vers.split(" ");
                        String[] iparts = fVers.split(" ");
                        for ( int i=0;i<parts.length;i++ )
                            if ( /*!parts[i].startsWith(DEL) 
                                &&*/ !parts[i].equals(BASE) ) 
                                invVers.add( parts[i] );
                        for ( int i=0;i<iparts.length;i++ )
                            if ( invVers.contains(iparts[i]))
                                invVers.remove(iparts[i]);
                        String newVers = hashsetToString(invVers);
                        Discriminator.addVersion( elem, newVers );
                        addDoneTag( elem );
                        Element lastOChild = discriminator.lastOpenChild( elem );
                        while ( lastOChild != null )
                        {
                            Discriminator.addVersion( lastOChild, newVers );
                            lastOChild = discriminator.lastOpenChild( lastOChild );
                        }
                    }
                }
                // else ignore it
            }
        }
        // now examine the children of elem
        Element child = Discriminator.firstChild( elem );
        while ( child != null && !isDone(child) )
        {
            percolateDown( child );
            child = Discriminator.firstChild( child );
        }
        // finall the siblings of elem
        Element brother = Discriminator.nextSibling( elem, true );
        while ( brother != null )
        {
            if ( !isDone(brother) )
                percolateDown( brother );
            brother = Discriminator.nextSibling( brother, true );
        }
    }
    boolean verifyMembership( String pVers, String cVers )
    {
        boolean result = true;
        String[] parts = cVers.split(" ");
        for ( int i=0;i<parts.length;i++ )
        {
            if ( !pVers.contains(parts[i]) )
            {
                result = false;
                break;
            }
        }
        return result;
    }
    /**
     * Rule 1 states that the versions of every element are also 
     * present in its parents, up to the root.
     * @param elem the element to start from
     */
    void verifyRule1( Element elem )
    {
        String pVers = elem.getAttribute( VERSIONS );
        Node child = elem.getFirstChild();
        // descend depth-first
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                String cVers = ((Element)child).getAttribute(VERSIONS);
                if ( cVers != null && pVers != null )
                {
                    if ( !verifyMembership(pVers,cVers) )
                        System.out.println("Rule 1 broken for element "
                            +elem.getNodeName()+" with child "
                            +child.getNodeName()+": \""+cVers
                            +"\" not contained in \""+pVers+"\"");
                    // recurse
                    verifyRule1( (Element)child );
                }
                else
                    System.out.println("Missing parent ("
                        +pVers+") or child versions ("+cVers+")");
            }
            child = child.getNextSibling();
        }
    }
    /**
     * Split a TEI-XML file into versions of XML
     * @param tei the TEI file containing versions
     * @return a map of version names to XML files as strings
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
            prepare( root, new Cluster(discriminator) );
            percolateDown( root );
            //verifyRule1( root );
            return XMLPrinter.splitAll( doc, discriminator.drops, 
                discriminator.removals );
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
                String textConf = Discriminator.defaultConf;
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
