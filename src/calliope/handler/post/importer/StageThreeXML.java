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
import calliope.importer.Archive;
import calliope.exception.ImportException;
import calliope.json.JSONDocument;
import calliope.json.JSONResponse;
import calliope.AeseStripper;
import calliope.AeseTransformer;
import calliope.Utils;
import calliope.constants.Formats;
import calliope.constants.Globals;
import calliope.exception.AeseException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Process the XML files for import
 * @author desmond
 */
public class StageThreeXML extends Stage
{
    String stripConfig;
    String splitConfig;
    String style;
    String xslt;
    String dict;
    String hhExcepts;
    boolean hasTEI;
    static HashMap<String,String> humanKeyMap;
    static
    {
        humanKeyMap = new HashMap<String,String>();
        humanKeyMap.put("add0","level 1 addition");
        humanKeyMap.put("del1","level 2 deletion");
        humanKeyMap.put("add1","level 2 addition");
    }
    public StageThreeXML()
    {
        super();
        this.dict = Globals.DEFAULT_DICT;
    }
    public StageThreeXML( String style, String dict, String hhExcepts )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
    }
    /**
     * 
     * @param last
     * @param style
     * @param dict name of dictionary e.g. en_GB
     * @param hhExcepts hard hyphen exceptions list (space delimited)
     */
    public StageThreeXML( Stage last, String style, String dict, 
        String hhExcepts )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
        for ( int i=0;i<last.files.size();i++ )
        {
            File f = last.files.get( i );
            if ( f.isXML(log) )
            {
                if ( f.isTEI() )
                    hasTEI = true;
                if ( f.isTEICorpus() )
                {
                    File[] members = f.splitTEICorpus();
                    for ( int j=0;j<members.length;j++ )
                        this.files.add( members[j] );
                }
                else
                    this.files.add( f );
            }
            // irrelvant files already exceluded by stage 1
//            else
//            {
//                log.append( "excluding from XML set ");
//                log.append( f.name );
//                log.append(", not being valid XML\n" );
//            }
        }
    }               
    /**
     * Does this stage3 have at least ONE TEI file?
     * @return true if it does
     */
    public boolean hasTEI()
    {
        return hasTEI;
    }
    /**
     * Set the XSLT stylesheet
     * @param xslt the XSLT transform stylesheet (XML)
     */
    public void setTransform( String xslt )
    {
        this.xslt = xslt;
    }
    /**
     * Set the stripping recipe for the XML filter
     * @param config a json document from the database
     */
    public void setStripConfig( String config )
    {
        this.stripConfig = config;
    }
    /**
     * Set the splitting recipe for the XML filter
     * @param config a json document from the database
     */
    public void setSplitConfig( String config )
    {
        this.splitConfig = config;
    }
    /**
     * Convert ordinary quotes into curly ones
     * @param a char array containing the unicode text
     */
    void convertQuotes( char[] chars )
    {
        char prev = 0;
        for ( int i=0;i<chars.length;i++ )
        {
            if ( chars[i]==39 )    // single quote, straight
            {
                if ( Character.isWhitespace(prev)
                    ||Character.getType(prev)==21
                    ||Character.getType(prev)==29 )
                    chars[i] = '‘';
                else
                    chars[i] = '’';
            }
            else if ( chars[i]==34 )   // double quote, straight
            {
                if ( Character.isWhitespace(prev)
                    ||Character.getType(prev)==21
                    ||Character.getType(prev)==29 )
                    chars[i] = '“';
                else
                    chars[i]='”';
            }
            prev = chars[i];
        }
    }
    /**
     * Append a note or interp to the notes document
     * @param notes the notes document
     * @param notesElem the element in notes to append to
     * @param node the actual element to append
     */
    private void addToNotes( Document notes, Node notesElem, Node node )
	{
		notesElem.appendChild( notes.createTextNode("\n") );
		notesElem.appendChild( node );		
	}
	/**
     * Recursively search through the DOM for note, interp and interpGrp
     * @param src the source document 
     * @param node the source node to start searching from
     * @param notes the notes document 
     * @param notesElem the top level element (not root) to append notes to
     */
    private void lookForNotes( Document src, Node node, Document notes, 
        Node notesElem )
    {
        ArrayList<Node> delenda = new ArrayList<Node>();
        NodeList nl = node.getChildNodes();
        for ( int i=0;i<nl.getLength();i++ )
        {
            Node n = nl.item(i);
            if ( n.getNodeType()==Node.ELEMENT_NODE )
            {
                String nName = n.getNodeName();
                if ( nName.equals("interp") )
                {
                    Node interp = notes.importNode( n, true );
					addToNotes( notes, notesElem, interp );
					delenda.add( n );
                }
                else if ( nName.equals("interpGrp") )
                {
                    NodeList nl2 = n.getChildNodes();
                    for ( int j=0;j<nl2.getLength();j++ )
                    {
                        // for each interp child
                        Node child = nl.item(j);
                        if ( child.getNodeType()==Node.ELEMENT_NODE 
                            && child.getNodeName().equals("interp") )
                        {
                            // to each interp add interpGrp's attributes
                            Element e = (Element)n;
                            NamedNodeMap attrs = e.getAttributes();
                            for ( int k=0;k<attrs.getLength();k++ )
                            {
                                Node attr = attrs.item(k);
                                ((Element)child).setAttribute( 
                                    attr.getNodeName(), 
                                    attr.getTextContent() );
                            }
                            String id = e.getAttribute("xml:id");
                            // to do: adjust id to point to document
                            // copy interp child to the notes document
                            Node interp = notes.importNode( child, true );
							addToNotes( notes, notesElem, interp );
                        }
                    }
                    // delete the interpGrp and its interp children from src
					delenda.add(n);
                }
                else if ( nName.equals("note") )
                {
                    Element anchor = src.createElement("anchor");
                    String id = "I"+UUID.randomUUID().toString();
                    anchor.setAttribute("ana", "#"+id );
                    ((Element)n).setAttribute("xml:id", id );
                    Node next = n.getNextSibling();
                    if ( next != null )
                        n.getParentNode().insertBefore(anchor,next);
                    else
                        n.getParentNode().appendChild(anchor);
                    Node note = notes.importNode( n, true );
					addToNotes(notes,notesElem,note);
					delenda.add( n );
                }
                else
                    lookForNotes(src,n,notes,notesElem);
            }
        }
        // now delete outside of loop
        for ( int i=0;i<delenda.size();i++ )
		{
			Node n = delenda.get( i );
			n.getParentNode().removeChild( n );
		}
    }
    /**
     * Remove note, interp and interpGrp from TEI-Lite documents
     * @param doc the doc to remove them from
     * @return a TEI document containing the removed notes etc.
     * @throws AeseException 
     */
    private Document separateNotes( Document doc ) throws Exception
    {
        Element root = doc.getDocumentElement();
        Document notes = null;
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            notes = db.newDocument();
            // not a real TEI document
            Element notesRoot = notes.createElement("TEI");
            notes.appendChild( notesRoot );
            Element textElem = notes.createElement("text");
            notesRoot.appendChild( textElem );
            Element bodyElem = notes.createElement("body");
            textElem.appendChild( bodyElem );
            lookForNotes( doc, root, notes, bodyElem );
            return notes;
        }
        catch ( Exception e )
        {
            throw new Exception( e );
        }
    }
    /**
     * Get the set of files that are TEI files containing note-like elements
     * @return a set of files containing notes etc
     */
    public ArrayList<File> getNotes() throws AeseException
    {
        ArrayList<File> notes = new ArrayList<File>();
        for ( int i=0;i<files.size();i++ )
        {
            File f = files.get(i);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try 
            {
                DocumentBuilder db = dbf.newDocumentBuilder();
                StringReader sr = new StringReader( f.toString() );
                InputSource is = new InputSource( sr );
                XPathFactory factory = XPathFactory.newInstance();
                XPath xp = factory.newXPath();
                XPathExpression note = xp.compile("//note|//interp|//interpGrp");
                String noteRes = note.evaluate( is );
                if ( noteRes != null && noteRes.length()>0 )
                {
                    sr = new StringReader( f.toString() );
                    is = new InputSource( sr );
                    Document doc = db.parse( is );
                    log.append("Separating notes in ");
                    log.append(f.name);
                    log.append("\n");
                    Document notesDoc = separateNotes( doc );
                    f.setData( Utils.docToString(doc) );
                    File g = new File(f.simpleName()+"-notes",
                        Utils.docToString(notesDoc) );
                    notes.add( g );
                }
            }
            catch ( Exception e ) 
            {
                throw new AeseException( e );
            }
        }
        return notes;
    }
    /** 
     * Convert the cryptic "add0" "del1" names into something intelligible
     * @param key the cryptic key
     * @return the human-readable version
     */
    private String translateKey( String key )
    {
        if ( humanKeyMap.containsKey(key) )
            return humanKeyMap.get(key);
        else
            return key;
    }
    /**
     * Process the files
     * @param cortex the cortext MVD to accumulate files into
     * @param corcode the corcode MVD to accumulate files into
     * @return the log output
     */
    @Override
    public String process( Archive cortex, Archive corcode ) 
        throws ImportException
    {
        try
        {
            if ( files.size() > 0 )
            {
                JSONDocument jDoc = JSONDocument.internalise( splitConfig );
                Splitter splitter = new Splitter( jDoc );
                for ( int i=0;i<files.size();i++ )
                {
                    // optional transform
                    File file = files.get(i);
                    String fileText = file.toString();
                    if ( xslt != null )
                        fileText = new AeseTransformer().transform(xslt,fileText);
                    long startTime = System.currentTimeMillis();
                    Map<String,String> map = splitter.split( fileText );
                    long diff = System.currentTimeMillis()-startTime;
                    log.append("Split ");
                    log.append( file.name );
                    log.append(" in " );
                    log.append( diff );
                    log.append( " milliseconds into " );
                    log.append( map.size() );
                    log.append( " versions\n" );
                    Set<String> keys = map.keySet();
                    Iterator<String> iter = keys.iterator();
                    while ( iter.hasNext() )
                    {
                        String key = iter.next();
                        JSONResponse markup = new JSONResponse(JSONResponse.STIL);
                        JSONResponse text = new JSONResponse(JSONResponse.TEXT);
                        AeseStripper stripper = new AeseStripper();
                        String xml = map.get(key);
                        int res = stripper.strip( xml, stripConfig, 
                            Formats.STIL, style, dict, hhExcepts, 
                            Utils.isHtml(xml), text, markup );
                        if ( res == 1 )
                        {
                            String vid = "Base/";
                            vid += stripSuffix(files.get(i).name);
                            if ( map.size()>1 )
                                vid += "/"+translateKey(key);
                            //char[] chars = text.getBody().toCharArray();
                            //convertQuotes( chars );
                            //cortex.put( group+key, new String(chars).getBytes("UTF-8") );
                            cortex.put( vid, text.getBody().getBytes("UTF-8") );
                            corcode.put( vid, markup.getBody().getBytes("UTF-8") );
                            log.append( "Stripped " );
                            log.append( file.name );
                            log.append("(");
                            log.append( key );
                            log.append(")");
                            log.append(" successfully\n" );
                        }
                        else
                        {
                            throw new ImportException("Stripping of "
                                +files.get(i).name+" XML failed");
                        }
                    }
                }
            }
        }
        catch ( Exception e ) 
        {
            if ( e instanceof ImportException )
                throw (ImportException)e;
            else
                throw new ImportException( e );
        }
        return log.toString();
    }
}
