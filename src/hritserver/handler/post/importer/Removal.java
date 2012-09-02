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

package hritserver.handler.post.importer;
import hritserver.json.JSONDocument;
import hritserver.exception.ConfigException;

/**
 * A specification of an element in TEI that can contain versions. We should 
 * really initialise these from a config file.
 * @author desmond
 */
public class Removal 
{
    private static String NAME = "name";
    private static String VERSIONS = "versions";
    private static String WITS = "wits";
    private static String STRIP = "strip";
    String name;
    String wits;
    Versions versions;
    boolean strip;
    /**
     * Initialise a version set from a config file
     * @param config the JSON document config
     * @throws ConfigException if something vital was missing
     */
    public Removal( JSONDocument config ) throws ConfigException
    {
        this.name = (String)config.get( NAME );
        if ( name == null )
            throw new ConfigException( 
                "missing main element name in removal definition");
        String vValue = (String)config.get( VERSIONS );
        if ( vValue == null )
            this.versions = Versions.none;
        else
            this.versions = Versions.valueOf( vValue );
        // can be null
        this.wits = (String)config.get( WITS );
        try
        {
            if ( config.containsKey(STRIP) )
                this.strip = ((Boolean)config.get(STRIP)).booleanValue();
            else
                this.strip = true;
        }
        catch ( Exception e )
        {
            throw new ConfigException( e );
        }
    }
}
