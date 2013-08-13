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
package calliope.tests.html;

/**
 * Represent a HTML attribute
 * @author desmond
 */
public class Attribute 
{
    String key;
    String value;
    /**
     * An attribute is just a key-value pair
     * @param key the key
     * @param value the value
     */
    public Attribute( String key, String value )
    {
        this.key = key;
        this.value = value;
    }
    /**
     * Convert this attribute to a String
     * @return a String
     */
    public String toString()
    {
        if ( value !=null )
            return " "+key+"=\""+value+"\"";
        else
            return " "+key;
    }
}
