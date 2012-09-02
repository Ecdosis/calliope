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
/**
 * A stage to eliminate over-size files
 * @author desmond
 */
public class StageOne extends Stage
{
    static int MAX_LEN = 102400;
    public StageOne( ArrayList<File> files )
    {
        super();
        this.files = files;
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
            if ( item.data.length()>MAX_LEN )
            {
                log.append("File ");
                log.append( item.name );
                log.append( " is too long (" );
                log.append( item.data.length() );
                log.append( "). Maximum is " );
                log.append( MAX_LEN );
                log.append( ". Skipping...\n" );
            }
            else
                newFiles.add( item );
        }
        files = newFiles;
        return log.toString();
    }
}
