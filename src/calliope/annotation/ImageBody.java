/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.annotation;
import java.net.URL;
/**
 * Body for an image reference
 * @author desmond
 */
public class ImageBody extends Body
{
    URL url;
    ImageBody( URL url )
    {
        this.url = url;
    }
    public String getId()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": \"");
        sb.append( url.toString() );
        sb.append("\"");
        return sb.toString();
    }
}
