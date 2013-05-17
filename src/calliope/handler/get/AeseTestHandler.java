/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get;

import calliope.Connector;
import calliope.constants.Database;
import calliope.constants.JSONKeys;
import calliope.db.Connection;
import calliope.db.CouchConnection;
import calliope.db.MongoConnection;
import calliope.exception.AeseException;
import calliope.constants.Params;
import calliope.json.JSONDocument;
import java.util.HashSet;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.path.Path;

/**
 * Handle individual internal tests facelessly over REST
 * @author desmond
 */
public class AeseTestHandler 
{
    /**
     * Load all docids into a hashset for quick lookup
     * @param collName the colection to process
     * @return a set of docids for that collection
     */
    private HashSet<String> loadDocIDs( String collName )
    {
        HashSet<String> docids = new HashSet<String>();
        try
        {
            String[] keys = Connector.getConnection().listCollection( 
                collName );
            for ( int i=0;i<keys.length; i++ )
                docids.add( keys[i] );
        }
        catch ( Exception e )
        {
            // ignore
        }
        return docids;
    }
    /**
     * Check the style, format and version1 fields
     * @param collName the collection to fetch the documents form
     * @param keys the keys of all the documents
     * @param corforms the keys of all the corforms
     * @param sb the log
     * @throws AeseException 
     */
    private void checkCorThing( String collName, HashSet<String> keys, 
        HashSet<String> corforms, StringBuilder sb ) throws AeseException
    {
        // examine attributes in all cortexs
        Iterator<String> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            String docid = iter.next();
            String json = Connector.getConnection().getFromDb(
                collName,docid);
            if ( json != null )
            {
                JSONDocument jdoc = JSONDocument.internalise(json);
                String style = (String)jdoc.get( JSONKeys.STYLE );
                if ( !corforms.contains(style) )
                {
                    sb.append( "Missing style " );
                    sb.append( style );
                    sb.append( " from cortex " );
                    sb.append( docid );
                }
                if ( !jdoc.containsKey(JSONKeys.VERSION1) )
                {
                    sb.append( "Missing version1 field in " );
                    sb.append( collName );
                    sb.append( docid );
                }
                if ( !jdoc.containsKey(JSONKeys.FORMAT) )
                {
                    sb.append( "Missing format field in " );
                    sb.append( collName );
                    sb.append( docid );
                }
            }
        }
    }
    /**
     * Test all the resources on the server for integrity
     * This is WAY too expensive to use on a production server
     * @return a String being a log of errors or a success statement
     */
    private String integrityTest()
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            HashSet<String> cortexs = loadDocIDs( Database.CORTEX );
            HashSet<String> corcodes = loadDocIDs( Database.CORCODE );
            HashSet<String> corforms = loadDocIDs( Database.CORFORM );
            HashSet<String> configs = loadDocIDs( Database.CONFIG );
            // examine corcodes similarly
            checkCorThing( Database.CORTEX, cortexs, corforms, sb );
            checkCorThing( Database.CORCODE, corcodes, corforms, sb );
            if ( !corforms.contains("default") )
                sb.append( "Missing default style\n" );
            if ( !configs.contains("stripper/default") )
                sb.append( "Missing default stripper config\n" );
            if ( !configs.contains("splitter/default") )
                sb.append( "Missing default splitter config\n" );
        }
        catch ( AeseException ae )
        {
            sb.append( ae.getMessage() );
        }
        if ( sb.length()==0 )
            sb.append("Data integrity test passed\n");
        return sb.toString();
    }
    /**
     * Basic heartbeat test
     * @return a String being the results of the test
     */
    private String basicTest()
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            String value = "No database";
            Connection conn = Connector.getConnection();
            if ( conn instanceof MongoConnection )
            {
                sb.append("Running MongoDB\n");
                value = ((MongoConnection)conn).test();
            }
            else if ( conn instanceof CouchConnection )
            {
                sb.append("Running CouchDB\n");
                value = ((CouchConnection)conn).test();
            }
            sb.append( "DB Port: " );
            sb.append( conn.getDbPort());
            sb.append( "\n" );
            sb.append( "WS Port: " );
            sb.append ( conn.getWsPort() );
            sb.append( "\n" );
            sb.append( "Host: " );
            sb.append( conn.getHost() );
            sb.append( "\n" );
            String[] docs = conn.listCollection( Database.CONFIG ); 
            sb.append( docs.length );
            sb.append( " documents in collection " );
            sb.append( Database.CONFIG );
            sb.append( "\n" );
            docs = conn.listCollection( Database.CORTEX ); 
            sb.append( docs.length );
            sb.append( " documents in collection " );
            sb.append( Database.CORTEX );
            sb.append( "\n" );
            docs = conn.listCollection( Database.CORCODE ); 
            sb.append( docs.length );
            sb.append( " documents in collection " );
            sb.append( Database.CORCODE );
            sb.append( "\n" );
            docs = conn.listCollection( Database.CORFORM ); 
            sb.append( docs.length );
            sb.append( " documents in collection " );
            sb.append( Database.CORFORM );
            sb.append( "\n" );
            sb.append( value );
        }
        catch ( Exception e )
        {
            sb.append( e.getMessage() );
        }
        return sb.toString();
    }
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            String first = Path.first(urn);
            String value = "No test";
            if ( first != null )
            {
                if ( first.equals("basic") )
                    value = basicTest();
                else if ( first.equals("integrity") )
                    value = integrityTest();
            }
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println( value );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
