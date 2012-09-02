/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.mime;
import java.nio.charset.Charset;
/**
 *
 * @author desmond
 */
public class PlainTextPart extends Part
{
    public PlainTextPart( String name, String mimeType )
    {
        super( Charset.defaultCharset().name() );
        addHeader( "content-disposition: form-data; name=\""+name+ "\"" );
        addHeader( "Content-Type: "+mimeType ); 
        addHeader( "Content-Transfer-Encoding: binary" );
    }
}
