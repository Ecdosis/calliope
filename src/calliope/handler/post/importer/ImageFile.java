/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;

/**
 * An image file
 * @author desmond
 */
public class ImageFile
{
    String type;
    String name;
    byte[] data;
    public ImageFile( String name, String type, byte[] data )
    {
        this.data = data;
        this.name = name;
        this.type = type;
    }
    public byte[] getData()
    {
        return this.data;
    }
    public String getName()
    {
        return this.name;
    }
    public String getType()
    {
        return this.type;
    }
}
