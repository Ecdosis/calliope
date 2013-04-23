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
 * Simple class to represent a Text node
 * @author desmond
 */
public class Text extends Element
{
    String content;
    public Text()
    {
        // empty constructor
    }
    public Text( String content )
    {
        this.content = content;
    }
    /**
     * Express ourselves as a String for outputting
     * @return a String
     */
    @Override
    public String toString()
    {
        return content;
    }
}
