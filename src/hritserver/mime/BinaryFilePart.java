/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.mime;
import java.io.File;
import java.io.FileInputStream;
import hritserver.exception.HritException;
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
     * @throws HritException if the file couldn't be read
     */
    public BinaryFilePart( String name, String fileName, 
        String mimeType, String encoding ) throws HritException
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
            throw new HritException( "Failed to read file "+fileName );
        }
    }
}
