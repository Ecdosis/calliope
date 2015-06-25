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
package calliope;

import calliope.json.JSONResponse;
import calliope.constants.Libraries;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import calliope.exception.AeseException;
public class AeseFormatter
{
    /**
     * Convert standoff properties into embedded markup
     * @param text the text as a string of chars
     * @param markup an array of CorCodes
     * @param css an array of CSS formats
     * @param format and array of format names, e.g. STIL
     * @param output object to contain output
     * @return 1 if it worked else 0
     */
	public native int format( String text, String[] markup, String[] css, 
		JSONResponse output );
    // ensure library is loaded
	static 
	{
        System.loadLibrary(Libraries.AESEFORMATTER);
	}
    static String readStringFromFile( String fname ) throws AeseException
    {
        try
        {
            File input = new File(fname);
            if ( input.exists() )
            {
                FileInputStream fis = new FileInputStream(input);
                byte[] data = new byte[(int)input.length()];
                fis.read(data);
                fis.close();
                return new String(data,"UTF-8");
            }
            else
            {
                throw new FileNotFoundException(fname);
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * JNI test
     * @param args 
     */
    public static void main(String[] args)
    {
        try
        {
            JSONResponse html = new JSONResponse(JSONResponse.HTML);
            if ( args.length==3 )
            {
                String text = readStringFromFile(args[0]);
                String[] markup = new String[1];
                markup[0] = readStringFromFile(args[1]);
                String[] css = new String[1];
                css[0] = readStringFromFile(args[2]);
                new AeseFormatter().format(text,markup,css,html);
                System.out.print(html.getBody());
            }
            else
                System.out.println("usage: java calliope.AeseFormatter "
                    +"<text-file <stil-markup-file> <css-file>");
        }
        catch ( AeseException e )
        {
            System.out.println(e.getMessage());
        }
    }
}
