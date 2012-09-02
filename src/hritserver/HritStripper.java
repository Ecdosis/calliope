/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
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

import hritserver.json.JSONResponse;
import hritserver.constants.Libraries;
/**
 *
 * @author desmond
 */
public class HritStripper 
{
    public native int strip( String xml, String recipe, String format, 
            String style, JSONResponse text, JSONResponse markup );
	public native String[] formats();
	static 
	{
        try
        {
            System.loadLibrary(Libraries.HRITSTRIPPER);
        }
        catch ( Exception e )
        {
            System.out.println(e.getMessage());
        }
	}
}
