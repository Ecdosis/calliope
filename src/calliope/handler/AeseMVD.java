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
package calliope.handler;
import edu.luc.nmerge.mvd.MVD;
import edu.luc.nmerge.mvd.MVDFile;
import calliope.constants.JSONKeys;
import calliope.json.JSONDocument;
/**
 * Loaded representation of the JSON document representing an MVD
 * @author desmond
 */
public class AeseMVD 
{
    public String format;
    public MVD mvd;
    public String style;
    public String description;
    public String title;
    public String author;
    public String version1;
    /**
     * Initialise a AeseMVD object
     * @param doc the JSON document from the database
     */
    public AeseMVD( JSONDocument doc )
    {
        String body = (String)doc.get(JSONKeys.BODY);
        if ( body != null )
            this.mvd = MVDFile.internalise( body );
        this.format = (String)doc.get(JSONKeys.FORMAT);
        this.author = (String)doc.get(JSONKeys.AUTHOR);
        this.description = (String)doc.get(JSONKeys.DESCRIPTION);
        this.style = (String)doc.get(JSONKeys.STYLE);
        this.title = (String)doc.get(JSONKeys.TITLE);
        this.version1 = (String)doc.get(JSONKeys.VERSION1);
    }
}
