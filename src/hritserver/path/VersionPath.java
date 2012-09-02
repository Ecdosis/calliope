/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
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
package hritserver.path;
import hritserver.exception.*;
import hritserver.HritServer;
import hritserver.handler.HritMVD;
import hritserver.json.JSONDocument;
import hritserver.constants.JSONKeys;
import edu.luc.nmerge.mvd.MVDFile;
/**
 * A variety of Path that strips version information from a urn
 * @author desmond
 */
public class VersionPath extends Path
{
    /** The version short name which must be unique to the group */
    protected String shortName;
    /** a list of group-names, separate by "/" and terminated by "/" */
    protected String groups;
    /**
     * When version and/or groups are lacking supply the first one
     * @param response the json document already fetched
     */
    private void setVersionAndGroups( String response ) 
    {
        HritMVD hMvd = new HritMVD();
        JSONDocument doc = JSONDocument.internalise(response);
        if ( doc != null )
        {
            String body = (String)doc.get(JSONKeys.BODY);
            if ( body != null )
            {
                hMvd.mvd = MVDFile.internalise( body );
                hMvd.format = (String)doc.get(JSONKeys.FORMAT);
                String table = hMvd.mvd.getVersionTable();
                String[] lines = table.split("\n");
                if ( lines.length > 1 )
                {
                    String[] cols = lines[1].split("\t");
                    if ( cols.length>1 )
                    {
                        StringBuilder sb = new StringBuilder();
                        // ignore base group
                        for ( int i=1;i<cols.length-2;i++ )
                        {
                            if ( cols[i].length()>0 )
                            {
                                sb.append( "/" );
                                sb.append( cols[i] );
                            }
                        }
                        groups = sb.toString();
                        shortName = cols[cols.length-2];
                    }
                }
            }
        }
    }
    /**
     * Construct a path containing version information at the end
     * @param urn the raw urn from the user, non-canonicalised
     */
    public VersionPath( String urn ) throws HritException
    {
        super( urn );
        String response = null;
        shortName = "";
        groups = "";
        while ( !isEmpty() )
        {
            try
            {
                byte[] data = HritServer.getFromDb(
                    "/"+getDbName()+"/"+resource);
                if ( data != null )
                {
                    response = new String(data,"UTF-8");
                    break;
                }
                else
                {
                    String popped = chomp();
                    if ( shortName.length()==0 )
                        shortName = popped;
                    else if ( groups.length() > 0 )
                        groups = groups+"/"+popped;
                    else
                        groups = popped;
                }
            }
            catch ( Exception e )
            {
                throw new HritException( e );
            }
        }
        if ( isEmpty() )
            throw new PathException( "Invalid path" ); 
        else if ( shortName.length()==0 && response != null )
            setVersionAndGroups( response );
    }
    /**
     * Get the resource path but append versions if requested
     * @return the resource as a path
     */
    public String getResourcePath( boolean appendVersions )
    {
        if ( appendVersions )
        {
            StringBuilder sb = new StringBuilder( this.path );
            if ( groups.length()>0 )
            {
                sb.append("/");
                sb.append( groups );
                sb.append("/");
            }
            sb.append("/");
            sb.append( shortName );
            return sb.toString();
        }
        else
            return super.getResourcePath();
    }
    /**
     * Get the groups string
     * @return a String
     */
    public String getGroups()
    {
        return this.groups;
    }
    /**
     * Get the path's version short-name component 
     * @return a String
     */
    public String getShortName()
    {
        return this.shortName;
    }
}
