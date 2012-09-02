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
import hritserver.exception.HritException;
import hritserver.handler.post.importer.File;
import hritserver.importer.Archive;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle a specific import request for a CorCode/Cortex combo uploaded 
 * via a tool like mmpupload.
 * @author desmond 23-7-2012
 */
public class HritMVDImportHandler extends HritImportHandler 
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
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
                for ( int i=0;i<files.size();i++ )
                {
                    File file = files.get(i);
                    if ( file.isJSON() )
                    {
                        corcode.put( file.name, file.data.getBytes("UTF-8") );
                        if ( nameMap.containsKey(file.name) )
                            corcode.addLongName( file.name, 
                                nameMap.get(file.name) );
                    }
                    else
                    {
                        cortex.put( file.name, file.data.getBytes("UTF-8") );
                        if ( nameMap.containsKey(file.name) )
                            cortex.addLongName( file.name, 
                                nameMap.get(file.name) );
                    }
                }
                if ( !cortex.isEmpty()&&corcode.isEmpty() )
                {
                    // remember to set style into corcode
                    // now get the json docs and add them at the right docid
                    HritServer.putToDb( "/cortex/"+docID.get(false), 
                        cortex.toMVD("cortex") );
                    log.append( cortex.getLog() );
                    HritServer.putToDb( "/corcode/"+docID.get(false), 
                        corcode.toMVD("corcode") );
                    log.append( corcode.getLog() );
                }
                else
                    log.append("cortex or corcode empty");
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println( wrapLog() );
            }
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
