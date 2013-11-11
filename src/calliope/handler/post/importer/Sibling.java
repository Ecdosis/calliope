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

package calliope.handler.post.importer;

/**
 * A specification of an element in TEI that can contain versions. We should 
 * really initialise these from a config file.
 * @author desmond
 */
public class Sibling 
{
    String name;
    String brother;
    String wits;
    Sibling( String name, String brother, String wits )
    {
        this.name = name;
        this.brother = brother;
        this.wits = wits;
    }
    /**
     * Get the wits attribute name
     * @return a string
     */
    String getWits()
    {
        return wits;
    }
    String getName()
    {
        return name;
    }
    String getSibling()
    {
        return brother;
    }
}
