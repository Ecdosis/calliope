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
import calliope.AeseStripper;
import calliope.Utils;
import calliope.constants.Formats;
import calliope.constants.Globals;
import calliope.constants.JSONKeys;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import calliope.handler.post.annotate.Annotation;
import calliope.handler.post.annotate.NoteStripper;
import mml.filters.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
/**
 * Process the XML files for import
 * @author desmond
 */
public class StageThreeXML extends Stage
{
    String stripConfig;
    String splitConfig;
    String style;
    String xslt;
    String dict;
    String hhExcepts;
    boolean hasTEI;
    String docid;
    String encoding;
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
     * Reduce the length of the package name of the filter being sought
     * @param className the classname of the filter that wasn't there
     * @return a shorted path e.g. mml.filters.english.harpur.Filter 
     * instead of mml.filters.english.harpur.h642.Filter
     */
    String popClassName( String className )
    {
        String[] parts = className.split("\\.");
        StringBuilder sb = new StringBuilder();
        if ( parts.length > 1 )
        {
            for ( int i=0;i<parts.length;i++ )
            {
                if ( i != parts.length-2 )
                {
                    if ( sb.length()>0 )
                        sb.append(".");
                    sb.append(parts[i]);
                }
            }
        }
        return sb.toString();
    }
    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }
    public void setDocId( String docid )
    {
        this.docid = docid;
    }
    /**
     * Convert the corcode using the filter corresponding to its docid
     * @param pair the stil and its corresponding text - return result here
     * @param docID the docid of the document
     * @param enc the encoding
     */
    void convertCorcode( StandoffPair pair )
    {
        String[] parts = this.docid.split("/");
        StringBuilder sb = new StringBuilder("mml.filters");
        for ( String part : parts )
        {
            if ( part.length()>0 )
            {
                sb.append(".");
                sb.append(part);
            }
        }
        sb.append(".Filter");
        String className = sb.toString();
        Filter f = null;
        while ( className.length() > "mml.filters".length() )
        {
            try
            {
                Class fClass = Class.forName(className);
                f = (Filter) fClass.newInstance();
                break;
            }
            catch ( Exception e )
            {
                className = popClassName(className);
            }
        }
        if ( f != null )
        {
            System.out.println("Applying filter "+className+" to "+pair.vid );
            // woo hoo! we have a filter baby!
            try
            {
                JSONObject cc = (JSONObject)JSONValue.parse(pair.stil);
                cc = f.translate( cc, pair.text );
                pair.stil = cc.toJSONString();
                pair.text = f.getText();
            }
            catch ( Exception e )
            {
                //OK it didn't work
                System.out.println("It failed for "+pair.vid+e.getMessage());
                e.printStackTrace(System.out);
            }
        }
        else
            System.out.println("Couldn't find filter for "+this.docid);
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
                        // just remove all notes for now
                        xml = ns.strip( xml ); 
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
                            StandoffPair pair = new StandoffPair(
                                markup.getBody(),text.getBody(),vid);
                            //System.out.println("text len="+text.getBody().length()+" xml length ="+xml.length());
                            convertCorcode( pair );
                            cortex.put( vid, pair.text.getBytes("UTF-8") );
                            corcode.put( vid, pair.stil.getBytes("UTF-8") );
                            if ( !verifyCorCode(pair.stil,pair.text) )
                                System.out.println("corcode of "+pair.vid+" was invalid");
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
    boolean verifyCorCode(String stil, String text )
    {
        JSONObject jObj = (JSONObject)JSONValue.parse(stil);
        JSONArray ranges = (JSONArray)jObj.get(JSONKeys.RANGES);
        int offset = 0;
        for ( int i=0;i<ranges.size();i++ )
        {
            JSONObject range = (JSONObject)ranges.get(i);
            offset += ((Number)range.get("reloff")).intValue();
            int len = ((Number)range.get("len")).intValue();
            if ( offset+len > text.length() )
                return false;
        }
        return true;
    }
}
