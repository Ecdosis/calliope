/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.db;

import calliope.AeseServer;
import calliope.ByteHolder;
import calliope.constants.MIMETypes;
import calliope.exception.AeseException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;

/**
 * Implementation of database Connection interface for CouchDB
 * @author desmond
 */
public class CouchConnection extends Connection 
{
    /** time to wait after read for any data at start */
    static long bigTimeout = 2000;// milliseconds
    /** time to wait after read for any more data */
    static long smallTimeout = 200;// milliseconds
    static String webRoot;
    public CouchConnection( 
        String user, String password, String host, 
        int dbPort, int wsPort, String webRoot )
    {
        super( user, password, host, dbPort, wsPort );
        this.webRoot = webRoot;
    }
    /**
     * Fetch a resource from the server, or try to.
     * @param path the path to the reputed resource
     * @return the response as a string or null if not found
     */
    @Override
    public String getFromDb( String path )
    {
        //long startTime = System.currentTimeMillis();
        try
        {
            String login = (user==null)?"":user+":"+password+"@";
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
            {
                System.out.println("failed to fetch resource "+path);
                return null;
            }
            //System.out.println("time taken to fetch from couch: "+(System.currentTimeMillis()-startTime) );
            else
                return new String( bh.getData(), "UTF-8" );
        }
        catch ( Exception e )
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(
                    System.getProperty("java.io.tmpdir")+File.pathSeparator
                    +"calliope.log",true);
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
     * @return a string or null to indicate it isn't there
     */
    private String getRevId( String path ) throws AeseException
    {
        HttpURLConnection conn = null;
        try
        {
            path = path.replace(" ","%20");
            String login = (user==null)?"":user+":"+password+"@";
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
            if ( revid != null )
                revid = revid.replaceAll("\"","");
            conn.disconnect(); 
            return revid;
        } 
        catch (Exception e) 
        {
            if (conn != null) 
                conn.disconnect(); 
            throw new AeseException( e );
        } 
    }
    /**
     * Add a revid to a json document
     * @param json the json in question
     * @param revid the revid of its current incarnation 
     * @return the rebuilt json file
     */
    private String addRevId( String json, String revid )
    {
        StringBuilder sb = new StringBuilder( json );
        int pos = sb.indexOf( "{" );
        if ( pos != -1 )
        {
            sb.insert(pos+1,"\n\t\"_rev\": \""+revid+"\",");
        }
        return sb.toString();
    }
    /**
     * Read the response of the server
     * @param conn an open connection
     * @return the server's response or the empty string
     * @throws IOException 
     */
    private String readResponse( HttpURLConnection conn, long delay ) throws Exception
    {
        if ( delay < 50 )
        {
            try
            {
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
            catch ( Exception e )
            {
                Thread.sleep( 10 );
                return readResponse( conn, delay+10 );
            }
        }
        else
            return "";
    }
    /**
     * Remove a document from the database
     * @param path the full path of the resource including database
     * @param json the json to put there
     * @return the server response
     */
    @Override
    public String removeFromDb( String path ) throws AeseException
    {
        HttpURLConnection conn = null;
        try
        {
            path = path.replace(" ","%20");
            String login = (user==null)?"":user+":"+password+"@";
            String revid = getRevId( path );
            if ( revid != null && revid.length()> 0 )
            {
                String url = "http://"+login+host+":"+dbPort+path+"?rev="+revid;
                URL u = new URL(url);
                conn = (HttpURLConnection)u.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setUseCaches (false);
                conn.setDoOutput(true);
                //Get Response	
                return readResponse( conn, 0L );
            }
            else // it's not there, so do nothing
                return "";
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
    }
    /**
     * PUT a json file to the database
     * @param path the full path of the resource including database
     * @param json the json to put there
     * @return the server response
     */
    @Override
    public String putToDb( String path, String json ) throws AeseException
    {
        HttpURLConnection conn = null;
        try
        {
            path = path.replace(" ","%20");
            String login = (user==null)?"":user+":"+password+"@";
            String url = "http://"+login+host+":"+dbPort+path;
            String revid = getRevId( path );
            if ( revid != null )
                json = addRevId( json, revid );
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
            return readResponse( conn, 0L );
        } 
        catch (Exception e) 
        {
            if (conn != null) 
                conn.disconnect(); 
            throw new AeseException( e );
        } 
    }
    /**
     * Get an image from the database
     * @param path the path to the corpix
     * @return the image data
     */
    @Override
    public byte[] getImageFromDb( String path ) throws AeseException
    {
        try
        {
            File wd = new File( CouchConnection.webRoot );
            File f = new File( wd, path );
            if ( f.exists() )
            {
                int len = (int)f.length();
                byte[] data = new byte[len];
                FileInputStream fis = new FileInputStream( f );
                fis.read( data );
                fis.close();
                return data;
            }
            else
                throw new AeseException( "File not found "+f.getAbsolutePath() );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Save a file to the file system
     * @param path the path to the file
     * @param data the data of the file
     * @throws AeseException 
     */
    @Override
    public void putImageToDb( String path, byte[] data ) throws AeseException
    {
        try
        {
            File wd = new File( CouchConnection.webRoot );
            File child = new File( wd, path );
            if ( !child.getParentFile().exists() )
                child.getParentFile().mkdirs();
            if ( child.exists() )
                child.delete();
            child.createNewFile();
            FileOutputStream fos = new FileOutputStream( child );
            fos.write( data );
            fos.close();
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Delete a file from the file system
     * @param path the relative path to the file
     * @throws AeseException 
     */
    @Override
    public void removeImageFromDb( String path ) throws AeseException
    {
        try
        {
            File wd = new File( CouchConnection.webRoot );
            File f = new File( wd, path );
            if ( f.exists() )
            {
                f.delete();
                File parent = f.getParentFile();
                do
                {
                    File[] children = parent.listFiles();
                    if ( children.length > 0 )
                        break;
                    else
                    {
                        parent.delete();
                        parent = parent.getParentFile();
                    }
                }
                while ( parent != null );
            }
            else
                throw new AeseException( "File not found "+path );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
