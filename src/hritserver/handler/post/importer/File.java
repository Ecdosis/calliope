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

package hritserver.handler.post.importer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
/**
 * Represent a loaded file
 * @author desmond
 */
public class File 
{
    public String data;
    public String name;
    private char UTF8_BOM = 65279;
    public File( String name, String data )
    {
        this.name = name;
        this.data = data;
        if ( data.length()>0&&data.charAt(0)==UTF8_BOM )
            this.data = data.substring(1);
    }
    public boolean isJSON()
    {
        return this.name.endsWith(".json");
    }
    private boolean isBom( char token )
    {
        return token == UTF8_BOM;
    }
    /**
     * Is this an XML file?
     * @return true if it is
     */
    public boolean isXML()
    {
        boolean isXML = false;
        for ( int i=0;i<data.length();i++ )
        {
            char token = data.charAt(i);
            if ( !Character.isWhitespace(token) )
            {
                if ( token == '<' )
                    isXML = true;
                break;
            }
        }
        // Hmmm. Looks like XML, smells like XML. Let's check to be sure...
        if ( isXML )
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try 
            {
                DocumentBuilder db = dbf.newDocumentBuilder();
                StringReader sr = new StringReader( data );
                InputSource is = new InputSource( sr );
                Document dom = db.parse( is );
            }
            catch ( Exception e ) 
            {
                isXML = false;
            }
        }
        return isXML;
    }
    @Override
    public String toString()
    {
        return data;
    }
}
