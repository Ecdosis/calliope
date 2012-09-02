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

package hritserver.json.corcode;
import hritserver.json.JSONDocument;
import hritserver.exception.JSONException;
/**
 * Represent a simple attribute
 * @author desmond
 */
public class Annotation 
{
    String name;
    Object value;
    public Annotation( String name, Object value )
    {
        this.name = name;
        this.value = value;
    }
    public JSONDocument toJSONDocument() throws JSONException
    {
        JSONDocument doc = new JSONDocument();
        doc.add( name, (String)value, false );
        return doc;
    }
    public String getName()
    {
        return name;
    }
    public Object getValue()
    {
        return value;
    }
}
