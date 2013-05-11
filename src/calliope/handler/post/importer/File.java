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
import org.xml.sax.InputSource;
import java.util.ArrayList;
import calliope.exception.AeseException;
import calliope.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Represent a loaded file
 * @author desmond
 */
public class File 
{
    public String data;
    public String name;
    boolean TEI;
    boolean TEICorpus;
    private char UTF8_BOM = 65279;
    public File( String name, String data )
    {
        this.name = name;
        this.data = data;
        if ( data.length()>0&&data.charAt(0)==UTF8_BOM )
            this.data = data.substring(1);
    }
    /**
     * Reset the data content
     * @param data the new data
     */
    public void setData( String data )
    {
        this.data = data;
    }
    public boolean isJSON()
    {
        return this.name.endsWith(".json");
    }
    private boolean isBom( char token )
    {
        return token == UTF8_BOM;
    }
    /**
     * Is this file a TEI Corpus (a collection of TEI files)?
     * @return true if it is
     */
    public boolean isTEICorpus()
    {
        return TEICorpus;
    }
    /**
     * Get the name of the file minus extension
     * @return the simple file name
     */
    public String simpleName()
    {
        int dotPos = name.lastIndexOf(".");
        if ( dotPos != -1 )
            return name.substring( 0,dotPos );
        else
            return name;
    }
    /**
     * Look for a node called "title" and return its content
     * @param node the node to search from
     * @return the title content
     */
    String getDocTitle( Node node )
    {
        String title = null;
        Node sibling = node;
        do
        {
            if ( sibling.getNodeType()==Node.ELEMENT_NODE )
            {
                if ( sibling.getNodeName().equals("title"))
                {
                    title = sibling.getTextContent();
                    break;
                }
                else    // descend
                {
                    Node child = sibling.getFirstChild();
                    if ( child != null )
                    {
                        title = getDocTitle( child );
                        if ( title != null )
                            break;
                    }
                }
            }
            sibling = sibling.getNextSibling();
        } while ( sibling != null );
        return title;
    }
    /**
     * Split a teiCorpus into a set of TEI documents
     * @return an array of Files
     * @throws AeseException 
     */
    public File[] splitTEICorpus()
    {
        ArrayList<File> splits = new ArrayList<File>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try 
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader( data );
            InputSource is = new InputSource( sr );
            Document dom = db.parse( is );
            Element root = dom.getDocumentElement();
            if ( root.getNodeName().equals("teiCorpus") )
            {
                Node child = root.getFirstChild();
                while ( child != null )
                {
                    if ( child.getNodeType()==Node.ELEMENT_NODE ) 
                    {
                        if ( child.getNodeName().equals("TEI") )
                        {
                            Document doc = db.newDocument();
                            Node newRoot = doc.importNode( child, true );
                            String title = getDocTitle( child.getFirstChild() );
                            doc.appendChild( newRoot );
                            String subDoc = Utils.docToString( doc );
                            File g = new File( title, subDoc );
                            splits.add( g );
                        }
                    }
                    child = child.getNextSibling();
                }
            }
            File[] array = new File[splits.size()];
            return splits.toArray( array );
        }
        catch ( Exception e )
        {
            System.out.println("couldn't split teiCorpus "+this.name);
            File[] files = new File[1];
            files[0] = this;
            return files;
        }
    }
    /**
     * Is this an XML file?
     * @param log the log to write error messages to
     * @return true if it is
     */
    public boolean isXML( StringBuilder log )
    {
        boolean isXML = false;
        for ( int i=0;i<data.length();i++ )
        {
            char token = data.charAt(i);
            if ( !Character.isWhitespace(token) )
            {
                if ( token == '<' )
                    isXML = true;
                break;
            }
        }
        // Hmmm. Looks like XML, smells like XML. Let's check to be sure...
        // and also check if it is TEI-XML...
        if ( isXML )
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try 
            {
                DocumentBuilder db = dbf.newDocumentBuilder();
                StringReader sr = new StringReader( data );
                InputSource is = new InputSource( sr );
                Document dom = db.parse( is );
                Element root = dom.getDocumentElement();
                if ( root == null )
                    throw new AeseException("No root element");
                String name = root.getNodeName();
                if ( name != null 
                    && (name.equals("TEI")||name.equals("TEI.2")) )
                    TEI = true;
                if ( name.equals("teiCorpus") )
                {
                    TEI = true;
                    TEICorpus = true;
                }
            }
            catch ( Exception e ) 
            {
                isXML = false;
                log.append( e.getMessage() );
                log.append("\n");
            }
        }
        return isXML;
    }
    /**
     * Is this XML file in TEI format? (should be preceded by isXML call)
     * @return true if it is 
     */
    public boolean isTEI()
    {
        return TEI;
    }
    @Override
    public String toString()
    {
        return data;
    }
}
