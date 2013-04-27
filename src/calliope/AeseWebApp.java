/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope;
import calliope.db.Connection;
import calliope.db.CouchConnection;
import calliope.db.MongoConnection;
import static calliope.db.Repository.COUCH;
import static calliope.db.Repository.MONGO;
import calliope.exception.AeseException;
import calliope.exception.AeseExceptionMessage;
import calliope.handler.get.AeseGetHandler;
import calliope.handler.post.AesePostHandler;
import calliope.handler.delete.AeseDeleteHandler;
import calliope.handler.put.AesePutHandler;
import calliope.handler.AeseHandler;
import calliope.db.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;

/**
 * WebApp for Tomcat
 * @author desmond
 */
public class AeseWebApp extends HttpServlet
{
    /** this should persist across invocations */
    static Connection connection;
    static final String REPOSITORY = "repository";
    static final String USER = "user";
    static final String PASSWORD = "password";
    static final String DBPORT = "dbport";
    private String getParameter( ServletConfig config, String key, 
        String defValue )
    {
        String value = config.getInitParameter( key );
        if ( value == null )
            value = defValue;
        return value;
    }
    /**
     * Open a shared connection
     * @param req the HttpServlet request
     * @throws AeseException 
     */
    private void openConnection( HttpServletRequest req ) throws AeseException
    {
        ServletConfig config = getServletConfig();
        String repoName = getParameter( config, REPOSITORY, "MONGO" );
        Repository repository = Repository.valueOf( repoName );
        String user = getParameter( config, USER, "admin" );
        String password = getParameter( config, PASSWORD, "jabberw0cky" );
        String dbPortValue = getParameter( config, DBPORT, "27017" );
        int dbPort = Integer.valueOf( dbPortValue );
        String host = req.getServerName();
        int wsPort = req.getServerPort();
        switch ( repository )
        {
            case COUCH:
                connection = new CouchConnection(
                    user,password,host,dbPort,wsPort );
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
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, java.io.IOException
    {
        try
        {
            String method = req.getMethod();
            String target = req.getRequestURI();
            AeseHandler handler;
            if ( connection == null )
                openConnection( req );
            if ( method.equals("GET") )
                handler = new AeseGetHandler();
            else if ( method.equals("PUT") )
                handler = new AesePutHandler();
            else if ( method.equals("DELETE") )
                handler = new AeseDeleteHandler();
            else if ( method.equals("POST") )
                handler = new AesePostHandler();
            else
                throw new AeseException("Unknown http method "+method);
            resp.setStatus(HttpServletResponse.SC_OK);
                handler.handle( req, resp, target );
        }
        catch ( AeseException e )
        {
            AeseException he;
            if ( e instanceof AeseException )
                he = (AeseException) e ;
            else
                he = new AeseException( e );
            resp.setContentType("text/html");
            try 
            {
                resp.getWriter().println(
                    new AeseExceptionMessage(he).toString() );
            }
            catch ( Exception e2 )
            {
                e.printStackTrace( System.out );
            }
        }
    }
}
