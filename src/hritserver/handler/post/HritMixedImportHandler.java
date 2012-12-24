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
import hritserver.constants.Formats;
import hritserver.importer.Archive;
import hritserver.handler.post.importer.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.exception.HritException;
import hritserver.importer.filters.Config;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.util.ArrayList;
/**
 * Handle importation of mixed files from the import dialog.
 * @author desmond
 */
public class HritMixedImportHandler extends HritImportHandler
{
    HritMixedImportHandler()
    {
        super();
        style = Formats.TEI;
    }
    /**
     * Handle posted files from the input dialog. Can be mixed TEXT etc files
     * @param request the request object
     * @param response the response object
     * @param urn the urn of the request
     * @throws HritException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                if ( !demo )
                {
                    Archive cortex = new Archive(docID.getWork(), 
                        docID.getAuthor());
                    cortex.setStyle( style );
                    Archive corcode = new Archive(docID.getWork(), 
                        docID.getAuthor());
                    corcode.setStyle( style );
                    StageOne stage1 = new StageOne( files );
                    log.append( stage1.process(cortex,corcode) );
                    if ( stage1.hasFiles() )
                    {
                        // it's safer to do this in a try-catch handler
                        try
                        {
                            StageTwo stage2 = new StageTwo( stage1, true );
                            log.append( stage2.process(cortex,corcode) );
                            StageThreeXML stage3Xml = new StageThreeXML( stage2, 
                                style );
                            stage3Xml.setStripConfig( getConfig(Config.STRIPPER,
                                stripperName) );
                            stage3Xml.setSplitConfig( getConfig(Config.SPLITTER,
                                splitterName) );
                            log.append( stage3Xml.process(cortex,corcode) );
                            // process the text filers
                            StageThreeText stage3Text = new StageThreeText( 
                                filterName );
                            stage3Text.setConfig( getConfig(Config.TEXT,
                                filterName+"%2F"+textName) );
                            ArrayList<File> stage2Files = stage2.getFiles();
                            for ( int i=0;i<stage2Files.size();i++ )
                            {
                                File f = stage2Files.get(i);
                                if ( !stage3Xml.containsFile(f) )
                                    stage3Text.add( f );
                            }
                            log.append( stage3Text.process(cortex,corcode) );
                            // now get the json docs and add them at the right docid
                            if ( !cortex.isEmpty() )
                            {
                                HritServer.putToDb( "/cortex/"+docID.get(false), 
                                    cortex.toMVD("cortex") );
                                log.append( cortex.getLog() );
                            }
                            else
                                log.append("No cortex created\n");
                            if ( !corcode.isEmpty() )
                            {
                                HritServer.putToDb( "/corcode/"+docID.get(false)
                                    +"%2Fdefault", corcode.toMVD("corcode") );
                                log.append( corcode.getLog() );
                            }
                            else
                                log.append("No corcode created\n");
                        }
                        catch ( Exception e )
                        {
                            log.append( e.getMessage() );
                        }
                    }
                    else
                        log.append("No cortex/corcode created\n");
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println( wrapLog() );
                }
                else
                {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println( "<p>Not enabled on public server</p>" );
                }
            }
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
