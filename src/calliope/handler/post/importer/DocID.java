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
import calliope.Utils;
/**
 * Build a suitable docid
 * @author desmond
 */
public class DocID 
{
    static String UNKNOWN = "unknown";
    /** work's primary language */
    String language;
    /** author's name used to identify work */
    String author;
    /** subdirectory */
    String subDir;
    /** /-delimited sequence of work and section names of unlimited length*/
    String work;
    /** the remainder of the docID if any */
    String rest;
    /** raw parts unassigned to internal variables */
    String[] parts;
    /**
     * Replace uppercase with lowercase and spaces with underscores
     * @param raw the uncanonised string
     * @return the canonised string
     */
    private String canonise( String raw )
    {
        StringBuilder sb = new StringBuilder();
        int state = 0;
        for ( int i=0;i<raw.length();i++ )
        {
            char token = raw.charAt(i);
            switch ( state )
            {
                case 0: // looking at text
                    if ( token=='%' )
                    {
                        sb.append('%' );
                        state = 2;
                    }
                    else if ( Character.isUpperCase(token) )
                        sb.append( Character.toLowerCase(token) );
                    else if ( Character.isWhitespace(token) )
                    {
                        sb.append('_');
                        state = 1;
                    }
                    else
                        sb.append( token );
                    break;
                case 1: // see a space already
                    if ( !Character.isWhitespace(token) )
                    {
                        if ( Character.isUpperCase(token) )
                            sb.append( Character.toLowerCase(token) );
                        else
                            sb.append( token );
                        state = 0;
                    }
                    break;
                case 2: // seen %
                    if ( token == '2' )
                        state = 3;
                    else
                        state = 0;
                    sb.append(token);
                    break;
                case 3: // seen %2
                    sb.append( Character.toUpperCase(token) );
                    state = 0;
                    break;
            }
        }
        return sb.toString();
    }
    public String getLanguage()
    {
        return language;
    }
    public String getAuthor()
    {
        return author;
    }
    public String getWork()
    {
        return work;
    }
    /**
     * Initialise an docid from canonical reference to one
     * @param docID language/author/work...
     */
    public DocID( String docID )
    {
        this.author = UNKNOWN;
        this.language = UNKNOWN;
        this.work = UNKNOWN;
        canonise( docID );
        docID = Utils.removePercent2F( docID );
        parts = docID.split("/");
        int state = 0;
        StringBuilder sb = null;
        for ( int i=0;i<parts.length;i++ )
        {
            switch ( state )
            {
                case 0:
                    if ( parts[i].length() > 0 )
                    {
                        language = parts[i];
                        state = 1;
                    }
                    break;
                case 1:
                    if ( parts[i].length()>0 )
                    {
                        author = parts[i];
                        state = 2;
                    }
                    break;
                case 2:
                    if ( parts[i].length()>0 )
                    {
                        if ( parts[i].startsWith(" ") )
                            subDir = parts[i];
                        else 
                        {
                            work = parts[i];
                            state = 3;
                        }
                    }
                    break;
                case 3:
                    if ( parts[i].length()>0 )
                    {
                        if ( sb == null )
                            sb = new StringBuilder();
                        else
                            sb.append("/");
                        sb.append( parts[i] );
                    }
                    break;
            }
        }
        if ( sb != null )
            rest = sb.toString();
    }
    /**
     * Build the docID 
     * @return the docID for use in the database as a document key
     */
    public String get()
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<parts.length;i++ )
        {
            if ( parts[i].length()>0 )
            {
                sb.append( parts[i] );
                if ( i < parts.length-1 )
                    sb.append("/");
            }
        }
        return sb.toString();
    }
    /**
     * Get the language/author form of the docID only
     * @return a shortened docID
     */
    public String shortID()
    {
        return this.language+"/"+this.author;
    }
}