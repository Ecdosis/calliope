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
package hritserver;
import hritserver.handler.get.HritGetHandler;
import hritserver.handler.delete.HritDeleteHandler;
import hritserver.handler.put.HritPutHandler;
import hritserver.handler.post.HritPostHandler;
import hritserver.exception.HritException;
import hritserver.exception.HritErrorMessage;
import hritserver.constants.MIMETypes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
 
public class HritServer extends AbstractHandler
{
    public static String user;
    public static String password;
    public static String host;
    public static int dbPort;
    public static int wsPort;
    /** time to wait after read for any data at start */
    static long bigTimeout = 1000;// milliseconds
    /** time to wait after read for any more data */
    static long smallTimeout = 30;// milliseconds
    static String DEFAULT_STYLE = "TEI";
    HritServer()
    {
        super();
    }
    /**
     * Fetch a resource from the server, or try to.
     * @param path the path to the reputed resource
     * @return the response as a string
     */
    public static byte[] getFromDb( String path )
    {
        try
        {
            String login = (HritServer.user==null)?"":user+":"+password+"@";
            URL u = new URL("http://"+login+host+":"+dbPort+path);
            URLConnection conn = u.openConnection();
            InputStream is = conn.getInputStream();
            ByteHolder bh = new ByteHolder();
            long timeTaken=0,start = System.currentTimeMillis();
            // HttpURLConnection seems to use non-blocking I/O
            while ( timeTaken <bigTimeout && (is.available()>0
                ||timeTaken<smallTimeout) )
            {
                if ( is.available()>0 )
                {
                    byte[] data = new byte[is.available()];
                    is.read( data );
                    bh.append( data );
                    // restart timeout
                    timeTaken = 0;
                }
                else
                    timeTaken = System.currentTimeMillis()-start;
            }
            is.close();
            if ( bh.isEmpty() )
                System.out.println("failed to fetch resource "+path);
            //System.out.println("time taken to fetch from couch: "+timeTaken );
            return bh.getData();
        }
        catch ( Exception e )
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(
                    System.getProperty("java.io.tmpdir")+"hritserver.log",true);
                PrintWriter pw = new PrintWriter(fos);
                e.printStackTrace( pw );
                pw.close();
            }
            catch ( Exception ee )
            {
                // do nothing in this case
            }
            return null;
        }
    }
    /**
     * Get a document's revid
     * @param path the path of the document including its database
     * @return a string or nul to indicate it isn't there
     */
    public static String getRevId( String path ) throws HritException
    {
        HttpURLConnection conn = null;
        try
        {
            path = path.replace(" ","%20");
            String login = (HritServer.user==null)?"":user+":"+password+"@";
            URL u = new URL("http://"+login+host+":"+dbPort+path);
            conn = (HttpURLConnection)u.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setRequestProperty("Content-Type",MIMETypes.JSON);
            conn.setUseCaches (false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            //Get Response	
            String revid = conn.getHeaderField("ETag");
            conn.disconnect(); 
            return revid;
        } 
        catch (Exception e) 
        {
            if (conn != null) 
                conn.disconnect(); 
            throw new HritException( e );
        } 
    }
    /**
     * Add a revid to a json document
     * @param json the json in question
     * @param revid the revid of its current incarnation 
     * @return the rebuilt json file
     */
    static String addRevId( String json, String revid )
    {
        StringBuilder sb = new StringBuilder( json );
        int pos = sb.indexOf( "{" );
        if ( pos != -1 )
        {
            sb.insert(pos+1,"\n\t\"_rev\": "+revid+",");
        }
        return sb.toString();
    }
    /**
     * PUT a json file to the database
     * @param path the full path of the resource including database
     * @param json the json to put there
     * @return the server response
     */
    public static String putToDb( String path, String json ) 
        throws HritException
    {
        HttpURLConnection conn = null;
        try
        {
            path = path.replace(" ","%20");
            String login = (HritServer.user==null)?"":user+":"+password+"@";
            String url = "http://"+login+host+":"+dbPort+path;
            String revid = getRevId( path );
            if ( revid != null )
                json = HritServer.addRevId( json, revid );
            URL u = new URL(url);
            conn = (HttpURLConnection)u.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type",MIMETypes.JSON);
            byte[] jData = json.getBytes();
			conn.setRequestProperty("Content-Length", Integer.toString(
                jData.length));
            conn.setRequestProperty("Content-Language", "en-US");  
			conn.setUseCaches (false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream( conn.getOutputStream() );
            wr.writeBytes( json );
            wr.flush ();
            wr.close ();
            //Get Response	
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder(); 
            while ((line = rd.readLine()) != null) 
            {
                response.append(line);
                response.append('\r');
            }
            is.close();
            rd.close();
            conn.disconnect(); 
            conn = null;
            return response.toString();
        } 
        catch (Exception e) 
        {
            if (conn != null) 
                conn.disconnect(); 
            throw new HritException( e );
        } 
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
            if ( method.equals("GET") )
                new HritGetHandler().handle( request, response, target );
            else if ( method.equals("PUT") )
                new HritPutHandler().handle( request, response, target );
            else if ( method.equals("DELETE") )
                new HritDeleteHandler().handle( request, response, target );
            else if ( method.equals("POST") )
                new HritPostHandler().handle( request, response, target );
            else
                throw new HritException("Unknown http method "+method);
            //System.out.println(target);
        }
        catch ( Exception e )
        {
            HritException he;
            if ( e instanceof HritException )
                he = (HritException) e ;
            else
                he = new HritException( e );
            response.setContentType("text/html");
            response.getWriter().println(new HritErrorMessage(he).toString() );
        }
        //response.getWriter().close();
    }
    /**
     * Set the default values for the HritServer itself
     * @param user the db user name
     * @param password the db password
     * @param host the host domain name where we are running
     * @param dbPort the port used by couchdb
     * @param wsPort the web services port
     */
    public static void setDefaults( String user, String password, String host, 
        int dbPort, int wsPort )
    {
        HritServer.user = user;
        HritServer.password = password;
        HritServer.host = host;
        HritServer.dbPort = dbPort;
        HritServer.wsPort = wsPort;
    }
    /**
     * Read the commandline arguments
     * @param args the arguments passed into java
     */
    static boolean readArgs( String[] args )
    {
        boolean sane = true;
        // set up defaults
        HritServer.wsPort = 8080;
        HritServer.dbPort = 5984;
        HritServer.host = "localhost";
        HritServer.password = "";
        HritServer.user = null;
        for ( int i=0;i<args.length;i++ )
        {
            if ( args[i].charAt(0)=='-' && args[i].length()==2 )
            {
                if ( args.length>i+1 )
                {
                    if ( args[i].charAt(1) == 'u' )
                        HritServer.user = args[i+1];
                    else if ( args[i].charAt(1) == 'p' )
                        HritServer.password = args[i+1];
                    else if ( args[i].charAt(1) == 'h' )
                        HritServer.password = args[i+1];
                    else if ( args[i].charAt(1) == 'd' )
                        HritServer.dbPort = Integer.parseInt(args[i+1]);
                    else if ( args[i].charAt(1) == 'w' )
                        HritServer.wsPort = Integer.parseInt(args[i+1]);
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
        System.out.println( "java -jar hritserver.jar [-u user] [-p password]"
            +"[-h host] [-d db-port] [-w ws-port]" );
    }
    /**
     * Launch the HritServer
     * @throws Exception 
     */
    public static void launchServer() throws Exception
    {
        HritServerThread p = new HritServerThread();
        p.start();
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