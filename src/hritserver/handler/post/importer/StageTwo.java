/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.post.importer;
import edu.luc.nmerge.mvd.diff.Diff;
import edu.luc.nmerge.mvd.diff.Matrix;
import hritserver.importer.Archive;
import java.util.ArrayList;
/**
 * Filter a set of files by comparing them with the first one
 * @author desmond
 */
public class StageTwo extends Stage 
{
    static float CUTOFF = 0.1f;
    boolean checkSimilarity;
    public StageTwo( Stage last, boolean checkSimilarity )
    {
        super( last );
        this.checkSimilarity = checkSimilarity;
    }
    private void accept( String fileName, float percent )
    {
        log.append("Accepted " );
        log.append( fileName );
        if ( percent > 0.0f )
        {
            log.append( ". Similarity ");
            int intPart = (int)Math.floor(percent);
            log.append( intPart );
            log.append( "." );
            int fracPart = Math.round((percent-(float)intPart)*100);
            log.append( fracPart );
            log.append( "%.\n" );
        }
        else
            log.append("\n");
    }
    /**
     * Is a file similar enough to be merged?
     * @param file1 the first file in the set
     * @param file2 the new file to compare to
     * @return true if it is similar enough
     */
    private boolean similar( File file1, File file2 )
    {
        try
        {
            byte[] data1 = file1.data.getBytes("UTF-8");
            byte[] data2 = file2.data.getBytes("UTF-8");
            Diff[] diffs = Matrix.computeBasicDiffs( data1, data2 );
            float diffLen = 0;
            float totalLen = data1.length;
            for ( int j=0;j<diffs.length;j++ )
            {
                diffLen += diffs[j].newLength();
            }
            if ( (totalLen-diffLen)/totalLen < CUTOFF )
            {
                log.append("Rejecting " );
                log.append( file2.name );
                log.append( ": not similar enough (");
                log.append( (totalLen-diffLen)/totalLen );
                log.append(")\n");
                return false;
            }
            else
            {
                accept( file2.name, ((totalLen-diffLen)/totalLen)*100.0f );
                return true;
            }
        }
        catch ( Exception e )
        {
            return false;
        }

    }
    /**
     * Process the files
     * @return the log output
     */
    @Override
    public String process( Archive cortex, Archive corcode )
    {
        if ( files.size() > 1 )
        {
            ArrayList<File> newFiles = new ArrayList<File>();
            File first = files.get( 0 );
            accept( first.name, 0.0f );
            newFiles.add( first );
            for ( int i=1;i<files.size();i++ )
            {
                float l1 = first.data.length();
                float l2 = files.get(i).data.length();
                if ( Math.min(l1,l2)/Math.max(l1,l2)<CUTOFF  )
                {
                    log.append("Rejecting " );
                    log.append( files.get(i).name );
                    log.append( ": lengths of " );
                    log.append( first.name );
                    log.append( "(" );
                    log.append( l1 );
                    log.append( ") and " );
                    log.append( files.get(i).name );
                    log.append("(" );
                    log.append( l2 );
                    log.append( ") too different\n");
                    continue;
                }
                if ( !checkSimilarity || similar(first,files.get(i)) )
                {
                    newFiles.add( files.get(i) );
                    accept( files.get(i).name, 0.0f );
                }
            }
            files = newFiles;
        }
        else
            log.append("Only one file: no diffs computed\n");
        return log.toString();
    }
}
