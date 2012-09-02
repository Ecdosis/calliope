/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.post.importer;
import hritserver.importer.Archive;
import hritserver.exception.ImportException;
import hritserver.json.JSONDocument;
import hritserver.json.JSONResponse;
import hritserver.HritStripper;
import hritserver.constants.Formats;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Process the XML files for import
 * @author desmond
 */
public class StageThreeXML extends Stage
{
    String stripConfig;
    String splitConfig;
    String style;
    public StageThreeXML( Stage last, String style )
    {
        super();
        this.style = style;
        for ( int i=0;i<last.files.size();i++ )
        {
            File f = last.files.get( i );
            if ( f.isXML() )
                this.files.add( f );
            else
            {
                log.append( "excluding from XML set ");
                log.append( f.name );
                log.append(", not being valid XML\n" );
            }
        }
    }
    /**
     * Set the stripping recipe for the XML filter
     * @param config a json document from the database
     */
    public void setStripConfig( String config )
    {
        this.stripConfig = config;
    }
    /**
     * Set the splitting recipe for the XML filter
     * @param config a json document from the database
     */
    public void setSplitConfig( String config )
    {
        this.splitConfig = config;
    }
    /**
     * Strip the suffix form a file name
     * @param fileName the filename with a possible suffix
     * @return the name minus its suffix if any
     */
    private String stripSuffix( String fileName )
    {
        int index = fileName.lastIndexOf(".");
        if ( index != -1 )
            fileName = fileName.substring(0,index);
        return fileName;
    }
    /**
     * Convert ordinary quotes into curly ones
     * @param a char array containing the unicode text
     */
    void convertQuotes( char[] chars )
    {
        char prev = 0;
        for ( int i=0;i<chars.length;i++ )
        {
            if ( chars[i]==39 )    // single quote, straight
            {
                if ( Character.isWhitespace(prev)
                    ||Character.getType(prev)==21
                    ||Character.getType(prev)==29 )
                    chars[i] = '‘';
                else
                    chars[i] = '’';
            }
            else if ( chars[i]==34 )   // double quote, straight
            {
                if ( Character.isWhitespace(prev)
                    ||Character.getType(prev)==21
                    ||Character.getType(prev)==29 )
                    chars[i] = '“';
                else
                    chars[i]='”';
            }
            prev = chars[i];
        }
    }
    /**
     * Process the files
     * @return the log output
     */
    public String process( Archive cortex, Archive corcode ) throws ImportException
    {
        try
        {
            JSONDocument jDoc = JSONDocument.internalise( splitConfig );
            Splitter splitter = new Splitter( jDoc );
            for ( int i=0;i<files.size();i++ )
            {
                long startTime = System.currentTimeMillis();
                Map<String,String> map = splitter.split( files.get(i).toString() );
                long diff = System.currentTimeMillis()-startTime;
                log.append("split ");
                log.append( files.get(i).name );
                log.append(" in " );
                log.append( diff );
                log.append( " milliseconds into " );
                log.append( map.size() );
                log.append( " versions\n" );
                Set<String> keys = map.keySet();
                Iterator<String> iter = keys.iterator();
                while ( iter.hasNext() )
                {
                    String key = iter.next();
                    JSONResponse markup = new JSONResponse();
                    JSONResponse text = new JSONResponse();
                    HritStripper stripper = new HritStripper();
                    int res = stripper.strip( map.get(key), stripConfig, 
                        Formats.STIL, style, text, markup );
                    if ( res == 1 )
                    {
                        String group = "";
                        if ( keys.size()>1 )
                            group = stripSuffix(files.get(i).name)+"/";
                        //char[] chars = text.getBody().toCharArray();
                        //convertQuotes( chars );
                        //cortex.put( group+key, new String(chars).getBytes("UTF-8") );
                        cortex.put( group+key, text.getBody().getBytes("UTF-8") );
                        corcode.put( group+key, markup.getBody().getBytes("UTF-8") );
                        log.append( "stripped " );
                        log.append( files.get(i).name );
                        log.append("(");
                        log.append( key );
                        log.append(")");
                        log.append(" successfully\n" );
                    }
                    else
                    {
                        throw new ImportException("stripping of "
                            +files.get(i).name+" XML failed");
                    }
                }
            }
        }
        catch ( Exception e ) 
        {
            if ( e instanceof ImportException )
                throw (ImportException)e;
            else
                throw new ImportException( e );
        }
        return log.toString();
    }
}
