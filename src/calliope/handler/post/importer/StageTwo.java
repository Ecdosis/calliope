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
import edu.luc.nmerge.mvd.diff.Diff;
import edu.luc.nmerge.mvd.diff.Matrix;
import calliope.importer.Archive;
import java.util.ArrayList;
/**
 * Filter a set of files by comparing them with the first one
 * @author desmond
 */
public class StageTwo extends Stage 
{
    static float CUTOFF = 0.1f;
    String encoding;
    boolean checkSimilarity;
    public StageTwo( Stage last, boolean checkSimilarity )
    {
        super( last );
        this.checkSimilarity = checkSimilarity;
        this.encoding = "UTF-8";
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
     * @return the percentage of similarity
     */
    private float similar( File file1, File file2 )
    {
        try
        {
            byte[] data1 = file1.data.getBytes(encoding);
            byte[] data2 = file2.data.getBytes(encoding);
            Diff[] diffs = Matrix.computeBasicDiffs( data1, data2 );
            float diffLen = 0;
            float totalLen = data1.length;
            for ( int j=0;j<diffs.length;j++ )
            {
                diffLen += diffs[j].newLen();
            }
            float fraction = (totalLen-diffLen)/totalLen;
            if ( fraction < CUTOFF )
            {
                log.append("Rejecting " );
                log.append( file2.name );
                log.append( ": not similar enough (");
                log.append( (totalLen-diffLen)/totalLen );
                log.append(")\n");
                return fraction*100.0f;
            }
            else
            {
                return fraction*100.0f;
            }
        }
        catch ( Exception e )
        {
            return 0.0f;
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
                float percent = similar(first,files.get(i));
                if ( !checkSimilarity || percent/100.0f>CUTOFF )
                {
                    newFiles.add( files.get(i) );
                    accept( files.get(i).name, percent );
                }
            }
            files = newFiles;
        }
        else
            log.append("Only one file: no diffs computed\n");
        return log.toString();
    }
    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }
}
