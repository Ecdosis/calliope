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
package hritserver;
import java.util.ArrayList;
/**
 * ByteBuffer is not dynamic
 * @author desmond
 */
public class ByteHolder extends ArrayList<byte[]>
{
    int overall;
    public ByteHolder()
    {
    }
    /**
     * Add a byte array to the holder
     * @param data the byte array to append
     */
    public void append( byte[] data )
    {
        this.add( data );
        overall += data.length;
    }
    /**
     * Get this buffer's data
     * @return a byte array
     */
    byte[] getData()
    {
        byte[] data = new byte[overall];
        for ( int i=0,pos=0;i<size();i++ )
        {
            byte[] bytes = get(i);
            System.arraycopy(bytes,0,data,pos,bytes.length);
            pos += bytes.length;
        }
        return data;
    }
    /**
     * Get the length of this byte holder's data
     * @return an int
     */
    int length()
    {
        return overall;
    }
}
