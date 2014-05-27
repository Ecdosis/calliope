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

import calliope.constants.Config;
import calliope.constants.Formats;
import calliope.exception.AeseException;
import calliope.handler.post.importer.StageOne;
import calliope.handler.post.importer.Stage3HTML;
import calliope.handler.post.importer.StageTwo;
import calliope.importer.Archive;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle importation of a set of plain text files from a tool like psef-tool.
 * @author desmond 23-7-2012
 */
public class AeseHTMLImportHandler extends AeseImportHandler
{
    AeseHTMLImportHandler()
    {
        super();
        style = "TEI/default";
    }
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                Archive cortex = new Archive(docID.getWork(), 
                    docID.getAuthor(),Formats.TEXT,encoding);
                Archive corcode = new Archive(docID.getWork(), 
                    docID.getAuthor(),Formats.STIL,encoding);
                cortex.setStyle( style );
                corcode.setStyle( style );
                StageOne stage1 = new StageOne( files );
                log.append( stage1.process(cortex,corcode) );
                if ( stage1.hasFiles() )
                {
                    String suffix = "";
                    StageTwo stage2 = new StageTwo( stage1, false );
                    stage2.setEncoding( encoding );
                    log.append( stage2.process(cortex,corcode) );
                    Stage3HTML stage3Html = new Stage3HTML( stage2,
                        style, dict, hhExceptions, encoding );
                    if ( stripperName==null || stripperName.equals("default") )
                        stripperName = "html";
                    stage3Html.setStripConfig( 
                        getConfig(Config.stripper,stripperName) );
                    log.append( stage3Html.process(cortex,corcode) );
                    addToDBase( cortex, "cortex", suffix );
                    addToDBase( corcode, "corcode", suffix );
                }
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println( wrapLog() );   
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
