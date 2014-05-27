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
package calliope.handler.get;
import calliope.handler.get.compare.AeseComparisonHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.AeseFormatter;
import calliope.json.corcode.STILDocument;
import calliope.json.corcode.Range;
import calliope.json.JSONResponse;
import calliope.Utils;
import calliope.constants.*;
import calliope.exception.*;
import calliope.path.*;
import calliope.handler.AeseVersion;
import calliope.tests.html.HTMLComment;
import edu.luc.nmerge.mvd.MVD;
import edu.luc.nmerge.mvd.Pair;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.BitSet;
/**
 *
 * @author desmond
 */
public class AeseHTMLHandler extends AeseGetHandler
{
    /**
     * Get the HTML for the given path
     * @param request the request to read from
     * @param urn the original URN, minus the prefix
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        String first = Path.first(urn);
        if ( first.equals(Services.COMPARISON) )
        {
            new AeseComparisonHandler().handle(request,response,Path.pop(urn));
        }
        else if ( first.equals(Services.LIST))
        {
            new AeseListHandler().handle(request,response,Path.pop(urn));
        }
        else if ( first.equals(Services.TABLE) )
        {
            new AeseTableHandler().handle(request,response,Path.pop(urn));
        }
        else
            handleGetVersion( request, response, urn );
    }
    /**
     * Does one set of versions entirely contain another
     * @param container the putative container
     * @param contained the containee
     * @return true if all the bits of contained are in container
     */
    boolean containsVersions( BitSet container, BitSet contained )
    {
        for (int i=contained.nextSetBit(0);i>=0;i=contained.nextSetBit(i+1)) 
        {
            if ( container.nextSetBit(i)!= i )
                return false;
        }
        return true;
    }
    /**
     * Compute the IDs of spans of text in a set of versions
     * @param corCodes the existing corCodes array
     * @param mvd the MVD to use
     * @param version1 versionID of the base version
     * @param spec a comma-separated list of versionIDs 
     * @return an updated corCodes array
     */
    String[] addMergeIds( String[] corCodes, MVD mvd, String version1, 
        String spec )
    {
        STILDocument doc = new STILDocument();
        int base = mvd.getVersionByNameAndGroup(
            Utils.getShortName(version1),
            Utils.getGroupName(version1));
        ArrayList<Pair> pairs = mvd.getPairs();
        BitSet merged = mvd.convertVersions( spec );
        int start = -1;
        int len = 0;
        int pos = 0;
        int id = 1;
        for ( int i=0;i<pairs.size();i++ )
        {
            Pair p = pairs.get( i );
            if ( p.versions.nextSetBit(base)==base )
            {
                if ( containsVersions(p.versions,merged) )
                {
                    if ( start == -1 )
                        start = pos;
                    len += p.length();
                }
                else if ( start != -1 )
                {
                    // add range with annotation to doc
                    try
                    {
                        // see diffs/default corform
                        Range r = new Range("merged", start, len );
                        r.addAnnotation( "mergeid", "v"+id );
                        id++;
                        doc.add( r );
                        start = -1;
                        len = 0;
                    }
                    catch ( Exception e )
                    {
                        // ignore it: we just failed to add that range
                        start = -1;
                        len = 0;
                    }
                }
                // the position within base
                pos += p.length();
            }
        }
        // coda: in case we have a part-fulfilled range
        if ( start != -1 )
        {
            try
            {
                Range r = new Range("merged", start, len );
                r.addAnnotation( "mergeid", "v"+id );
                id++;
                doc.add( r );
            }
            catch ( Exception e )
            {
                // ignore it: we just failed to add that range
            }
        }
        // add new CorCode to the set
        String[] newCCs = new String[corCodes.length+1];
        newCCs[newCCs.length-1] = doc.toString();
        for ( int i=0;i<corCodes.length;i++ )
            newCCs[i] = corCodes[i];
        return newCCs;
    }
    /**
     * Format the requested URN version as HTML
     * @param request the original http request
     * @param urn the original request urn
     * @return the converted HTML
     * @throws AeseException 
     */
    protected void handleGetVersion( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws AeseException
    {
        String version1 = request.getParameter( Params.VERSION1 );
        if ( version1 == null )
        {
            try
            {
                response.getWriter().println(
                    "<p>version1 parameter required</p>");
            }
            catch ( Exception e )
            {
                throw new AeseException( e );
            }
        }
        else
        {
            String selectedVersions = request.getParameter( 
                Params.SELECTED_VERSIONS );
            //System.out.println("version1="+version1);
            AeseVersion corTex = doGetResourceVersion( Database.CORTEX, urn, version1 );
            // 1. get corcodes and styles
            Map map = request.getParameterMap();
            String[] corCodes = getEnumeratedParams( Params.CORCODE, map, true );
            String[] styles = getEnumeratedParams( Params.STYLE, map, false );
            String[] formats = new String[corCodes.length];
            HashSet<String> styleSet = new HashSet<String>();
            for ( int i=0;i<styles.length;i++ )
                styleSet.add( styles[i] );
            try
            {
                for ( int i=0;i<corCodes.length;i++ )
                {
                    String ccResource = Utils.canonisePath(urn,corCodes[i]);
                    AeseVersion hv = doGetResourceVersion( Database.CORCODE, 
                        ccResource, version1 );
                    HTMLComment comment = new HTMLComment();
                    comment.addText( "version-length: "+hv.getVersionLength() );
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().println( comment.toString() );
                    styleSet.add( hv.getStyle() );
                    corCodes[i] = new String(hv.getVersion(),"UTF-8");
                    formats[i] = hv.getContentFormat();
                }
            }
            catch ( Exception e )
            {
                // this won't ever happen because UTF-8 is always supported
                throw new AeseException( e );
            }
            // 2. add mergeids if needed
            if ( selectedVersions != null && selectedVersions.length()>0 )
            {
                corCodes = addMergeIds( corCodes, corTex.getMVD(), version1, 
                    selectedVersions );
                styleSet.add( "diffs/default" );
                String[] newFormats = new String[formats.length+1];
                System.arraycopy( formats, 0, newFormats, 0, formats.length );
                newFormats[formats.length] = "STIL";
                formats = newFormats;
            }
            // 3. recompute styles array (docids)
            styles = new String[styleSet.size()];
            styleSet.toArray( styles );
            // 4. convert style names to actual corforms
            styles = fetchStyles( styles );
            // 5. call the native library to format it
            JSONResponse html = new JSONResponse(JSONResponse.HTML);
            byte[] text = corTex.getVersion();
    //        // debug
//            try{
//                String textString = new String(text,"UTF-8");
//                System.out.println(textString);
//            }catch(Exception e){}
            // end
//            if ( text.length==30712 )
//            {
//                try
//                {
//                    String textStr = new String( text, "UTF-8");
//                    System.out.println(textStr );
//                }
//                catch ( Exception e )
//                {
//                }
//            }
            int res = new AeseFormatter().format( 
                text, corCodes, styles, formats, html );
            if ( res == 0 )
                throw new NativeException("formatting failed");
            else
            {
                response.setContentType("text/html;charset=UTF-8");
                try
                {
                    HTMLComment comment = new HTMLComment();
                    comment.addText( "styles: ");
                    for ( int i=0;i<styles.length;i++ )
                        comment.addText( styles[i] );
                    response.getWriter().println( comment.toString() );
                    response.getWriter().println(html.getBody());   
                }
                catch ( Exception e )
                {
                    throw new AeseException( e );
                }
            }
        }
    }
}
