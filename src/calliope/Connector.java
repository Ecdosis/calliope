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
 * Handle connections with a database
 * @author desmond
 */
public class Connector 
{
    static Connection connection;
    /**
     * Initialise once per instantiation
     * @param repository the repository type
     * @param user the user name
     * @param password the user's password
     * @param host the domain name of the host
     * @param dbPort the database port
     * @param wsPort the web-service port
     * @param webRoot the full path to the web-root
     * @throws AeseException 
     */
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
    /**
     * Is the connection open?
     * @return true if so
     */
    public static boolean isOpen()
    {
        return connection != null;
    }
}
