/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */

package hritserver.handler.post;

import hritserver.HritServer;
import hritserver.constants.*;
import hritserver.exception.HritException;
import hritserver.handler.post.importer.*;
import hritserver.importer.Archive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Superclass of the various concrete import classes
 * @author desmond
 */
public abstract class HritImportHandler extends HritPostHandler
{
    StringBuilder log;
    DocID docID;
    String style;
    String filterName;
    String database;
    String splitterName;
    String stripperName;
    /** uploaded xslt file contents */
    String xslt;
    boolean demo;
    /** text filter config */
    String textName;
    HashMap<String,String> nameMap;
    ArrayList<File> files;
    public HritImportHandler()
    {
        nameMap = new HashMap<String,String>(); 
        filterName = Filters.EMPTY;
        stripperName = "default";
        splitterName = "default";
        textName = "default";
        files = new ArrayList<File>();
        log = new StringBuilder();
    }
    /**
     * Add the archive to the database
     * @param archive the archive
     * @param type its type: cortex or corcode
     * @param suffix the suffix to append
     * @throws HritException 
     */
    protected void addToDBase( Archive archive, String type, String suffix ) 
        throws HritException
    {
        // now get the json docs and add them at the right docid
        if ( !archive.isEmpty() )
        {
            String path;
            if ( suffix.length()>0 )
                path = "/"+type+"/"+docID.get(false)+"%2F"+suffix;
            else
                path = "/"+type+"/"+docID.get(false);
            if ( type.equals("corcode") )
                path += "%2Fdefault";
            String revid = HritServer.getRevId( path );
            if ( revid != null && revid.length()>0 )
                archive.setRevId( revid );
            HritServer.putToDb( path, archive.toMVD(type) );
            log.append( archive.getLog() );
        }
        else
            log.append("No "+type+" created (empty)\n");
    }
    /**
     * Parse the import params from the request
     * @param request the http request
     */
    void parseImportParams( HttpServletRequest request ) throws HritException
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
                        if ( fieldName.equals(Params.DOC_ID) )
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
                        else if ( fieldName.equals(Params.STYLE) )
                            style = contents;
                        else if ( fieldName.equals(Params.DEMO) )
                            demo = true;
                        else if ( fieldName.equals(Params.FILTER) )
                            filterName = contents.toLowerCase();
                        else if ( fieldName.equals(Params.SPLITTER) )
                            splitterName = contents.toLowerCase().replace("/","%2F");
                        else if ( fieldName.equals(Params.STRIPPER) )
                            stripperName = contents.toLowerCase().replace("/","%2F");
                        else if ( fieldName.equals(Params.TEXT) )
                            textName = contents.toLowerCase();
                        else if ( fieldName.equals(Params.XSLT) )
                            xslt = getConfig(Config.xslt,contents);
                    }
                }
                else if ( item.getName().length()>0 )
                {
                    try
                    {
                        File f = new File( item.getName(), 
                            item.getString("UTF-8") );
                        files.add( f );
                    }
                    catch ( Exception e )
                    {
                        throw new HritException( e );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
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
        sb.append( docID.get(true) );
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
     * @throws HritException 
     */
    String getConfig( Config kind, String path ) throws HritException
    {
        try
        {
            String doc = null;
            String configDocId = "/"+Database.CONFIG+"/"+kind.toString()+"%2F"+path;
            while ( doc == null )
            {
                byte[] data = HritServer.getFromDb( configDocId.toLowerCase() );
                if ( data == null )
                {
                    String[] parts = configDocId.split("%2F");
                    if ( parts.length == 1 )
                        throw new HritException("config not found: "
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
                            sb.append("%2F");
                        }
                        if ( sb.length()==0 )
                        {
                            sb.append("/");
                            sb.append(Database.CONFIG);
                            sb.append("/");
                            sb.append(kind);
                            sb.append("%2F");
                        }
                        configDocId = sb.toString()+Formats.DEFAULT;
                        if ( oldDocId.equals(configDocId) )
                            throw new HritException("config "+oldDocId
                                +" not found");
                    }
                }
                else
                {
                    doc = new String( data, "UTF-8" );
                }
            }
            return doc;
        }
        catch ( Exception e )
        {
            HritException he;
            if ( e instanceof HritException )
                he = (HritException) e ;
            else
                he = new HritException( e );
            throw he;
        }
    }
}
