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

package hritserver.handler.get.compare;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.exception.*;
import hritserver.constants.Params;
import hritserver.constants.ChunkState;
import hritserver.constants.MIMETypes;
import hritserver.constants.MMP;
import hritserver.path.*;
import hritserver.mime.Multipart;
import hritserver.HritFormatter;
import hritserver.handler.HritMVD;
import hritserver.json.JSONResponse;
import java.util.Map;
import hritserver.constants.Database;
import hritserver.constants.Formats;
import hritserver.handler.get.HritGetHandler;


/**
 * Handle comparison between two versions of a document
 * @author desmond
 */
public class HritComparisonHandler extends HritGetHandler
{
    /**
     * Get the HTML of two versions, comparing both the corcode and the 
     * cortex for differences
     * @param request the request to read from
     * @param path the parsed URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        VersionPath path = new VersionPath( urn );
        Map map = request.getParameterMap();
        String[] corCodes = getEnumeratedParams( Params.CORCODE, map, true );
        String[] styles = getEnumeratedParams( Params.STYLE, map, true );
        String[] withName = (String[])map.get( Params.SHORTNAME ); 
        if ( withName == null || withName.length == 0 || withName[0].length()==0 )
            throw new ParamException("must specify a version to compare");
        String[] withGroups = (String[])map.get( Params.GROUPS );
        try
        {
            CorCode leftCC = new CorCode( ChunkState.DELETED );
            CorCode rightCC = new CorCode( ChunkState.ADDED );
            // two extra corcodes for diffs in text and markup
            String[] lCorCodes = new String[corCodes.length+2];
            String[] rCorCodes = new String[corCodes.length+2]; 
            String[] formats = new String[corCodes.length+2];
            String groups = (withGroups==null||withGroups.length==0
                ||withGroups[0].length()==0)?"":withGroups[0];
            path.setName( Database.CORCODE );
            RunSet leftRuns = new RunSet();
            RunSet rightRuns = new RunSet();
            for ( int i=0;i<corCodes.length;i++ )
            {
                HritMVD cc = loadMVD( path.getResource(true,corCodes[i]) );
                int v1 = cc.mvd.getVersionByNameAndGroup(path.getShortName(), 
                    path.getGroups() );
                int v2 = cc.mvd.getVersionByNameAndGroup( withName[0], groups );
                if ( v1 == -1 )
                    throw new ParamException("version "+path.getShortName()
                        +" not found");
                if ( v2 == -1 )
                    throw new ParamException("version "+withName[0]+" not found");
                formats[i] = cc.format;
                lCorCodes[i] = new String(cc.mvd.getVersion(v1),
                    cc.mvd.getEncoding());
                leftRuns.add( lCorCodes[i], true );
                rCorCodes[i] = new String(cc.mvd.getVersion(v2),
                    cc.mvd.getEncoding());
                rightRuns.add( rCorCodes[i], true );
                leftCC.compareCode( cc.mvd, v1, v2, ChunkState.DELETED );
                rightCC.compareCode( cc.mvd, v2, v1, ChunkState.ADDED );
            }
            // erase alignment runs where the corcode doesn't match
            leftRuns.add( leftCC, false );
            rightRuns.add( rightCC, false );
            // now compare the text versions
            CorCode leftText = new CorCode( ChunkState.DELETED );
            CorCode rightText = new CorCode( ChunkState.ADDED );
            path.setName( Database.CORTEX );
            HritMVD text = loadMVD(path.getResource(true));
            int v1 = text.mvd.getVersionByNameAndGroup(path.getShortName(), 
                path.getGroups() );
            if ( v1 == -1 )
                throw new ParamException("version "+path.getShortName()
                    +" not found");
            int v2 = text.mvd.getVersionByNameAndGroup( withName[0], groups );
            if ( v2 == -1 )
                throw new ParamException("version "+withName[0]+" not found");
            leftText.compareText( text.mvd, v1, v2, ChunkState.DELETED, 
                leftRuns.toArray() );
            rightText.compareText( text.mvd, v2, v1, ChunkState.ADDED, 
                leftRuns.toArray() );
            // store the computed corcodes in the left & right cc arrays
            lCorCodes[lCorCodes.length-2] = leftCC.toString();
            rCorCodes[rCorCodes.length-2] = rightCC.toString();
            lCorCodes[lCorCodes.length-1] = leftText.toString();
            rCorCodes[rCorCodes.length-1] = rightText.toString();
            formats[formats.length-1] = Formats.STIL;
            formats[formats.length-2] = Formats.STIL;
            String[] actualStyles = fetchStyles( styles );
            // now format the text with the corcodes
            JSONResponse lResponse = new JSONResponse();
            JSONResponse rResponse = new JSONResponse();
            new HritFormatter().format( text.mvd.getVersion(v1), lCorCodes, 
                actualStyles, formats, lResponse );
            new HritFormatter().format( text.mvd.getVersion(v2), rCorCodes, 
                actualStyles, formats, rResponse );
            // compose response
            Multipart mmp = new Multipart();
            mmp.putTextParam( MMP.LHS, lResponse.getBody(), MIMETypes.HTML ); 
            mmp.putTextParam( MMP.RHS, rResponse.getBody(), MIMETypes.HTML );
            response.setContentType(MIMETypes.MULTIPART);
            response.getWriter().println(mmp.toString());
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
