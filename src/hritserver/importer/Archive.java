/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.importer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import hritserver.json.JSONDocument;
import hritserver.constants.JSONKeys;
import hritserver.constants.Formats;
import hritserver.exception.HritException;
import edu.luc.nmerge.mvd.MVD;
import edu.luc.nmerge.mvd.Version;

/**
 * A set of CorCode or CorTex files, each a version of the same work, 
 * indexed by the group-path/shortname separated by "/"
 * @author desmond
 */
public class Archive extends HashMap<String,byte[]>
{
    String author;
    String title;
    String description;
    StringBuilder log;
    String style;
    String version1;
    HashMap<String,String> nameMap;
    /**
     * Create an archive
     * @param work the work path, after language and author, %2F delimited
     * @param author the full author's name
     * @param section the section of the work if any or null
     * @param subsection the subsection of the section or null
     */
    public Archive( String work, String author )
    {
        this.title = work.replace("%20"," ");
        this.author = author;
        this.log = new StringBuilder();
        this.nameMap = new HashMap<String,String>();
        StringBuilder sb = new StringBuilder();
        String[] parts = work.split("%2F");
        for ( int i=0;i<parts.length;i++ )
        {
            if ( sb.length()==0 )
                sb.append( parts[i].replace("%20"," ") );
            else
            {
                sb.append(", ");
                sb.append( parts[i].replace("%20"," ") );
            }
        }
        if ( sb.length()>0 )
            sb.setCharAt( 0, Character.toUpperCase(sb.charAt(0)) );
        sb.append( " by ");
        if ( author.length()>0 )
            sb.append( Character.toUpperCase(author.charAt(0)) );
        if ( author.length()>1 )
            sb.append( author.substring(1) );
        description = sb.toString();
    }
    /**
     * Add a long name to our map for later use
     * @param key the groups+version short name key
     * @param longName its long name
     */
    public void addLongName( String key, String longName )
    {
        nameMap.put( key, longName );
    }
    public void setStyle( String style )
    {
        this.style = style;
    }
    /**
     * Get the log report of this archive's merging activities
     * @return a string
     */
    public String getLog()
    {
        return log.toString();
    }
    /**
     * Split off the groups path if any
     * @param key the groups+short name separated by slashes
     * @return the groups name
     */
    private String getGroups( String key )
    {
        String[] parts = key.split("/");
        String groups = "Base";
        if ( parts.length > 1 )
        {
            groups = "";
            for ( int i=0;i<parts.length-1;i++ )
            {
                groups = parts[i];
                if ( i < parts.length-2 )
                    groups += "/";
            }
        }
        return groups;
    }
    /**
     * Get the short name from a compound groups+short name
     * @param key the compound key
     * @return the bare short name at the end
     */
    private String getKey( String key )
    {
        String[] parts = key.split("/");
        key = parts[parts.length-1];
        return key;
    }
    /**
     * Convert this archive to an MVD, wrapped in JSON for storage
     * @param mvdName name of the MVD
     * @return a string representation of the MVD as a JSON document
     * @throws HritException 
     */
    public String toMVD( String mvdName ) throws HritException
    {
        try
        {
            JSONDocument doc = new JSONDocument();
            doc.add( JSONKeys.FORMAT, Formats.TEXT, false );
            doc.add( JSONKeys.TITLE, title, false ); 
            doc.add( JSONKeys.AUTHOR, author, false );
            Set<String> keys = keySet();
            Iterator<String> iter = keys.iterator();
            MVD mvd = new MVD();
            mvd.setDescription( description );
            // go through the files, adding versions to the MVD
            short vId;
            long startTime = System.currentTimeMillis();
            while ( iter.hasNext() )
            {
                String key = iter.next();
                String groups = getGroups( key );
                String shortKey = getKey( key );
                if ( version1 == null )
                    version1 = "/"+groups+"/"+shortKey;
                byte[] data = get( key );
                vId = (short)mvd.newVersion( shortKey, "Version "+shortKey, 
                    groups, Version.NO_BACKUP, false );
                mvd.update( vId, data, true );
            }
            long diff = System.currentTimeMillis()-startTime;
            log.append( "merged " );
            log.append( title );
            log.append( ": " );
            log.append( mvdName );
            log.append( " in " );
            log.append( diff );
            log.append( " milliseconds\n" );
            String body = mvd.toString();
            if ( body.length() == 0 )
                throw new HritException("failed to create MVD");
            doc.add( JSONKeys.TITLE, title, false );
            doc.add( JSONKeys.VERSION1, version1, false );
            doc.add( JSONKeys.DESCRIPTION, description, false );
            doc.add( JSONKeys.AUTHOR, author, false );
            doc.add( JSONKeys.STYLE, style, false );
            doc.add( JSONKeys.FORMAT, Formats.STIL, false );
            doc.add( JSONKeys.BODY, body, false );
            return doc.toString();
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
