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
package calliope;
import calliope.handler.get.AeseGetHandler;
import calliope.handler.delete.AeseDeleteHandler;
import calliope.handler.put.AesePutHandler;
import calliope.handler.post.AesePostHandler;
import calliope.exception.AeseException;
import calliope.exception.AeseExceptionMessage;
import calliope.db.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
 
public class AeseServer extends AbstractHandler
{
    public static String user;
    public static String password;
    public static String host;
    public static int dbPort;
    public static int wsPort;
    public static Repository repository;
    static Connection connection;
    AeseServer()
    {
        super();
    }
    /**
     * Main entry point
     * @param target the URN part of the URI
     * @param baseRequest 
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException 
     */
    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        try
        {
            response.setStatus(HttpServletResponse.SC_OK);
            String method = request.getMethod();
            baseRequest.setHandled( true );
            //System.out.println(target);
            if ( method.equals("GET") )
                new AeseGetHandler().handle( request, response, target );
            else if ( method.equals("PUT") )
                new AesePutHandler().handle( request, response, target );
            else if ( method.equals("DELETE") )
                new AeseDeleteHandler().handle( request, response, target );
            else if ( method.equals("POST") )
                new AesePostHandler().handle( request, response, target );
            else
                throw new AeseException("Unknown http method "+method);
            //System.out.println(target);
        }
        catch ( Exception e )
        {
            AeseException he;
            if ( e instanceof AeseException )
                he = (AeseException) e ;
            else
                he = new AeseException( e );
            response.setContentType("text/html");
            response.getWriter().println(new AeseExceptionMessage(he).toString() );
        }
        //response.getWriter().close();
    }
    /**
     * Set the default values for the AeseServer itself
     * @param user the db user name
     * @param password the db password
     * @param host the host domain name where we are running
     * @param dbPort the port used by couchdb
     * @param wsPort the web services port
     * @param rep the type of repository
     */
    public static void setDefaults( String user, String password, String host, 
        int dbPort, int wsPort, Repository rep )
    {
        AeseServer.user = user;
        AeseServer.password = password;
        AeseServer.host = host;
        AeseServer.dbPort = dbPort;
        AeseServer.wsPort = wsPort;
        AeseServer.repository = rep;
    }
    /**
     * Read the commandline arguments
     * @param args the arguments passed into java
     */
    static boolean readArgs( String[] args )
    {
        boolean sane = true;
        // set up defaults
        AeseServer.wsPort = 8080;
        AeseServer.dbPort = 5984;
        AeseServer.host = "localhost";
        AeseServer.password = "";
        AeseServer.user = null;
        for ( int i=0;i<args.length;i++ )
        {
            if ( args[i].charAt(0)=='-' && args[i].length()==2 )
            {
                if ( args.length>i+1 )
                {
                    if ( args[i].charAt(1) == 'u' )
                        AeseServer.user = args[i+1];
                    else if ( args[i].charAt(1) == 'p' )
                        AeseServer.password = args[i+1];
                    else if ( args[i].charAt(1) == 'h' )
                        AeseServer.password = args[i+1];
                    else if ( args[i].charAt(1) == 'd' )
                        AeseServer.dbPort = Integer.parseInt(args[i+1]);
                    else if ( args[i].charAt(1) == 'w' )
                        AeseServer.wsPort = Integer.parseInt(args[i+1]);
                    else
                        sane = false;
                } 
                else
                    sane = false;
            }
            if ( !sane )
                break;
        }
        return sane;
    }
    /**
     * Tell user how to invoke it on commandline
     */
    private static void usage()
    {
        System.out.println( "java -jar calliope.jar [-u user] [-p password]"
            +"[-h host] [-d db-port] [-w ws-port]" );
    }
    /**
     * Launch the AeseServer
     * @throws Exception 
     */
    public static void launchServer() throws Exception
    {
        AeseServerThread p = new AeseServerThread();
        p.start();
    }
    /**
     * Get the connection object
     * @return an active Connection
     */
    public static Connection getConnection() throws AeseException
    {
        if ( connection == null )
        {
            switch ( repository )
            {
                case COUCH:
                    connection = new CouchConnection(
                        user,password,host, dbPort,wsPort );
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
        return connection;
    }
    /**
     * Launch the Jetty service
     * @param args unused
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        if ( readArgs(args) )
            launchServer();
        else
            usage();
    }
}