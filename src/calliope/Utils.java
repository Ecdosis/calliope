/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope;
import calliope.constants.Database;
import calliope.constants.Formats;
import calliope.constants.JSONKeys;
import calliope.exception.AeseException;
import calliope.json.JSONDocument;
import calliope.path.Path;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Document;
import java.io.StringWriter;
import java.util.Locale;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
/**
 * Things accessible form everywhere
 * @author desmond
 */
public class Utils 
{
    /**
     * Join two paths together with a single slash
     * @param part1 the first path perhaps ending in a slash
     * @param part2 the second path perhaps starting with a slash
     * @return a single-slash joined version of path1 and path2
     */
    public static String canonisePath( String part1, String part2 )
    {
        if ( part1.length()==0 )
            return part2;
        else if ( part1.endsWith("/") )
            if ( part2.startsWith("/") )
                return part1+part2.substring(1);
            else
                return part1+"/"+part2;
        else if ( part2.startsWith("/") )
            return part1+part2;
        else
            return part1+"/"+part2;
    }
    /**
     * Separate the group from the full path
     * @param path the path to split
     */
    public static String getGroupName( String path )
    {
        int index = path.lastIndexOf("/");
        if ( index == -1 )
            return "";
        else
            return path.substring( 0, index );
    }
    /**
     * Separate the short name from the full path
     * @param path the path to split
     */
    public static String getShortName( String path )
    {
        int index = path.lastIndexOf("/");
        if ( index == -1 )
            return path;
        else
            return path.substring( index+1 );
    }
    public static String escape( String value )
    {
        StringBuilder sb = new StringBuilder();
        {
            for ( int i=0;i<value.length();i++ )
            if ( value.charAt(i) == ' ' )
                sb.append("%20");
            //else if ( value.charAt(i) == '/' )
            //   sb.append("%2F");
            else
                sb.append( value.charAt(i) );
        }
        return sb.toString();
    }
    /**
     * Look for and get the html contained in the body element
     * @return if found, the HTML body else the original html 
     */
    public static String getHTMLBody( String html )
    {
        // find start of text after "<body>"
        int pos = html.indexOf("<body");
        if ( pos == -1 )
            pos = html.indexOf("<BODY");
        if ( pos != -1 )
            pos = html.indexOf(">",pos);
        if ( pos != -1 )
        {
            pos++;
            int rpos = html.indexOf("</body>");
            if ( rpos == -1 )
                rpos = html.indexOf("</BODY>");
            if ( rpos != -1 )
                return html.substring(pos,rpos);
        }
        return html;
    }
    /**
     * Print a single element node
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to buffers
     */
    static void printElementBody( Element elem, StringWriter sw )
    {
        // recurse into its children
        Node child = elem.getFirstChild();
        while ( child != null )
        {
            printNode( child, sw );
            child = child.getNextSibling();
        }
    }
    /**
     * Print an element's end-code to the current buffer
     * @param elem the element in question
     */
    static void printElementEnd( Element elem, StringWriter sw )
    {
        if ( elem.getFirstChild()!=null )
        {
            sw.write( "</" );
            sw.write(elem.getNodeName() );
            sw.write( ">" );
        }
    }
    /**
     * Print a single element node to the current buffer
     * @param elem the element in question
     */
    static void printElementStart( Element elem, StringWriter sw )
    {
        sw.write("<");
        sw.write(elem.getNodeName());
        NamedNodeMap attrs = elem.getAttributes();
        for ( int j=0;j<attrs.getLength();j++ )
        {
            Node attr = attrs.item( j );
            sw.write( " ");
            sw.write( attr.getNodeName() );
            sw.write( "=\"" );
            sw.write( attr.getNodeValue() );
            sw.write( "\"" );
        }
        if ( elem.getFirstChild()==null )
            sw.write("/>");
        else
            sw.write(">");
    }
    /**
     * Write a node to the current output buffer
     * @param node the node to start from
     */
    static void printNode( Node node, StringWriter sw )
    {
        if ( node.getNodeType()==Node.TEXT_NODE )
        {
            String content = node.getTextContent();
            if ( content.contains("&") )
                content = content.replace("&","&amp;");
            sw.write( content );
        }
        else if ( node.getNodeType()==Node.ELEMENT_NODE )
        {
            printElementStart( (Element)node,sw );
            printElementBody( (Element)node,sw );
            printElementEnd( (Element)node, sw );
        }
        else if ( node.getNodeType()==Node.COMMENT_NODE )
        {
            sw.write("<!--");
            sw.write(node.getTextContent());
            sw.write("-->");
        }
    }
	
