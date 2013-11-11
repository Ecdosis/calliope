/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope;

import calliope.constants.Database;
import calliope.constants.JSONKeys;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import calliope.constants.Formats;
import calliope.db.Repository;
/**
 * One off: set format of all documents in cortex/corcode to 'MVD'
 * @author desmond
 */
public class MVDify 
{
    enum ContentType
    {
        MVD,
        TEXT,
        STIL,
        NONE;
    }
    private static boolean isBase64Char( char c )
    {
        return Character.isWhitespace(c)||Character.isDigit(c)
            ||Character.isLetter(c)||c=='/'||c=='+';
    }
    /**
     * Work out what kind of content we have here
     * @param body the body toe classify
     * @return the body's type
     */
    private static ContentType classifyBody( String body )
    {
        body = body.trim();
        if ( body.length() > 0 )
        {
            if ( body.charAt(0)=='{' && body.charAt(body.length()-1)=='}' )
                return ContentType.STIL;
            else
            {
                for ( int i=0;i<body.length()&&i<100;i++ )
                {
                    char token = body.charAt(i);
                    if ( !isBase64Char(token) )
                        return ContentType.TEXT;
                }
                return ContentType.MVD;
            }
        }
        else
            return ContentType.NONE;
    }
    private static void mvdifyCollection( String collection, String mvdFmt ) throws Exception
    {
        String regex = ".*";
        String[] docs = Connector.getConnection().listDocuments( 
            collection, regex );
        for ( int i=0;i<docs.length;i++ )
        {
            String contents = Connector.getConnection().getFromDb(
                collection,docs[i] );
            DBObject doc = (DBObject) JSON.parse(contents);
            String body = (String)doc.get(JSONKeys.BODY);
            ContentType cType = classifyBody(body);
            if ( cType==ContentType.MVD )
                doc.put( JSONKeys.FORMAT, mvdFmt );
            else if ( cType==ContentType.TEXT )
                doc.put( JSONKeys.FORMAT, Formats.TEXT );
            else if ( cType==ContentType.STIL )
                doc.put( JSONKeys.FORMAT, Formats.STIL);
            else
                System.out.println("Leaving resource "+docs[i]
                    +" with format "+(String)doc.get(JSONKeys.FORMAT));
            // otherwise leave it alone
            Connector.getConnection().putToDb( collection, 
                docs[i], doc.toString() );
        }
    }
    public static void main(String[] args)
    {
        try
        {
            Connector.init( Repository.MONGO, "admin", "jabberw0cky", "localhost", 
                27017,  8080, "/Library/WebServer/Document");
            mvdifyCollection( Database.CORTEX, "MVD/TEXT" );
            mvdifyCollection( Database.CORCODE, "MVD/STIL" );
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
    }
}
