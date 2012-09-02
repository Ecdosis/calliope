/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