    /**
     * Convert a loaded DOM document to a String
     * @param doc the DOM document
     * @return a String being its content
     */
    public static String docToString( Document doc )
    {
        StringWriter sw = new StringWriter();
        printNode( doc.getDocumentElement(),sw );
        return sw.toString();
    }
    public static String cleanCR( String value, boolean spaces )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<value.length();i++ )
        {
            if ( value.charAt(i)!='\n'&&value.charAt(i)!='\r' )
            {
                if ( value.charAt(i)=='"' && sb.charAt(sb.length()-1) != '\\')
                    sb.append("\\\"");
                else
                    sb.append(value.charAt(i));
            }
            else if ( spaces )
                sb.append( " " );
        }
        return sb.toString();
    }
    public static String removePercent2F( String urn )
    {
        String urnUpper = urn.toUpperCase();
        return urnUpper.replace("%2F","/").toLowerCase();
    }
     /**
      * Get the display name for a language given its code+country
      * @param langCode e.g. "it" or "en_GB" etc
      */
     public static String languageName( String langCode )
     {
         String country = langCode;
         if ( langCode.contains("_") )
            country = langCode.substring(0,langCode.indexOf("_"));
        return new Locale(country).getDisplayName();                 
     }
     /**
      * Is the given markup file HTML or something else (e.g. XML)?
      * @param markup
      * @return 
      */
     public static boolean isHtml( String markup )
     {
         StringBuilder sb = new StringBuilder();
         int state = 0;
         for ( int i=0;i<markup.length();i++ )
         {
             char token = markup.charAt(i);
             switch( state )
             {
                 case 0:
                     if ( token == '<' )
                         state = 1;
                     break;
                 case 1:    // seen '<'
                     if ( Character.isLetter(token) )
                     {
                         sb.append( token );
                         state = 2;
                     }
                     else
                         state = 0;
                     break;
                 case 2:    // seen "<[letter]"
                     if ( Character.isWhitespace(token) )
                     {
                         if ( sb.toString().toLowerCase().equals("html") )
                             return true;
                         else
                             return false;
                     }
                     else
                         sb.append(token);
                     break;
             }
         }
         return false;
     }
     /**
     * Get the document body of the given urn or null
     * @param db the database where it is
     * @param docID the docID of the resource
     * @return the document body or null if not present
     */
    private static String getDocumentBody( String db, String docID ) 
        throws AeseException
    {
        try
        {
            String jStr = Connector.getConnection().getFromDb(db,docID);
            if ( jStr != null )
            {
                JSONDocument jDoc = JSONDocument.internalise( jStr );
                if ( jDoc != null )
                {
                    Object body = jDoc.get( JSONKeys.BODY );
                    if ( body != null )
                        return body.toString();
                }
            }
            throw new AeseException("document "+db+"/"+docID+" not found");
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
     /**
     * Fetch a single style text
     * @param style the path to the style in the corform database
     * @return the text of the style
     */
    public static String fetchStyle( String style ) throws AeseException
    {
        // 1. try to get each literal style name
        String actual = getDocumentBody(Database.CORFORM,style);
        while ( actual == null )
        {
            // 2. add "default" to the end
            actual = getDocumentBody( Database.CORFORM,
                URLEncoder.append(style,Formats.DEFAULT) );
            if ( actual == null )
            {
                // 3. pop off last path component and try again
                if ( style.length()>0 )
                    style = Path.chomp(style);
                else
                    throw new AeseException("no suitable format");
            }
        }
        return actual;
    }
    /**
     * Pinched from Tim Bray aerc on GoogleCode
     * @param in the input stream to read from
     * @return a byte array containing the read data
     * @throws IOException 
     */
    public static byte[] readStream(InputStream in) throws IOException 
    {
        byte[] buf = new byte[1024];
        int count = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(8096);
        while ((count = in.read(buf)) != -1)
            out.write(buf, 0, count);
        return out.toByteArray();
    }
    
}
