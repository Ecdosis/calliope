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
package calliope.json;
import calliope.exception.AeseException;
/**
 * A simple holder for responses returned by native methods. We pass one of 
 * these objects into the native method, it sets the fields and we get the 
 * result as a string.
 * @author desmond
 */
public class JSONResponse 
{
    String body;
    static int BLOCK_SIZE = 5;
    public static int TEXT = 0;
    public static int HTML = 1;
    public static int XML = 2;
    public static int MARKDONW = 3;
    public static int STIL = 4;
    int nLayers;
    // extra storage for additional strings and layer names
    String[] layers;
    String[] names;
    int outputFormat;
    public JSONResponse( int outputFormat )
    {
        this.outputFormat = outputFormat;
    }
    public String getBody()
    {
        return body;
    }
    /**
     * Add a layer
     * @param layer the layer (a markup string)
     * @param name the name for this layer
     */
    public void addLayer( String layer, String name )
    {
        if ( layers == null )
            layers = new String[BLOCK_SIZE];
        if ( names == null )
            names = new String[BLOCK_SIZE];
        if ( nLayers == layers.length )
        {
            int newSize = layers.length+BLOCK_SIZE;
            String[] temp = new String[newSize];
            System.arraycopy( layers, 0, temp, 0, nLayers );
            layers = temp;
        }
        if ( nLayers == names.length )
        {
            int newSize = names.length+BLOCK_SIZE;
            String[] temp = new String[newSize];
            System.arraycopy( names, 0, temp, 0, nLayers );
            names = temp;
        }
        names[nLayers] = name;
        layers[nLayers++] = layer;
    }
    /**
     * How many layers do we have?
     * @return the size of the layers array
     */
    public int numLayers()
    {
        return nLayers;
    }
    /**
     * Get the text of a particular layer
     * @param n the index of the layer
     * @return the layer's content
     * @throws AeseException 
     */
    public String getLayer( int n ) throws AeseException
    {
        if ( n >= nLayers )
            throw new AeseException("Invalid layer index "
                +n+" out of "+nLayers);
        return layers[n];
    }
    public String getLayerName( int n ) throws AeseException
    {
        if ( n >= nLayers )
            throw new AeseException("Invalid layer index "
                +n+" out of "+nLayers);
        return names[n];
    }
    /**
     * Get the output format of this response object
     * @return a simple integer
     */
    public int getOutputFormat()
    {
        return outputFormat;
    }
}
