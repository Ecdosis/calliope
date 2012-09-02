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

package hritserver.mime;
import java.util.ArrayList;
import hritserver.exception.HritException;
import hritserver.exception.ParamException;
import java.util.HashMap;
import hritserver.constants.MIMETypes;
import java.nio.charset.Charset;
/**
 * A simple array of lines. But it also knows a few basic things
 * @author desmond
 */
public class Header 
{
    ArrayList<String> lines;
    HashMap<String,String> properties;
    Header()
    {
        lines = new ArrayList<String>();
        properties = new HashMap<String,String>();
    }
    /**
     * Add a header line. 
     * @param header 
     */
    void add( String header )
    {
        lines.add( parseLine(header) );
    }
    /**
     * Strip leading and trailing double=quotation marks
     * @param quoted the word with perhaps quotation marks around it
     * @return the same string if no quote marks, else stripped
     */
    String unquote( String quoted )
    {
        if ( quoted.length() > 0 )
        {
            int pos1=0;
            int pos2 = quoted.length();
            if ( quoted.charAt(0)=='"' )
                pos1 = 1;
            if ( quoted.charAt(quoted.length()-1)=='"' )
                pos2 = quoted.length()-1;
            return quoted.substring( pos1, pos2 );
        }
        else
            return quoted;
    }
    /**
     * Parse a header line. This is it, when the shit hits the fan
     * @param headerLine the header line
     * @return the original line, but parsed by setting things in this object
     */
    String parseLine( String headerLine )
    {
        String[] parts = headerLine.split(";");
        for ( int i=0;i<parts.length;i++ )
        {
            String pair = parts[i].trim();
            String[] keyValue = pair.split("=");
            if ( keyValue.length != 2 )
                keyValue = pair.split("\\:");
            if ( keyValue.length == 2 )
            {
                String key = keyValue[0].trim().toLowerCase();
                String value = keyValue[1].trim();
                value = unquote( value );
                properties.put( key, value );
            }
        }
        return headerLine;
    }
    /**
     * Get the header's encoding
     * @return a canonical charset name
     */
    public String getEncoding()
    {
        String encoding = properties.get("charset");
        if ( encoding == null )
            return Charset.defaultCharset().name();
        else
            return encoding;
    }
    /**
     * Parse a header and return a Header
     * @param header the text of the header
     * @return a Header
     */
    static Header parse( String header )
    {
        Header h = new Header();
        String[] headers = header.split( Part.CRLF );
        for ( int i=0;i<headers.length;i++ )
            h.add( headers[i] );
        return h;
    }
    /**
     * Get the content transfer encoding, defaulting to "binary"
     * @return a valid ct encoding
     */
    String getCtEncoding()
    {
        String ctEncoding = properties.get("content-transfer-encoding");
        if ( ctEncoding == null )
            return "binary";
        else
            return ctEncoding;
    }
    /**
     * Get the content type
     * @return a valid mime type defaulting to plain text
     */
    public String getContentType()
    {
        String contentType = properties.get("content-type");
        if ( contentType == null )
            return MIMETypes.TEXT;
        else
            return contentType;
    }
    /**
     * Get the content type
     * @return the name given to this header
     */
    public String getName()
    {
        return properties.get("name");
    }
    /**
     * Create an appropriate part to hold this header
     * @return an appropriate subclass of Part
     */
    Part createPart() throws HritException
    {
        Part p = null;
        String fileName = properties.get("filename");
        String mimeType = getContentType();
        String encoding = getEncoding();
        String name = properties.get("name");
        String ctEncoding = getCtEncoding();
        if ( name == null )
            throw new ParamException("need parameter name");
        if ( fileName != null )
            p = new BinaryFilePart( name, fileName, mimeType, encoding );
        else if ( ctEncoding.equals("binary") )
            p = new PlainTextPart( name, mimeType );
        else
            p = new StandardParamPart( name, encoding );
        return p;
    }
    /**
     * What's the length of this header, including the two trailing CRLFs
     * @return its overall length
     */
    int length()
    {
        int length = 0;
        for ( int i=0;i<lines.size();i++ )
        {
            length += lines.get(i).length();
            length += Part.CRLF.length();
        }
        length += Part.CRLF.length()*2;
        return length;
    }
    /**
     * Convert the header to a string
     * @return a string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if ( lines.size()==0 )
            sb.append( Part.CRLF );
        for ( int i=0;i<lines.size();i++ )
        {
            sb.append( lines.get(i) );
            sb.append( Part.CRLF );
        }
        sb.append( Part.CRLF );
        return sb.toString();
    }
}
