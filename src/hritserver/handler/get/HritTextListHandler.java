/*
 * Create a simple groups/shortName list of version names
 */
package hritserver.handler.get;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.exception.*;
import hritserver.constants.Database;
import hritserver.path.Path;
import hritserver.handler.HritMVD;
import hritserver.constants.Params;
import java.util.ArrayList;

/**
 *
 * @author desmond
 */
public class HritTextListHandler extends HritGetHandler
{
    String version1;
    ArrayList<String> groupPath;
    /**
     * Convert a row based on tabs and group-path components, followed by 
     * the short name. Last column is the long name
     * @param row
     * @return 
     */
    private String getVersionPath( String row ) throws HritException
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
            for ( int i=1;i<groupPath.size();i++ )
            {
                sb.append(groupPath.get(i));
                sb.append("/");
            }
            sb.append( cols[cols.length-2] );
            return sb.toString();
        }
        else
            throw new HritException("wrong number of columns: "+cols.length);
    }
    /**
     * Format the raw table form the database into plain text: each version 
     * separated by a comma and preceded by its group path if it is not at 
     * the top level
     * @param rawTable the raw table tabbed and with CRs
     * @return comma-separated list of versions
     */
    private String formatTable( String rawTable ) throws HritException
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
        HttpServletResponse response, String urn ) throws HritException
    {
        Path path = new Path( urn );
        version1 = request.getParameter( Params.VERSION1 );
        path.setName( Database.CORTEX );
        try
        {
            HritMVD mvd = loadMVD( path.getResource(true) );
            String table = mvd.mvd.getVersionTable();
            response.setContentType("text/plain;charset=UTF-8");
            String list = formatTable( table );
            response.getWriter().println( list );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
        }
    }

}
