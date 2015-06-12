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
import calliope.constants.Formats;
import calliope.importer.Archive;
import calliope.handler.post.importer.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.exception.AeseException;
import calliope.constants.Config;
import calliope.handler.post.annotate.Annotation;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.util.ArrayList;
/**
 * Handle importation of mixed files from the import dialog.
 * @author desmond
 */
public class AeseMixedImportHandler extends AeseImportHandler
{
    AeseMixedImportHandler()
    {
        super();
        style = Formats.TEI+"/default";
    }
    private String addCRs( String log )
    {
        StringBuilder sb = new StringBuilder(log);
        for ( int i=log.length()-1;i>0;i-- )
        {
            if ( log.charAt(i)=='\n')
                sb.insert(i,"<br>");
        }
        sb.insert(0, "<!doctype html><head><meta charset=\"UTF-8\">"
            +"<style type=\"text/css\">body { font: 12px ariel, "
            +"sans-serif }</style></head><body>");
        sb.append("</body></html>");
        return sb.toString();
    }
    /**
     * Handle posted files from the input dialog. Can be mixed TEXT etc files
     * @param request the request object
     * @param response the response object
     * @param urn the urn of the request
     * @throws AeseException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                if ( !demo )
                {
                    Archive cortex = new Archive(docID.getWork(), 
                        docID.getAuthor(), Formats.MVD_TEXT, encoding);
                    cortex.setStyle( style );
                    Archive corcode = new Archive(docID.getWork(), 
                        docID.getAuthor(),Formats.MVD_STIL,encoding);
                    corcode.setStyle( style );
                    StageOne stage1 = new StageOne( files );
                    log.append( stage1.process(cortex,corcode) );
                    if ( stage1.hasFiles() )
                    {
                        // it's safer to do this in a try-catch handler
                        try
                        {
                            String suffix = "";
                            StageTwo stage2 = new StageTwo( stage1, similarityTest );
                            stage2.setEncoding( encoding );
                            log.append( stage2.process(cortex,corcode) );
                            StageThreeXML stage3Xml = new StageThreeXML( stage2, 
                                style, dict, hhExceptions );
                            stage3Xml.setEncoding( encoding );
                            stage3Xml.setDocId( this.docID.get() );
                            /*stage3Xml.setStripConfig( getConfig(Config.stripper,
                                stripperName) );*/
                            stage3Xml.setSplitConfig( getConfig(Config.splitter,
                                splitterName) );
                            /*String sanitiser = getConfig(Config.sanitiser,
                                docID.shortID());
                            stage3Xml.setSanitiseConfig( sanitiser.equals("{}")?null:sanitiser );*/
                            log.append( stage3Xml.process(cortex,corcode) );
                            ArrayList<Annotation> notes = stage3Xml.getAnnotations();
                            if ( notes != null && notes.size()>0 )
                                addAnnotations( notes, true );
                            // process the text filers
                            StageThreeText stage3Text = new StageThreeText( 
                                filterName, dict, hhExceptions );
                            stage3Text.setConfig( getConfig(Config.text,
                                filterName+"/"+textName) );
                            ArrayList<File> stage2Files = stage2.getFiles();
                            for ( int i=0;i<stage2Files.size();i++ )
                            {
                                File f = stage2Files.get(i);
                                if ( !stage3Xml.containsFile(f)&&!f.isXML(log) )
                                    stage3Text.add( f );
                            }
                            log.append( stage3Text.process(cortex,corcode) );
                            //cortex.externalise();
                            addToDBase( cortex, "cortex", suffix );
                            addToDBase( corcode, "corcode", suffix );
                        }
                        catch ( Exception e )
                        {
                            e.printStackTrace(System.out);
                            log.append( e.getMessage() );
                        }
                    }
                    else
                        log.append("No cortex/corcode created\n");
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println( addCRs(log.toString()) );
                }
                else
                {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().println( 
                        "<p>Password required on public server</p>" );
                }
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
