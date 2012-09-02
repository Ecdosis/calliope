/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.get.compare;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.exception.*;
import hritserver.path.*;
import hritserver.constants.Params;
import hritserver.constants.Database;
import hritserver.handler.HritMVD;
import java.util.Map;
import hritserver.handler.get.HritGetHandler;
/**
 * A handler for comparing all versions. Output in JSON, HTML.
 * @author desmond
 */
public class CompareAllHandler extends HritGetHandler
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
        String[] base = (String[])map.get( Params.SHORTNAME ); 
        path.setName( Database.CORTEX );
        HritMVD text = loadMVD(path.getResource(true));
        // read all the pairs, creating a merged block where all the versions agree, and split blocks for the stuff in-between
        // at the same time, for each version in the corcode, split blocks if there are further differences
        // do all this using a corcode
        // preserve transposed bits of text
        // finally, format the blocks using the corcode for each version, plus the generated corcode for blocks.
        // output the blocks as HTML
            
    }
}
