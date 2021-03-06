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

package calliope.handler.post;

import calliope.Connector;
import calliope.constants.*;
import calliope.ByteHolder;
import calliope.exception.AeseException;
import calliope.db.Connection;
import calliope.handler.post.importer.*;
import calliope.importer.Archive;
import calliope.handler.post.annotate.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Superclass of the various concrete import classes
 * @author desmond
 */
public abstract class AeseImportHandler extends AesePostHandler
{
    StringBuilder log;
    DocID docID;
    String style;
    String filterName;
    String database;
    String splitterName;
    String stripperName;
    /** hard hyphen exceptions */
    String hhExceptions;
    boolean similarityTest;
    String dict;
    /** uploaded xslt file contents */
    String xslt;
    boolean demo;
    /** text filter config */
    String textName;
    /** title of work */
    String title;
    HashMap<String,String> nameMap;
    HashMap<String,String> jsonKeys;
    ArrayList<File> files;
    ArrayList<ImageFile> images;
    static String DEFAULT_STYLE = "TEI/default";
    public AeseImportHandler()
    {
        nameMap = new HashMap<String,String>(); 
        jsonKeys = new HashMap<String,String>();
        filterName = Filters.EMPTY;
        dict = Locale.getDefault().toString();
        encoding = "UTF-8";
        stripperName = "default";
        splitterName = "default";
        textName = "default";
        files = new ArrayList<File>();
        images = new ArrayList<ImageFile>();
        log = new StringBuilder();
        similarityTest = false;
        title = "untitled";
    }
    /**
     * Add a batch of annotations to the database
     * @param notes an array of annotation objects
     * @param clean if true remove old annotations for this docid
     * @throws AeseException 
     */
    protected void addAnnotations( ArrayList<Annotation> notes, boolean clean ) 
        throws AeseException
    {
        String path = docID.get(); 
        Connection conn = Connector.getConnection();
        if ( clean )
        {
            // remove all existing annotations for this document
            String[] docids = conn.listDocuments("annotations",path+"/.*");
            for ( int i=0;i<docids.length;i++ )
                conn.removeFromDb( "annotations", docids[i] );
        }
        // ensure that the annotations are all unique
        HashSet<String> unique = new HashSet<String>();
        String[] docids = conn.listDocuments("annotations",path+"/.*");
        for ( int i=0;i<docids.length;i++ )
            unique.add( docids[i] );
        for ( int i=0;i<notes.size();i++ )
        {
            String docid = path + "/" + UUID.randomUUID().toString();
            while ( unique.contains(docid) )
                docid = path + "/" + UUID.randomUUID().toString();
            conn.putToDb( "annotations", docid, notes.get(i).toString() );
            unique.add( docid );
        }
    }
    /**
     * Add the archive to the database
     * @param archive the archive
     * @param db cortex or corcode
     * @param suffix the suffix to append
     * @throws AeseException 
     */
    protected void addToDBase( Archive archive, String db, String suffix ) 
        throws AeseException
    {
        // now get the json docs and add them at the right docid
        if ( !archive.isEmpty() )
        {
            String path;
            if ( suffix.length()>0 )
                path = docID.get()+"/"+suffix;
            else
                path = docID.get();
            if ( db.equals("corcode") )
                path += "/default";
            Connector.getConnection().putToDb( db, path, 
                archive.toResource(db) );
            log.append( archive.getLog() );
        }
        else
            log.append("No "+db+" created (empty)\n");
    }
    /**
     * Parse the import params from the request
     * @param request the http request
     */
    void parseImportParams( HttpServletRequest request ) throws AeseException
    {
        try
        {
            FileItemFactory factory = new DiskFileItemFactory();
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // Parse the request
            List items = upload.parseRequest( request );
            for ( int i=0;i<items.size();i++ )
            {
                FileItem item = (FileItem) items.get( i );
                if ( item.isFormField() )
                {
                    String fieldName = item.getFieldName();
                    if ( fieldName != null )
                    {
                        String contents = item.getString();
                        if ( fieldName.equals(Params.DOCID) )
                        {
                            if ( contents.startsWith("/") )
                            {
                                contents = contents.substring(1);
                                int pos = contents.indexOf("/");
                                if ( pos != -1 )
                                {
                                    database = contents.substring(0,pos);
                                    contents = contents.substring(pos+1);
                                }
                            }
                            docID = new DocID( contents );
                        }
                        else if ( fieldName.startsWith(Params.SHORT_VERSION) )
                            nameMap.put( fieldName.substring(
                                Params.SHORT_VERSION.length()),
                                item.getString());
                        else if ( fieldName.equals(Params.LC_STYLE)
                            ||fieldName.equals(Params.STYLE) )
                        {
                            jsonKeys.put(fieldName.toLowerCase(),contents);
                            style = contents;
                        }
                        else if ( fieldName.equals(Params.DEMO) )
                        {
                            if ( contents!=null&&contents.equals("brillig") )
                                demo = false;
                            else
                                demo = true;
                        }
                        else if ( fieldName.equals(Params.TITLE) )
                            title = contents;
                        else if ( fieldName.equals(Params.FILTER) )
                            filterName = contents.toLowerCase();
                        else if ( fieldName.equals(Params.SIMILARITY) )
                            similarityTest = contents!=null&&contents.equals("1");
                        else if ( fieldName.equals(Params.SPLITTER) )
                            splitterName = contents;
                        else if ( fieldName.equals(Params.STRIPPER) )
                            stripperName = contents;
                        else if ( fieldName.equals(Params.TEXT) )
                            textName = contents.toLowerCase();
                        else if ( fieldName.equals(Params.XSLT) )
                            xslt = getConfig(Config.xslt,contents);
                        else if ( fieldName.equals(Params.DICT) )
                            dict = contents;
                        else if ( fieldName.equals(Params.ENCODING) )
                            encoding = contents;
                        else if ( fieldName.equals(Params.HH_EXCEPTIONS) )
                            hhExceptions = contents;
                        else
                            jsonKeys.put( fieldName, contents );
                    }
                }
                else if ( item.getName().length()>0 )
                {
                    try
                    {
                        // assuming that the contents are text
                        //item.getName retrieves the ORIGINAL file name
                        String type = item.getContentType();
                        if ( type != null && type.startsWith("image/") )
                        {
                            InputStream is = item.getInputStream();
                            ByteHolder bh = new ByteHolder();
                            while ( is.available()>0 )
                            {
                                byte[] b = new byte[is.available()];
                                is.read( b );
                                bh.append( b );
                            }
                            ImageFile iFile = new ImageFile(
                                item.getName(), 
                                item.getContentType(), 
                                bh.getData() );
                            images.add( iFile );
                        }
                        else
                        {
                            byte[] rawData = item.get();
                            guessEncoding( rawData );
                            //System.out.println(encoding);
                            File f = new File( item.getName(), 
                                new String( rawData, encoding) );
                            files.add( f );
                        }
                    }
                    catch ( Exception e )
                    {
                        throw new AeseException( e );
                    }
                }
            }
            if ( style == null )
                style = DEFAULT_STYLE;
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
//        System.out.println("Import params received:");
//        System.out.println("docID="+docID.get());
//        System.out.println("style="+style);
//        System.out.println("filterName="+filterName);
//        System.out.println("database="+database);
//        System.out.println("splitterName="+splitterName);
//        System.out.println("stripperName="+stripperName);
//        System.out.println("encoding="+encoding);
//        System.out.println("hhExceptions="+hhExceptions);
//        System.out.println("similarityTest="+similarityTest);
//        System.out.println("dict="+dict);
//        System.out.println("xslt="+xslt);
//        System.out.println("demo="+demo);
//        System.out.println("textName="+textName);
    }
    /**
     * Wrap the log in some HTML
     * @return the wrapped log
     */
    String wrapLog()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h3>LOG</h3>");
        sb.append("<p class=\"docid\"> DocID: ");
        sb.append( docID.get() );
        sb.append( "</p><p class=\"log\">" );
        sb.append( log.toString().replace("\n","<br>") );
        sb.append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
    /**
     * Fetch the specified config from the database. If not there, check 
     * for default configs progressively higher up.
     * @param kind the config kind: text, xslt or xml
     * @param path the path to the config
     * @return the loaded config document
     */
    public static String getConfig( Config kind, String path ) //throws AeseException
    {
        try
        {
            String doc = null;
            String configDocId = kind.toString()+"/"+path;
            while ( doc == null )
            {
                doc = Connector.getConnection().getFromDb( 
                    Database.CONFIG, configDocId.toLowerCase() );
                if ( doc == null )
                {
                    String[] parts = configDocId.split("/");
                    if ( parts.length == 1 )
                        throw new AeseException("config not found: "
                            +configDocId);
                    else
                    {
                        String oldDocId = configDocId;
                        StringBuilder sb = new StringBuilder();
                        int N=(parts[parts.length-1].equals(Formats.DEFAULT))?2:1;
                        // recurse up the path
                        for ( int i=0;i<parts.length-N;i++ )
                        {
                            sb.append( parts[i] );
                            sb.append("/");
                        }
                        if ( sb.length()==0 )
                        {
                            sb.append(kind);
                            sb.append("/");
                        }
                        configDocId = sb.toString()+Formats.DEFAULT;
                        if ( oldDocId.equals(configDocId) )
                            throw new AeseException("config "+oldDocId
                                +" not found");
                    }
                }
            }
            return doc;
        }
        catch ( Exception e )
        {
//            AeseException he;
//            if ( e instanceof AeseException )
//                he = (AeseException) e ;
//            else
//                he = new AeseException( e );
//            throw he;
            // just return empty config
            return "{}";
        }
    }
}
