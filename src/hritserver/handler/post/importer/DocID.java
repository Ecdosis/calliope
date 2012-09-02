/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.post.importer;

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
    /** %2F delimited sequence of work and section names of unlimited length*/
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
     * @param docID language%2Fauthor%2Fwork...
     */
    public DocID( String docID )
    {
        this.author = UNKNOWN;
        this.language = UNKNOWN;
        this.work = UNKNOWN;
        canonise( docID );
        parts = docID.split("%2F");
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
                        if ( parts[i].startsWith("%20") )
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
                            sb.append("%2F");
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
     * @param clean if true use " " and "/" not "%20" and "%2F"
     * @return the docID for use in the database as a document key
     */
    public String get( boolean clean )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<parts.length;i++ )
        {
            if ( parts[i].length()>0 )
            {
                String part = parts[i];
                if ( !clean )
                    part = part.replace(" ","%20");
                sb.append( part );
                if ( i < parts.length-1 )
                {
                    if ( clean )
                        sb.append("/");
                    else
                        sb.append( "%2F" );
                }
            }
        }
        return sb.toString();
    }
}