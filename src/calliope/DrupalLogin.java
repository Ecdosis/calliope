/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.util.Random;
/**
 * Login/logout of Drupal programmatically
 * @author desmond
 */
public class DrupalLogin 
{
    String boundary;
    public DrupalLogin()
    {
        boundary = "----------------------------"+randomHex(12);
    }
    String randomHex( int size )
    {
        String str = "0123456789abcdef";
        Random r = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<size;i++ )
        {
            int index = r.nextInt(16);
            char c = str.charAt(index);
            sb.append( c );
        }
        return sb.toString();
    }
    void encodeParam( StringBuilder sb, String key, String value, boolean last )
    {
        sb.append("Content-Disposition: form-data; name=\"");
        sb.append( key );
        sb.append( "\"\r\n\r\n" );
        sb.append( value );
        sb.append( "\r\n--");
        sb.append( boundary );
        if ( last )
            sb.append("--");
        else
            sb.append("\r\n");
    }
    String buildLoginRequest( String user, String password )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("--");
        sb.append(boundary);
        sb.append("\r\n");
        encodeParam( sb, "name", user, false );
        encodeParam( sb, "pass", password, false );
        encodeParam( sb, "form_id", "user_login", false );
        encodeParam( sb, "op", "Log in", true );
        return sb.toString();
    }
    public String login( String host, String user, String password ) 
        throws Exception
    {
        String cookie = null;
        URL login = new URL("http://"+host+"/user/login");
        URLConnection conn = login.openConnection();
        HttpURLConnection hconn = (HttpURLConnection) conn;
        hconn.setDoOutput( true );
        hconn.setRequestProperty("Accept","*/*");
        hconn.setRequestProperty("Host",host);
        hconn.setRequestProperty("Expect","100-continue");
        String body = buildLoginRequest( user, password);
        hconn.setRequestProperty( "Content-length",Integer.toString(
            body.getBytes().length) );
        hconn.setRequestProperty("Connection","keep-alive");
        hconn.setRequestProperty("Content-Type", 
            "multipart/form-data; boundary="+boundary);
        hconn.setInstanceFollowRedirects(false);
        OutputStream output = hconn.getOutputStream();
        output.write( body.getBytes() );
        output.write("\r\n\r\n".getBytes());
        output.flush();
        output.close();
        String headerName=null;
        for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) 
        {
            if (headerName.equals("Set-Cookie"))
            {
                System.out.println("logged in!");
                cookie = conn.getHeaderField(i);
                break;
            }
        }
        if ( cookie ==null)
            System.out.println("failed to login. error="
                +hconn.getResponseCode());
        return cookie;
    }
    public void logout( String host, String cookie ) throws Exception
    {
        URL logout = new URL("http://"+host+"/user/logout");
        URLConnection newConn = logout.openConnection();
        HttpURLConnection hconn2 = (HttpURLConnection) newConn;
        hconn2.setRequestProperty("Host",host);
        hconn2.setRequestProperty("Accept", 
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        hconn2.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        hconn2.setRequestProperty("Accept-Encoding", "gzip, deflate");
        hconn2.setRequestProperty("Referer", "http://"+host+"/user");
        hconn2.setRequestProperty("Cookie", "has_js=1; "+cookie);
        hconn2.setRequestProperty("Connection","keep-alive");
        hconn2.connect();
        int code = hconn2.getResponseCode();
        if ( code ==200 )
            System.out.println("Logged out!");
        else
            System.out.println("Not logged out. code="+code);
    }
    /**
     * Just a test of Drupal login
     * @param args the command line arguments
     */
     public static void main(String[] args) 
    {
        try
        {
            DrupalLogin dl = new DrupalLogin();
            String cookie = dl.login( "austese.net", "desmond","P1nkz3bra" );
            if ( cookie != null )
                dl.logout( "austese.net", cookie );
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
    }
}
