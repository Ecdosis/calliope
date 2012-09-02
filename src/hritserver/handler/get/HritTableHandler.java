/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.get;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.exception.*;
import hritserver.path.Path;
import hritserver.constants.Params;
import hritserver.constants.Database;
import hritserver.handler.HritMVD;
import java.util.Map;

/**
 * Generate a variant table for a range in the base version
 * @author desmond
 */
public class HritTableHandler extends HritGetHandler
{
    private static String ALL = "all";
    /** offset into base version */
    int offset;
    /** length of range to compute table for */
    int length;
    /** Base version */
    short base;
    /** hide merged sections in all but base version */
    boolean hideMerged;
    /** compact versions where possible */
    boolean compact;
    /** expand diffs to whole words */
    boolean wholeWords;
    /** false if all versions are wanted */
    boolean someVersions;
    /** comma-separated string of all selected versions */
    String selectedVersions;
    int getIntegerOption( Map map, String key, int defaultValue )
    {
        String[] values = (String[])map.get( key );
        if ( values == null )
        {
            values = new String[1];
            values[0] = Integer.toString(defaultValue);
        }
        return Integer.parseInt(values[0]);
    }
    /**
     * "boolean" options will be 1 or 0
     * @param map the map of passed in params
     * @param key the parameter key
     * @param defaultValue its default value
     * @return true or false
     */
    boolean getBooleanOption( Map map, String key, boolean defaultValue )
    {
        String[] values = (String[])map.get( key );
        if ( values == null )
        {
            int defValue;
            values = new String[1];
            if ( defaultValue )
                defValue = 1;
            else
                defValue = 0;
            values[0] = Integer.toString(defValue);
        }
        int value = Integer.parseInt(values[0]);
        return value==1;
    }
    /**
     * String options are just literals
     * @param map the map of passed in params
     * @param key the parameter key
     * @param defaultValue its default value
     * @return a String
     */
    String getStringOption( Map map, String key, String defaultValue )
    {
        String[] values = (String[])map.get( key );
        if ( values == null )
            return defaultValue;
        else
            return values[0];
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        Path path = new Path( urn );
        Map map = request.getParameterMap();
        offset = getIntegerOption( map, Params.OFFSET, 0 );
        length = getIntegerOption( map, Params.LENGTH, 100 );
        wholeWords = getBooleanOption( map, Params.WHOLE_WORDS, false );
        compact = getBooleanOption( map, Params.COMPACT, false );
        hideMerged = getBooleanOption( map, Params.HIDE_MERGED, false );
        someVersions = getBooleanOption( map, Params.SOME_VERSIONS, false );
        if ( someVersions )
            selectedVersions = getStringOption(map, Params.SELECTED_VERSIONS,ALL );
        else
            selectedVersions = ALL;
        String version1 = request.getParameter( Params.VERSION1 );
        if ( version1==null && !selectedVersions.equals(ALL) )
        {
            String[] parts = selectedVersions.split(",");
            if ( parts.length>=1 )
                version1 = parts[0];
        }
        String shortName="";
        String groups = "";
        if ( version1 != null )
        {
            int rPos = version1.lastIndexOf("/");
            if ( rPos == -1 )
                shortName = version1;
            else
            {
                shortName = version1.substring(rPos);
                groups = version1.substring( 0,rPos );
            }
        }
        path.setName( Database.CORTEX );
        try
        {
            HritMVD mvd = loadMVD( path.getResource(true) );
            this.base = (short)mvd.mvd.getVersionByNameAndGroup( shortName, groups );
            if ( base == 0 )
            {
                System.out.println("version "+shortName+" in group "
                    +groups+" not found. Substituting 1");
                base = 1;
            }
            String table = mvd.mvd.getTableView( base,offset,length,
                compact,hideMerged,wholeWords,selectedVersions );
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println( table );
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
