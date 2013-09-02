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
import calliope.json.JSONDocument;
import calliope.exception.ImportException;
import java.io.ByteArrayOutputStream;
/**
 * Import a poem with stanzas and a title
 * @author desmond
 */
public class PoemFilter extends Filter
{
    int minStanzaLength;
    int maxStanzaLength;
    int firstStanzaLength;
    boolean hasHeading;
    int lgStart;
    int numHeadingLines;
    int headLength;
    public PoemFilter()
    {
        super();
        minStanzaLength = Integer.MAX_VALUE;
    }
    @Override
    public void configure( JSONDocument config )
    {
        System.out.println("Poem filter: config");
    }
    /**
     * Return something for a dropdown menu
     * @return a String
     */
    @Override
    public String getDescription()
    {
        return "Poem with stanzas";
    }
    boolean isEmpty( String line )
    {
        if ( line.length()>0 )
        {
            for ( int i=0;i<line.length();i++ )
                if ( !Character.isWhitespace(line.charAt(i)) )
                    return false;
        }
        return true;
    }
    /**
     * Analyse the stanza lengths
     * @param lines 
     */
    private void analyseStanzas( String[] lines )
    {
        int stanzaLength = 0;
        for ( int i=0;i<lines.length;i++ )
        {
            if ( isEmpty(lines[i]) )
            {
                if ( stanzaLength>0 )
                {
                    if ( stanzaLength < minStanzaLength )
                        minStanzaLength = stanzaLength;
                    if ( stanzaLength > maxStanzaLength )
                        maxStanzaLength = stanzaLength;
                    if ( firstStanzaLength == 0 )
                        firstStanzaLength = stanzaLength;
                    stanzaLength = 0;
                }
            }
            else
                stanzaLength++;
        }
        if ( minStanzaLength < maxStanzaLength/2 && firstStanzaLength<3 
            && firstStanzaLength==minStanzaLength )
            hasHeading = true; 
    }
    @Override
    protected void init()
    {
        super.init();
        lgStart = 0;
        numHeadingLines = 0;
        headLength = 0;
        hasHeading = false;
        lgStart = 0;
        numHeadingLines = 0;
        headLength = 0;
    }
    /**
     * Convert to standoff properties
     * @param input the raw text input string
     * @param name the name of the new version
     * @param cortext a cortext mvd archive
     * @param corcode a corcode mvd archive
     * @return log output
     */
    @Override
    public String convert( String input, String name, Archive cortex, 
        Archive corcode ) throws ImportException
    {
        try
        {
            ByteArrayOutputStream txt = new ByteArrayOutputStream();
            String[] lines = input.split("\n");
            init();
            analyseStanzas(lines);
            int state = (hasHeading)?0:1;
            for ( int i=0;i<lines.length;i++ )
            {
                String str = lines[i].trim();
                byte[] current = str.getBytes(ENC);
                switch ( state )
                {
                    case 0:
                        if ( lines[i].length()>0 )
                        {
                            writeCurrent( txt, current );
                            writeCurrent( txt, CR );
                            numHeadingLines++;
                            if ( numHeadingLines==minStanzaLength )
                            {
                                markup.add("head",0,written);
                                state = 1;
                            }
                        }
                        break;
                    case 1: // new stanza
                        lgStart = written;
                        markup.add("l",written,current.length);
                        writeCurrent(txt, current );
                        writeCurrent(txt, CR);
                        state = 2;
                        break;
                    case 2: // body of stanza
                        if ( lines[i].length()>0 )
                        {
                            markup.add( "l",written,current.length );
                            writeCurrent( txt, current );
                            writeCurrent( txt, CR );
                        }
                        else
                        {
                            markup.add("lg",lgStart,written-lgStart);
                            writeCurrent( txt, CR );
                            state = 1;
                        }
                        break;
                }
            }
            if ( state == 2 )
            {
                markup.add("lg",lgStart,written-lgStart);
                writeCurrent(txt, CR);
            }
            markup.sort();
            cortex.put( name, txt.toByteArray() );
            String json = markup.toSTILDocument().toString();
            corcode.put( name, json.getBytes() );
            return "";
        }
        catch ( Exception e )
        {
            throw new ImportException( e );
        }
    }
}
