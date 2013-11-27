/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get;

import calliope.search.AeseSearch;
import calliope.constants.Params;
import calliope.exception.AeseException;
import calliope.search.HitProfile;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Search for an expression in the database contents
 * @author desmond
 */
public class AeseSearchHandler  extends AeseGetHandler
{
    static int DEFAULT_HPP = 20;
    /**
     * Get the JSON for the given path
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        int hitsPerPage = DEFAULT_HPP;
        // optional hits per page
        String hitsPer = request.getParameter( Params.HITS_PER_PAGE );
        if ( hitsPer != null )
        {
            try
            {
                hitsPerPage = Integer.parseInt(hitsPer);
            }
            catch ( Exception e )
            {
                // ignore
            }
        }
        // the search expression
        String expr = request.getParameter( Params.EXPR );
        if ( expr != null )
        {
            HitProfile hp = new HitProfile(0,hitsPerPage-1);
            String res = AeseSearch.searchIndex( expr, 
                Locale.getDefault().getLanguage(), hp );
            response.setContentType("text/html;charset=UTF-8");
            try
            {
                response.getWriter().println(res);   
            }
            catch ( Exception e )
            {
                throw new AeseException( e );
            }
        }
    }
}
