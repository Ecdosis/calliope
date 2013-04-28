/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope;

import calliope.db.Connection;
import calliope.db.CouchConnection;
import calliope.db.MongoConnection;
import calliope.db.Repository;
import calliope.exception.AeseException;

/**
 * Handle connections with database: once per instantiation
 * @author desmond
 */
public class Connector 
{
    static Connection connection;
    public static void init( Repository repository, String user, 
        String password, String host, int dbPort, int wsPort, String webRoot ) 
        throws AeseException
    {
        switch ( repository )
        {
            case COUCH:
                connection = new CouchConnection(
                    user,password,host,dbPort,wsPort,webRoot );
                break;
            case MONGO:
                connection = new MongoConnection(
                    user,password,host, dbPort,wsPort );
                break;
            default:
                throw new AeseException( "Unknown repository type "
                    +repository );
        }
    }
    /**
     * Get the connection object
     * @return an active Connection
     */
    public static Connection getConnection() throws AeseException
    {
        if ( connection == null )
            throw new AeseException( "connection to database was null" );
        return connection;
    }
    public static boolean isOpen()
    {
        return connection != null;
    }
}
