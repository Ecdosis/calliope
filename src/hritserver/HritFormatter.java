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

import hritserver.json.JSONResponse;

public class HritFormatter
{
    /**
     * Call the C formatting code
     * @param text the text as a string of bytes
     * @param markup an array of CorCodes
     * @param css an array of CSS formats
     * @param format and array of format names, e.g. STIL
     * @param html html object to contain output
     * @return 1 if it worked else 0
     */
	public native int format( byte[] text, String[] markup, String[] css, 
		String[] format, JSONResponse html );
    // ensure library is loaded
	static 
	{
        System.loadLibrary("HritFormatter");
	}
}
