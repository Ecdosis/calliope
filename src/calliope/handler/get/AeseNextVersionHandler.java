/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get;

import calliope.constants.Database;
import calliope.constants.Params;
import calliope.exception.AeseException;
import calliope.handler.AeseMVD;
import calliope.path.Path;
import calliope.Utils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get the next version given one as input
 * @author desmond
 */
public class AeseNextVersionHandler extends AeseGetHandler 
{
    /**
     * Write the next version to the output stream
     * @param request the request
     * @param response the response
     * @param urn the urn of the CorTex
     * @throws AeseException 
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        String version1 = request.getParameter( Params.VERSION1 );
        try
        {
            String fullName = "";
            if ( version1 != null && version1.length()>0 )
            {
                String shortName = Utils.getShortName( version1 );
                String groups = Utils.getGroupName( version1 );
                AeseMVD mvd = loadMVD( Database.CORTEX, urn );
                int v1 = mvd.mvd.getVersionByNameAndGroup( shortName, groups );
                if ( v1 > 0 )
                {
                    int v2 = mvd.mvd.getNextVersionId( (short)v1 );
                    String groups2 = mvd.mvd.getGroupPath( (short)v2 );
                    String shortName2 = mvd.mvd.getVersionShortName( (short)v2 );
                    fullName = Utils.canonisePath( groups2, shortName2 );
                }
                else
                    throw new AeseException( shortName+groups+" not found" );
            }
            else
                throw new AeseException( "version1 was empty");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println( fullName );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
