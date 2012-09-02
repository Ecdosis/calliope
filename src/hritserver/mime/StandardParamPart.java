/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.mime;

import java.nio.charset.Charset;

/**
 * Represent a standard POST parameter
 * @author desmond
 */
public class StandardParamPart extends Part
{
    public StandardParamPart( String name, String encoding )
    {
        super( Charset.defaultCharset().name() );
        addHeader( "Content-Disposition: form-data; name=\""+name+"\"" );
        addHeader( "Content-Type: text/plain; charset=" + encoding );
    }
}
