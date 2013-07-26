/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope;
import org.w3c.dom.Document;
import java.io.StringWriter;
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
                sb.append(value.charAt(i));
            else if ( spaces )
                sb.append( " " );
        }
        return sb.toString();
    }
     public static String removePercent2F( String urn )
    {
        StringBuilder sb;
        String urnUpper = urn.toUpperCase();
        int index = urnUpper.indexOf("%2F");
        if ( index != -1 )
        {
            sb = new StringBuilder( urnUpper );
            while ( index != -1 )
            {
                sb.replace( index, index+3, "/" );
                index = sb.indexOf( "%2F" );
            }
            urn = sb.toString();
        }
        return urn;
    }
}
