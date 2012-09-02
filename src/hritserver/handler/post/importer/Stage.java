/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.post.importer;
import java.util.ArrayList;
import hritserver.exception.ImportException;
import hritserver.importer.Archive;
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
}
