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
import java.io.ByteArrayOutputStream;
import hritserver.exception.HritException;
/**
 *
 * @author desmond
 */
public abstract class Part 
{
    ByteArrayOutputStream body;
    String encoding;
    Header header;
    static String CRLF = "\r\n";
    public Part( String encoding )
    {
        this.encoding = encoding;
        header = new Header();
        body = new ByteArrayOutputStream();
    }
    /**
     * Convert the part to a string
     * @return an empty string if the encoding didn't work
     */
    public String toString()
    {
        String ret = "";
        try
        {
            ret = header.toString()+body.toString( encoding );
        }
        catch ( Exception e )
        {
            System.err.println(e.getMessage());
        }
        return ret;
    }
    /**
     * Get the length of this part
     * @return an integer being the overall length of this part
     */
    int getLength()
    {
        return header.length()+body.size();
    }
    /**
     * Add a header to the part
     * @param header 
     */
    void addHeader( String header )
    {
        this.header.add( header );
    }
    /**
     * Retrieve the header
     * @return a Header
     */
    public Header getHeader()
    {
        return header;
    }
    /**
     * Get the body as a string
     * @return a string being only the body not the headers
     */
    public String getBody()
    {
        return this.body.toString();
    }
    /**
     * Add some text to the body of this part
     * @param text the text to add
     */
    void addToBody( byte[] body )
    {
        this.body.write( body, 0, body.length );
    }
    /**
     * Turn a string into a part
     * @param text the text to convert
     * @return a subclass of Part
     */
    static Part parse( String text ) throws HritException
    {
        Part p = null;
        String sep = CRLF+CRLF;
        int bodyPos = text.indexOf(sep);
        if ( bodyPos == -1 )
            throw new HritException("no division between header and body");
        else
        {
            Header h = Header.parse( text.substring(0,bodyPos) );
            p = h.createPart();
            String body = text.substring( bodyPos+sep.length() );
            try
            {
                p.addToBody( body.getBytes(h.getEncoding()) );
            }
            catch ( Exception e )
            {
                throw new HritException( e );
            }
        }
        return p;
    }
}
