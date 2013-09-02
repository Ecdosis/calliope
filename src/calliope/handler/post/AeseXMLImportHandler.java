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
import calliope.exception.AeseException;
import calliope.handler.post.importer.*;
import calliope.constants.Formats;
import calliope.importer.Archive;
import calliope.constants.Config;
import calliope.constants.Database;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle import of a set of XML files from a tool like mmpupload.
 * @author desmond 23-7-2012
 */
public class AeseXMLImportHandler extends AeseImportHandler 
{   
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                Archive cortex = new Archive(docID.getWork(), 
                    docID.getAuthor());
                Archive corcode = new Archive(docID.getWork(), 
                    docID.getAuthor());
                cortex.setStyle( style );
                corcode.setStyle( style );
                StageOne stage1 = new StageOne( files );
                log.append( stage1.process(cortex,corcode) );
                if ( stage1.hasFiles() )
                {
                    StageTwo stage2 = new StageTwo( stage1, false );
                    log.append( stage2.process(cortex,corcode) );
                    StageThreeXML stage3Xml = new StageThreeXML( stage2, 
                        style, dict, hhExceptions );
                    stage3Xml.setStripConfig( getConfig(Config.stripper,
                        stripperName) );
                    stage3Xml.setSplitConfig( getConfig(Config.splitter,
                        splitterName) );
                    log.append( stage3Xml.process(cortex,corcode) );
                    // now get the json docs and add them at the right docid
                    Connector.getConnection().putToDb( Database.CORTEX, 
                        docID.get(), cortex.toMVD("cortex") );
                    log.append( cortex.getLog() );
                    String fullAddress = docID.get()+"/"+Formats.DEFAULT;
                    log.append( Connector.getConnection().putToDb(
                        Database.CORCODE,fullAddress, corcode.toMVD("corcode")) );
                    log.append( corcode.getLog() );
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
