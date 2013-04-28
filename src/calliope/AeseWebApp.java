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
import calliope.handler.AeseHandler;
import calliope.handler.get.AeseGetHandler;
import calliope.handler.put.AesePutHandler;
import calliope.handler.post.AesePostHandler;
import calliope.handler.delete.AeseDeleteHandler;
import calliope.db.Repository;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;

/**
 * WebApp for Tomcat
 * @author desmond
 */
public class AeseWebApp extends HttpServlet
{
    static final String REPOSITORY = "repository";
    static final String USER = "user";
    static final String PASSWORD = "password";
    static final String DBPORT = "dbport";
    static final String WEBROOT = "webroot";
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
        //String repoName = getParameter( config, REPOSITORY, "COUCH" );
        Repository repository = Repository.valueOf( repoName );
        String user = getParameter( config, USER, "admin" );
        String password = getParameter( config, PASSWORD, "jabberw0cky" );
        String dbPortValue = getParameter( config, DBPORT, "27017" );
        //String dbPortValue = getParameter( config, DBPORT, "5984" );
        String webRoot = getParameter( config, WEBROOT, "/var/www" );
        int dbPort = Integer.valueOf( dbPortValue );
        String host = req.getServerName();
        int wsPort = req.getServerPort();
        Connector.init( repository, user, password, host, dbPort, wsPort, 
            webRoot );
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, java.io.IOException
    {
        try
        {
            String method = req.getMethod();
            String target = req.getRequestURI();
            // remove webapp prefix
            if ( target.startsWith("/calliope") )
                target = target.substring( 9 );
            //resp.getWriter().println("<html><body><p>uri="+target
            //    +"</p></body></html>");
            AeseHandler handler;
            if ( !Connector.isOpen() )
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
