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
package calliope.mime;
import java.util.ArrayList;
import calliope.exception.AeseException;
/**
 * Compose a MIME multipart request/response
 * @author desmond
 */
public class Multipart 
{
    String boundary;
    ArrayList<Part> parts;
    /**
     * Create a MIME multipart message
     */
    public Multipart()
    {
        parts = new ArrayList<Part>();
        boundary = Long.toHexString(System.currentTimeMillis()); 
    }
    /**
     * Get the content
     * @return a String
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<parts.size();i++ )
        {
            sb.append( divider() );
            sb.append( parts.get(i).toString() );
        }
        sb.append( Part.CRLF );
        sb.append( "--" );
        sb.append( boundary );
        sb.append( "--" );
        sb.append( Part.CRLF );
        return sb.toString();
    }
    /**
     * Get the boundary dividing parts
     * @return a string
     */
    public String getBoundary()
    {
        return boundary;
    }
    /**
     * Compose the part-divider
     * @return a String
     */
    public String divider()
    {
        return Part.CRLF+"--"+boundary+Part.CRLF;
    }
    /**
     * Get the overall length of this multipart object
     * @return the overall length including boundary lengths
     */
    public int getLength()
    {
        int length = 0;
        for ( int i=0;i<parts.size();i++ )
        {
            length += divider().length();
            length += parts.get(i).getLength();
        }
        return length;
    }
    /**
     * Compose an ordinary multipart parameter
     * @param name the name of the parameter
     * @param value its value
     * @param encoding the encoding for the parameter
     * @return a String representing the entire single part
     */
    public void putStandardParam( String name, String value, String encoding )
    {
        Part p = new StandardParamPart(name,encoding);
        p.addToBody( value.getBytes() );
        parts.add( p );
    }
    /**
     * Add a plain text parameter
     * @param name the name of the parameter
     * @param contents the contents to put
     * @param mimeType the mime type of the content
     * @throws Exception 
     */
    public void putTextParam( String name, String contents, String mimeType ) 
    {
        Part p = new PlainTextPart( name, mimeType );
        p.addToBody( contents.getBytes() );
        parts.add( p );
    }
    /**
     * Add a binary file parameter
     * @param name the name of the parameter
     * @param fileName the filename of the file
     * @param mimeType the mime type of the content
     * @param encoding the encoding of the file content
     * @throws Exception 
     */
    public void putBinaryFileParam( String name, String fileName, 
        String mimeType, String encoding ) throws Exception
    {
        parts.add( new BinaryFilePart(name,fileName,mimeType,encoding) );
    }
    /**
     * Set this multipart's boundary
     * @param multipart the raw multipart text
     * @param first the position in multipart of the first "--"
     */
    void setBoundary( String multipart, int first ) throws AeseException
    {
        int bStart = first+Part.CRLF.length()+2;
        int bEnd = multipart.indexOf(Part.CRLF, bStart);
        if ( bEnd != -1 )
            boundary = multipart.substring(bStart,bEnd);
        else
            throw new AeseException("expected CRLF at boundary end");
    }
    /**
     * Get the requested part
     * @param index the0-based index of the part
     * @return a Part
     * @throws AeseException 
     */
    public Part getPart( int index ) throws AeseException
    {
        if ( index <= parts.size() )
            return parts.get( index );
        else
            throw new AeseException("invalid index "+index+"+ (max="
                +parts.size()+")");
    }
    /**
     * How many parts do we have?
     * @return an int
     */
    public int numParts()
    {
        return parts.size();
    }
    /**
     * Read a multipart text and reduce it to a Multipart object
     * @param mimePart the mime multipart text
     */
    public static Multipart internalise( String multipart ) throws AeseException
    {
        Multipart m = new Multipart();
        String divStart = Part.CRLF+"--";
        int pos = multipart.indexOf( divStart, 0 );
        m.setBoundary( multipart, pos );
        String division = Part.CRLF+"--"+m.getBoundary();
        while ( pos != -1 )
        {
            int prev = pos;
            int next = prev+division.length();
            pos = multipart.indexOf( division, next );
            if ( pos != -1 )
            {
                // then there's something between prev and pos
                int start = multipart.indexOf( Part.CRLF, next );
                if ( start != -1 )
                    start += Part.CRLF.length();
                else
                    throw new AeseException( "expected CRLF at boundary end" );
                m.parts.add( Part.parse(multipart.substring(start,pos)) );
            }
        }
        return m;
    }
}
