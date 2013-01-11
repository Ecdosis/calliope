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
import java.util.ArrayList;
import hritserver.importer.Archive;
import hritserver.constants.Globals;
/**
 * A stage to eliminate over-size and non-text or non-XML files
 * @author desmond
 */
public class StageOne extends Stage
{
    static String XML = "xml";
    static String TEXT = "txt";
    public StageOne( ArrayList<File> files )
    {
        super();
        this.files = files;
    }
    /**
     * Extract the suffix if any from a file name
     * @param fileName the file name
     * @return the suffix or the empty string
     */
    private String suffix( String fileName )
    {
        int dotPos = fileName.lastIndexOf(".");
        if ( dotPos == -1 )
            return "";
        else
            return fileName.substring(dotPos+1).toLowerCase();
    }   
    /**
     * Eliminate files that are too big
     * @param cortex a MVD archive for the plain text
     * @param corcode an MVD archive for the versioned markup
     * @return the log record of the elimination process
     */
    @Override
    public String process( Archive cortex, Archive corcode )
    {
        ArrayList<File> newFiles = new ArrayList<File>();
        for ( int i=0;i<files.size();i++ )
        {
            File item = files.get(i);
            String suffix = suffix(item.name);
            if ( item.data.length()>Globals.MAX_UPLOAD_LEN )
            {
                log.append("File ");
                log.append( item.name );
                log.append( " rejected because it is too long (" );
                log.append( item.data.length() );
                log.append( "). Maximum is " );
                log.append( Globals.MAX_UPLOAD_LEN );
                log.append( ".\n" );
            }
            else if ( suffix.length() == 0 || suffix.equals(XML)
                || suffix.equals(TEXT) )
                newFiles.add( item );
            else
            {
                log.append( "File " );
                log.append( item.name );
                log.append( " rejected because suffix is .");
                log.append(suffix);
                log.append( " not .xml or .txt or empty\n");
            }
        }
        files = newFiles;
        return log.toString();
    }
}
