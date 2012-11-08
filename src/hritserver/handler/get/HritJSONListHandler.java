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
package hritserver.handler.get;

import hritserver.constants.Database;
import hritserver.constants.Params;
import hritserver.exception.HritException;
import hritserver.handler.HritMVD;
import hritserver.path.Path;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
/**
 * Handle production of a JSON formatted list of versions
 * @author desmond
 */
public class HritJSONListHandler  extends HritTextListHandler
{
    /**
     * Get the VersionID by combining the current groups set and the shortname
     * @param groups the currently defined groups
     * @param shortName the version short name
     * @return a valid VersionID
     */
    private String getVersionID( ArrayList<String> groups, String shortName )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<groups.size();i++ )
        {
            sb.append("/");
            sb.append(groups.get(i));
        }
        if ( sb.length()>0 )
            sb.append("/");
        sb.append( shortName );
        return sb.toString();
    }
    /**
     * Format the raw text table into JSON
     * @param table the raw table returned by nmerge
     * @return a JSON version of same
     */
    private String formatTable( String table )
    {
        StringBuilder sb = new StringBuilder();
        String[] lines = table.split("\n");
        if ( lines.length > 0 )
        {
            sb.append("{ \"description\": \"" );
            sb.append( lines[0] );
            sb.append("\", \"versions\": [ ");
            ArrayList<String> groups = new ArrayList<String>();
            for ( int i=1;i<lines.length;i++ )
            {
                String[] cols = lines[i].split("\t");
                if ( cols.length > 1 )
                {
                    for ( int j=0;j<cols.length-2;j++ )
                    {
                        if ( cols[j].length()>0 )
                        {
                            if ( j<groups.size() )
                                groups.set(j,cols[j]);
                            else
                                groups.add( cols[j]);
                        }
                    }
                    sb.append("{ ");
                    sb.append("\"version\": ");
                    sb.append("\"" );
                    sb.append( getVersionID(groups,cols[cols.length-2]) );
                    sb.append("\"" );
                    sb.append(", ");
                    sb.append("\"longname\": ");
                    sb.append("\"" );
                    sb.append( cols[cols.length-1] );
                    sb.append("\"" );
                    sb.append(" }");
                    if ( i < lines.length-1 )
                        sb.append(", ");
                }
            }
            sb.append(" ]");
            sb.append(" }");
        }
        return sb.toString();
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        Path path = new Path( urn );
        path.setName( Database.CORTEX );
        try
        {
            if ( !path.isEmpty() )
            {
                HritMVD mvd = loadMVD( path.getResource() );
                String table = mvd.mvd.getVersionTable();
                response.setContentType("text/plain;charset=UTF-8");
                String list = formatTable( table );
                response.getWriter().println( list );
            }
            else
                throw new HritException("Invalid path "+path.getResource());
        }
        catch ( Exception e )
        {
            throw new HritException(e);
        }
    }
}
