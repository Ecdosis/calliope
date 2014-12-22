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

import calliope.Connector;
import calliope.constants.Params;
import calliope.exception.AeseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get a list of the docids of any collection
 * @author desmond
 */
public class AeseListCollectionHandler extends AeseGetHandler
{
    private String formatList( String[] data )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for ( int i=0;i<data.length;i++ )
        {
            sb.append("\"");
            sb.append(data[i]);
            sb.append("\"");
            if ( i < data.length-1 )
                sb.append(",");
        }
        sb.append(" ]");
        return sb.toString();
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            String collection = request.getParameter(Params.COLLECTION);
            String list = "{ [] }";
            if ( collection != null && collection.length()>0 )
            {
                String[] data = Connector.getConnection().listCollection(
                    collection);
                list = formatList( data );
            }
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().println( list );
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }
    }
}