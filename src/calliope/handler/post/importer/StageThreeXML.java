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
import calliope.importer.Archive;
import calliope.exception.ImportException;
import calliope.json.JSONDocument;
import calliope.json.JSONResponse;
import calliope.handler.post.sanitise.Sanitiser;
import calliope.AeseStripper;
import calliope.Utils;
import calliope.constants.Formats;
import calliope.constants.Globals;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import calliope.handler.post.annotate.Annotation;
import calliope.handler.post.annotate.NoteStripper;

/**
 * Process the XML files for import
 * @author desmond
 */
public class StageThreeXML extends Stage
{
    String stripConfig;
    String splitConfig;
    String sanitiseConfig;
    String style;
    String xslt;
    String dict;
    String hhExcepts;
    boolean hasTEI;
    ArrayList<Annotation> notes;
    
    public StageThreeXML()
    {
        super();
        this.dict = Globals.DEFAULT_DICT;
    }
    public StageThreeXML( String style, String dict, String hhExcepts )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
    }
    /**
     * 
     * @param last
     * @param style
     * @param dict name of dictionary e.g. en_GB
     * @param hhExcepts hard hyphen exceptions list (space delimited)
     */
    public StageThreeXML( Stage last, String style, String dict, 
        String hhExcepts )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
        for ( int i=0;i<last.files.size();i++ )
        {
            File f = last.files.get( i );
            if ( f.isXML(log) )
            {
                if ( f.isTEI() )
                    hasTEI = true;
                if ( f.isTEICorpus() )
                {
                    File[] members = f.splitTEICorpus();
                    for ( int j=0;j<members.length;j++ )
                        this.files.add( members[j] );
                }
                else
                    this.files.add( f );
            }
            // irrelvant files already exceluded by stage 1
//            else
//            {
//                log.append( "excluding from XML set ");
//                log.append( f.name );
//                log.append(", not being valid XML\n" );
//            }
        }
    }     
    public ArrayList<Annotation> getAnnotations()
    {
        return notes;
    }
    /**
     * Does this stage3 have at least ONE TEI file?
     * @return true if it does
     */
    public boolean hasTEI()
    {
        return hasTEI;
    }
    /**
     * Set the XSLT stylesheet
     * @param xslt the XSLT transform stylesheet (XML)
     */
    public void setTransform( String xslt )
    {
        this.xslt = xslt;
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
     * Set the sanitise recipe for the XML filter
     * @param json a json document from the database
     */
    public void setSanitiseConfig( String json )
    {
        this.sanitiseConfig = json;
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
     * Get the version name from a standard file name
     * @param fileName the raw file name
     * @return the version name if it followed the standard pattern
     */
    String extractVersionName( String fileName )
    {
        String stripped = fileName;
        int index = stripped.lastIndexOf("/");
        if ( index != -1 )
            stripped = stripped.substring(index+1);
        index = stripped.lastIndexOf(".");
        if ( index != -1 )
            stripped = stripped.substring(0,index);
        index = stripped.indexOf("#");
        if ( index != -1 )
            stripped = stripped.substring(index+1);
        index = stripped.indexOf("-");
        if ( index != -1 )
            stripped = stripped.substring(index+1);
        return stripped;
    }
    /**
     * Process the files
     * @param cortex the cortext MVD to accumulate files into
     * @param corcode the corcode MVD to accumulate files into
     * @return the log output
     */
    @Override
    public String process( Archive cortex, Archive corcode ) throws ImportException
    {
        try
        {
            if ( files.size() > 0 )
            {
                JSONDocument jDoc = JSONDocument.internalise( splitConfig );
                Splitter splitter = new Splitter( jDoc );
                for ( int i=0;i<files.size();i++ )
                {
                    File file = files.get(i);
                    String fileText = file.toString();
                    long startTime = System.currentTimeMillis();
                    Map<String,String> map = splitter.split( fileText );
                    long diff = System.currentTimeMillis()-startTime;
                    log.append("Split ");
                    log.append( file.name );
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
                        JSONResponse markup = new JSONResponse(JSONResponse.STIL);
                        JSONResponse text = new JSONResponse(JSONResponse.TEXT);
                        AeseStripper stripper = new AeseStripper();
                        String xml = map.get(key);
                        // extract the notes from the xml
                        String vid = extractVersionName(files.get(i).name);
                        NoteStripper ns = new NoteStripper();
                        xml = ns.strip( vid, xml.getBytes(cortex.getEncoding()) ); 
                        if ( notes == null )
                            notes = ns.getNotes();
                        else
                            notes.addAll(ns.getNotes());
                        int res = stripper.strip( xml, stripConfig, 
                            Formats.STIL, style, dict, hhExcepts, 
                            Utils.isHtml(xml), text, markup );
                        if ( res == 1 )
                        {
                            if ( map.size()>1 )
                                vid += "/"+key;
                            //char[] chars = text.getBody().toCharArray();
                            //convertQuotes( chars );
                            //cortex.put( group+key, new String(chars).getBytes("UTF-8") );
                            cortex.put( vid, text.getBody().getBytes("UTF-8") );
                            String stil = markup.getBody();
                            // apply sanitiser if set
                            if ( this.sanitiseConfig != null )
                            {
                                Sanitiser st = new Sanitiser( this.sanitiseConfig );
                                stil = st.translate( stil );
                            }
                            corcode.put( vid, stil.getBytes("UTF-8") );
                            log.append( "Stripped " );
                            log.append( file.name );
                            log.append("(");
                            log.append( key );
                            log.append(")");
                            log.append(" successfully\n" );
                        }
                        else
                        {
                            throw new ImportException("Stripping of "
                                +files.get(i).name+" XML failed");
                        }
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
