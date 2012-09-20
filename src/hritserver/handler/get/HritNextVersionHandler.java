/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.get;

import hritserver.constants.Database;
import hritserver.constants.Params;
import hritserver.exception.HritException;
import hritserver.handler.HritMVD;
import hritserver.path.Path;
import java.util.Map;
import hritserver.Utils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get the next version given one as input
 * @author desmond
 */
public class HritNextVersionHandler extends HritGetHandler 
{
    /**
     * Write the next version to the output stream
     * @param request the request
     * @param response the response
     * @param urn the urn of the CorTex
     * @throws HritException 
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        Path path = new Path( urn );
        String version1 = request.getParameter( Params.VERSION1 );
        path.setName( Database.CORTEX );
        try
        {
            String fullName = "";
            if ( version1 != null && version1.length()>0 )
            {
                String shortName = Utils.getShortName( version1 );
                String groups = Utils.getGroupName( version1 );
                HritMVD mvd = loadMVD( path.getResource() );
                int v1 = mvd.mvd.getVersionByNameAndGroup( shortName, groups );
                if ( v1 > 0 )
                {
                    int v2 = mvd.mvd.getNextVersionId( (short)v1 );
                    String groups2 = mvd.mvd.getGroupPath( (short)v2 );
                    String shortName2 = mvd.mvd.getVersionShortName( (short)v2 );
                    fullName = Utils.canonisePath( groups2, shortName2 );
                }
                else
                    throw new HritException( shortName+groups+" not found" );
            }
            else
                throw new HritException( "version1 was empty");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println( fullName );
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
