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

package calliope.importer.filters;
import calliope.importer.Archive;
import calliope.exception.ImportException;
import calliope.exception.AeseException;
import calliope.json.JSONDocument;
import calliope.AeseSpeller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Specify how filters interact with the outside world
 * @author desmond
 */
public abstract class Filter 
{
    protected String dict;
    protected String hhExceptions;
    protected AeseSpeller speller;
    protected boolean lastEndsInHyphen;
    protected HashSet<String> compounds;
    protected int written;
    protected byte[] CR = {'\n'};
    protected byte[] HYPHEN = {'-'};
    protected byte[] SPACE = {' '};
    protected byte[] EMPTY = {};
    public Filter()
    {
        this.dict = "en_GB";
        this.hhExceptions = "";
        try
        {
            this.speller = new AeseSpeller( dict );
            this.compounds = new HashSet<String>();
            if ( hhExceptions != null && hhExceptions.length()>0 )
            {
                String[] items = hhExceptions.split( "\n" );
                for ( int i=0;i<items.length;i++ )
                    compounds.add( items[i] );
            }
        }
        catch ( Exception e1 )
        {
            try
            {
                this.speller = new AeseSpeller("en_GB");
            }
            catch ( Exception e2 )
            {
            }
        }
    }
    /**
     * We really should cleanup the speller before we go
     */
    protected void finalize()
    {
        if ( this.speller != null )
            this.speller.cleanup();
    }
    protected void writeCurrent( ByteArrayOutputStream txt, byte[] current )
        throws IOException
    {
        txt.write( current );
        written += current.length;    
    }
    /**
     * Should we hard-hyphenate two words or part-words?
     * @param last the previous 'word'
     * @param next the word on the next line
     * @return true for a hard hyphen else soft
     */
    public boolean isHardHyphen( String last, String next )
    {
        String compound = last+next;
        if ( speller.hasWord(last,dict)
            &&speller.hasWord(next,dict)
            &&(!speller.hasWord(compound,dict)
                ||compounds.contains(compound)))
            return true;
        else
            return false;
    }
    public void setDict( String dict )
    {
        this.dict = dict;
    }
    public void setHHExceptions( String hhExceptions )
    {
        this.hhExceptions = hhExceptions;
    }
    /**
     * Get the raw name of this filter e.g. "play"
     * @return the filter name
     * @throws AeseException 
     */
    public String getName() throws AeseException
    {
        String className = this.getClass().getSimpleName();
        int pos = className.indexOf("Filter");
        if ( pos != -1 )
            return className.substring(0,pos);
        else
            throw new AeseException("invalid class name: "+className);
    }
    /**
     * Get the first word of a line
     * @param line the line in question
     * @return 
     */
    protected String getFirstWord( String line )
    {
        int i;
        int len = line.length();
        for ( i=0;i<line.length();i++ )
        {
            if ( !Character.isWhitespace(line.charAt(i)) )
                break;
        }
        int j = i;
        for ( ;i<len;i++ )
        {
            if ( !Character.isLetter(line.charAt(i))||line.charAt(i)=='-' )
                break;
        }
        return line.substring(j,i);
    }
    /**
     * Get the last word of a line excluding punctuation etc
     * @param line the line in question
     * @return the word
     */
    protected String getLastWord( String text )
    {
        int len = text.length();
        if ( len > 0 )
        {
            int start = 0;
            int size=0,i=len-1;
            // point beyond trailing hyphen
            if ( text.charAt(len-1) == '-' )
            {
                lastEndsInHyphen = true;
                len--;
                i--;
            }
            else
            {
                lastEndsInHyphen = false;
                // point to last non-space
                for ( ;i>0;i-- )
                {
                    if ( !Character.isWhitespace(text.charAt(i)) )
                        break;
                }
            }
            int j = i;
            for ( ;i>0;i-- )
            {
                if ( !Character.isLetter(text.charAt(i)) )
                {
                    start = i+1;
                    size = j-i;
                    break;
                }
            }
            if ( i==0 )
                size = (j-i)+1;
            return text.substring(start,start+size);
        }
        else
            return "";
    }
    public abstract void configure( JSONDocument config );
    /**
     * Short description of this filter
     * @return a string
     */
    public abstract String getDescription();
    /**
     * Subclasses should override this
     * @param input the input text for conversion
     * @param name the name of the new version
     * @param cortex the cortex archive to save split text in
     * @param corcode the corcode archive to save split markup in
     * @return the log output
     */
    public abstract String convert( String input, String name, Archive cortex, 
        Archive corcode ) throws ImportException;
}
