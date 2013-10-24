/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get;

import calliope.constants.Params;
import calliope.exception.AeseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.store.RAMDirectory;
/**
 * Search for an expression in the database contents
 * @author desmond
 */
public class AeseSearchHandler  extends AeseGetHandler
{
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
        String terms = request.getParameter( Params.TERMS );
        if ( terms != null )
        {
            if ( index == null )
            {
            }
        }
    }
}
