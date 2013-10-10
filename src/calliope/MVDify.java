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
    private static void mvdifyCollection( String collection, String format ) throws Exception
    {
        String regex = ".*";
        String[] docs = Connector.getConnection().listDocuments( 
            collection, regex );
        for ( int i=0;i<docs.length;i++ )
        {
            String contents = Connector.getConnection().getFromDb(
                collection,docs[i] );
            DBObject doc = (DBObject) JSON.parse(contents);
            doc.put( JSONKeys.FORMAT, format );
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
