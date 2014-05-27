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
    // extra storage for additional strings
    String[] layers;
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
     */
    public void addLayer( String layer )
    {
        if ( layers == null )
            layers = new String[BLOCK_SIZE];
        if ( nLayers == layers.length )
        {
            int newSize = layers.length+BLOCK_SIZE;
            String[] temp = new String[newSize];
            System.arraycopy( layers, 0, temp, 0, nLayers );
            layers = temp;
        }
        layers[nLayers++] = layer;
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
