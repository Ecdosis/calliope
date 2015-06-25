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
import java.io.FileInputStream;
import java.io.File;
/**
 * Strip XML embedded codes from text out into STIL (JSON) format
 * @author desmond
 * JNIEXPORT jint JNICALL Java_calliope_AeseStripper_strip
  (JNIEnv *env, jobject obj, jstring xml, jstring rules, jstring format, 
    jstring style, jstring language, jstring hexcepts, jobject text, 
    jobject markup)
 */
public class AeseStripper 
{
    public native int strip( String xml, String recipe, 
        String style, String language, String hhExcepts, boolean html,
        JSONResponse text, JSONResponse markup );
	public native String[] formats();
	static 
	{
        try
        {
            System.loadLibrary(Libraries.AESESTRIPPER);
        }
        catch ( Exception e )
        {
            System.out.println(e.getMessage());
        }
	}
    public static void main( String[] args )
    {
        try
        {
            if ( args.length==1 )
            {
                File input = new File(args[0]);
                if ( input.exists() )
                {
                    FileInputStream fis = new FileInputStream(input);
                    byte[] data = new byte[(int)input.length()];
                    fis.read(data);
                    fis.close();
                    String xml = new String(data,"UTF-8");
                    JSONResponse stil = new JSONResponse(JSONResponse.STIL);
                    JSONResponse text = new JSONResponse(JSONResponse.TEXT);
                    new AeseStripper().strip(xml,null,"TEI","en_GB",null,false,text,stil);
                    System.out.println(stil.getBody());
                }
            }
            else
                System.out.println("usage: java calliope.AeseStripper <xml-file>");
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
    }
}
