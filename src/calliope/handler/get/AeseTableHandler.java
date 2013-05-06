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
import calliope.path.Path;
import calliope.constants.Params;
import calliope.constants.Database;
import calliope.handler.AeseMVD;
import java.util.Map;

/**
 * Generate a variant table for a range in the base version
 * @author desmond
 */
public class AeseTableHandler extends AeseGetHandler
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
    /** first ID of aligned table cell (default 0 )*/
    int firstID;
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
        if ( values == null || values[0].length()==0 )
            return defaultValue;
        else
            return values[0];
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        Map map = request.getParameterMap();
        offset = getIntegerOption( map, Params.OFFSET, 0 );
        length = getIntegerOption( map, Params.LENGTH, 100 );
        wholeWords = getBooleanOption( map, Params.WHOLE_WORDS, false );
        compact = getBooleanOption( map, Params.COMPACT, false );
        hideMerged = getBooleanOption( map, Params.HIDE_MERGED, false );
        someVersions = getBooleanOption( map, Params.SOME_VERSIONS, false );
        firstID = getIntegerOption( map, Params.FIRSTID, 0 );
        if ( someVersions )
            selectedVersions = getStringOption(map, Params.SELECTED_VERSIONS,ALL );
        else
            selectedVersions = ALL;
        try
        {
            String shortName="";
            String groups = "";
            String baseVersion=null;
            AeseMVD mvd = loadMVD( Database.CORTEX, urn );
            if ( selectedVersions.equals(ALL) )
                baseVersion = mvd.version1;  
            else
            {
                String[] parts = selectedVersions.split(",");
                if ( parts.length>=1 )
                    baseVersion = parts[0];
            }
            if ( baseVersion != null )
            {
                int pos = baseVersion.lastIndexOf("/");
                if ( pos != -1 )
                {
                    shortName = baseVersion.substring(pos+1);
                    groups = baseVersion.substring(0,pos);
                }
                else
                    shortName = baseVersion;
            }
            this.base = (short)mvd.mvd.getVersionByNameAndGroup( shortName, 
                groups );
            if ( base == 0 )
            {
                System.out.println("version "+shortName+" in group "
                    +groups+" not found. Substituting 1");
                base = 1;
            }
            String table = mvd.mvd.getTableView( base,offset,length,
                compact,hideMerged,wholeWords,selectedVersions,firstID,
                "apparatus" );
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println( table );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
