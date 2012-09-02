/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */
package hritserver.xml;
import hritserver.json.JSONDocument;
import hritserver.constants.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import org.xml.sax.InputSource;
import java.util.ArrayList;
/**
 * Represent a HRIT XML document as a JSON document
 * @author desmond
 */
public class HritDocument 
{
    /**
     * Convert an external XML HRIT document to a JSON document
     * @param src the external file
     * @param encoding its encoding, e.g. "UTF-8"
     * @return a JSON document or null
     */
    public static JSONDocument internalise( File src, String encoding )
    {
        try
        {
            FileInputStream fis = new FileInputStream( src );
            byte[] data = new byte[(int)src.length()];
            fis.read( data );
            fis.close();
            return HritDocument.internalise( new String(data,encoding) );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
            return null;
        }
    }
    /**
     * Convert a HRIT document to JSON internal format
     * @param xml the text of the HRIT xml
     * @return the JSONDOcument object
     */
    public static JSONDocument internalise( String xml )
    {
        JSONDocument jDoc = new JSONDocument();
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse( new InputSource(new StringReader(xml)) );
            Element root = doc.getDocumentElement();
            String style = root.getAttribute(JSONKeys.STYLE);
            if ( style != null )
                jDoc.put( JSONKeys.STYLE, style );
            else
                jDoc.put( JSONKeys.STYLE, Formats.DEFAULT );
            NodeList ranges = doc.getElementsByTagName( XMLElems.RANGE );
            ArrayList<JSONDocument> jsonRanges = new ArrayList<JSONDocument>();
            for ( int i=0;i<ranges.getLength();i++ )
            {
                Element r = (Element) ranges.item( i );
                JSONDocument jsonRange = new JSONDocument();
                jsonRange.put( JSONKeys.NAME, r.getAttribute(
                    XMLAttrs.NAME) );
                jsonRange.put( JSONKeys.RELOFF, Integer.parseInt(
                    r.getAttribute(XMLAttrs.RELOFF)) );
                jsonRange.put( JSONKeys.LEN, Integer.parseInt(
                    r.getAttribute(XMLAttrs.LEN)) );
                String removed = r.getAttribute(XMLAttrs.REMOVED);
                if ( removed != null && removed.length()>0 )
                    jsonRange.put(XMLAttrs.REMOVED, 
                        Boolean.parseBoolean(removed) );
                // examine range's children
                if ( r.hasChildNodes() )
                {
                    NodeList children = r.getChildNodes();
                    ArrayList<JSONDocument> annotations = null;
                    for ( int j=0;j<children.getLength();j++ )
                    {
                        Node n = children.item( j );
                        if ( n.getNodeType() == Node.ELEMENT_NODE )
                        {
                            Element f = (Element) n;
                            if ( f.getNodeName().equals(XMLElems.CONTENT) )
                            {
                                jsonRange.put(JSONKeys.CONTENT, 
                                    f.getTextContent() );
                            }
                            else if ( f.getNodeName().equals(XMLElems.ANNOTATION) )
                            {
                                if ( annotations == null )
                                    annotations = new ArrayList<JSONDocument>();
                                JSONDocument obj = new JSONDocument();
                                obj.put( f.getAttribute(XMLAttrs.NAME), 
                                    f.getAttribute(XMLAttrs.VALUE) );
                                annotations.add( obj );
                            }
                        }
                    }
                    if ( annotations != null )
                        jsonRange.put( JSONKeys.ANNOTATIONS, annotations );
                }
                jsonRanges.add( jsonRange );
            }
            jDoc.put( JSONKeys.RANGES, jsonRanges );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
        }
        return jDoc;
    }
}
