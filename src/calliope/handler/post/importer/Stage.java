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

package calliope.handler.post.importer;
import java.util.ArrayList;
import calliope.exception.ImportException;
import calliope.importer.Archive;
/**
 * Given a set of files, process them in some way
 * @author desmond
 */
abstract public class Stage 
{
    protected ArrayList<File> files;
    StringBuilder log;
    public Stage()
    {
        files = new ArrayList<File>();
        log = new StringBuilder();
    }
    /**
     * Iniitalise this stage from the last one
     * @param last the previous stage
     */
    public Stage( Stage last )
    {
        this.files = last.files;
        log = new StringBuilder();
    }
    /**
     * Add a new file to our collection
     * @param name the name of the file
     * @param data the file's contents to add
     */
    public void add( String name, String data )
    {
        files.add( new File(name,data) );
    }
    /**
     * Add a new file to our collection
     * @param file a ready-made file
     */
    public void add( File file )
    {
        files.add( file );
    }
    /**
     * Is the specified file in the collection?
     * @param f the file in question
     * @return true if it is there, else false
     */
    public boolean containsFile( File f )
    {
        for ( int i=0;i<files.size();i++ )
        {
            File g = files.get(i);
            if ( g.name.equals(f.name) )
                return true;
        }
        return false;
    }
    /**
     * Process the files
     * @return the log output
     */
    abstract public String process( Archive cortex, Archive corcode ) throws ImportException;
    /**
     * Retrieve the files for merging etc with other lists
     * @return the internal files array
     */
    public ArrayList<File> getFiles()
    {
        return files;
    }
    /**
     * Does this stage have any files left?
     * @return true if it does
     */
    public boolean hasFiles()
    {
        return files.size()>0;
    }
    /**
     * Strip the suffix from a file name
     * @param fileName the filename with a possible suffix
     * @return the name minus its suffix if any
     */
    protected String stripSuffix( String fileName )
    {
        int index = fileName.lastIndexOf(".");
        if ( index != -1 )
            fileName = fileName.substring(0,index);
        return fileName;
    }
}
