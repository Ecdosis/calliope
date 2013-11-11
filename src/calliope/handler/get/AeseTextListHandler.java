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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.exception.*;
import calliope.constants.Database;
import calliope.path.Path;
import calliope.handler.AeseMVD;
import calliope.constants.Params;
import java.util.ArrayList;

/**
 * Bare bones text list of versions (not HTML)
 * @author desmond
 */
public class AeseTextListHandler extends AeseGetHandler
{
    String version1;
    ArrayList<String> groupPath;
    /**
     * Convert a row based on tabs and group-path components, followed by 
     * the short name. Last column is the long name
     * @param row
     * @return 
     */
    private String getVersionPath( String row ) throws AeseException
    {
        StringBuilder sb = new StringBuilder();
        String[] cols = row.split("\t");
        if ( cols.length >= 2 )
        {
            if ( groupPath == null )
                groupPath = new ArrayList<String>();
            // build groupPath
            for ( int i=0;i<cols.length-2;i++ )
            {
                if ( !cols[i].isEmpty() )
                {
                    if ( groupPath.size()>i )
                        groupPath.set( i, cols[i] );
                    else
                        groupPath.add( i, cols[i]);
                }
            }
            while ( groupPath.size() > cols.length-2 )
                groupPath.remove( groupPath.size()-1 );
            // build full string
            if ( groupPath.size()>0 )
                sb.append("/");
            for ( int i=0;i<groupPath.size();i++ )
            {
                sb.append(groupPath.get(i));
                sb.append("/");
            }
            sb.append( cols[cols.length-2] );
            return sb.toString();
        }
        else
            throw new AeseException("wrong number of columns: "+cols.length);
    }
    /**
     * Format the raw table from the database into plain text: each version 
     * separated by a comma and preceded by its group path if it is not at 
     * the top level
     * @param rawTable the raw table tabbed and with CRs
     * @return comma-separated list of versions
     */
    private String formatTable( String rawTable ) throws AeseException
    {
        StringBuilder sb = new StringBuilder();
        String[] lines = rawTable.split("\n");
        for ( int i=1;i<lines.length;i++ )
        {
            String path = getVersionPath( lines[i] );
            if ( path.contains(",") )
                path = path.replace(",","\\,");
            sb.append( path );
            if ( i < lines.length-1 )
                sb.append(",");
        }
        return sb.toString();
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        version1 = request.getParameter( Params.VERSION1 );
        try
        {
            if ( urn.length()>0 )
            {
                AeseMVD mvd = loadMVD( Database.CORTEX, urn );
                String table = mvd.mvd.getVersionTable();
                response.setContentType("text/plain;charset=UTF-8");
                String list = formatTable( table );
                response.getWriter().println( list );
            }
            else
                throw new AeseException("Invalid path "+urn);
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }
    }

}
