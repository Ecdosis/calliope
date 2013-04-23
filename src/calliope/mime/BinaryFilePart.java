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
import java.io.File;
import java.io.FileInputStream;
import calliope.exception.AeseException;
/**
 *
 * @author desmond
 */
public class BinaryFilePart extends Part 
{
    /**
     * Create a binary file part
     * @param name its name
     * @param fileName the file to draw from
     * @param mimeType the mime type of the content
     * @param encoding the content's encoding
     * @throws AeseException if the file couldn't be read
     */
    public BinaryFilePart( String name, String fileName, 
        String mimeType, String encoding ) throws AeseException
    {
        super( encoding );
        addHeader( "Content-disposition: form-data; name=\""
            +name+"\"; filename=\""+fileName+"\"" );
        addHeader( "Content-Type: "+mimeType ); 
        addHeader( "Content-Transfer-Encoding: binary" );
        // now for the file
        File input = new File( fileName );
        try
        {
            FileInputStream fis = new FileInputStream(input);
            byte[] data = new byte[(int)input.length()];
            fis.read( data );
            fis.close();
            addToBody( data );
        }
        catch ( Exception e )
        {
            throw new AeseException( "Failed to read file "+fileName );
        }
    }
}
