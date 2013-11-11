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
package calliope.db;

import calliope.exception.AeseException;
import calliope.constants.Database;
import calliope.constants.JSONKeys;
import java.util.Iterator;
import java.util.ArrayList;
import calliope.Test;
import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.DBCursor;
import org.apache.commons.codec.binary.Base64;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;
import java.util.List;
import java.util.HashSet;


/**
 * Database interface for MongoDB
 * @author desmond
 */
public class MongoConnection extends Connection implements Test
{
    MongoClient client;
    static int MONGO_PORT = 27017;
    /** connection to database */
    DB  db;
    public MongoConnection( String user, String password, String host, 
        int dbPort, int wsPort )
    {
        super( user, password, host, dbPort, wsPort );
    }
    /**
     * Connect to the database
     * @throws Exception 
     */
    private void connect() throws Exception
    {
        if ( db == null )
        {
            MongoClient mongoClient = new MongoClient( host, MONGO_PORT );
            db = mongoClient.getDB("calliope");
            //boolean auth = db.authenticate( user, password.toCharArray() );
            //if ( !auth )
            //    throw new AeseException( "MongoDB authentication failed");
        }
    }
    /**
     * Get the Mongo db collection object form its name
     * @param collName the collection name
     * @return a DBCollection object
     * @throws AeseException 
     */
    private DBCollection getCollectionFromName( String collName )
        throws AeseException
    {
        DBCollection coll = db.getCollection( collName );
        if ( coll == null )
            coll = db.createCollection( collName, null );
        if ( coll != null )
            return coll;
        else
            throw new AeseException( "Unknown collection "+collName );
    }
    /**
     * Fetch a resource from the server, or try to.
     * @param collName the collection or database name
     * @param docID the path to the resource in the collection
     * @return the response as a string or null if not found
     */
    @Override
    public String getFromDb( String collName, String docID ) throws AeseException
    {
        try
        {
            connect();
            DBCollection coll = getCollectionFromName( collName );
            DBObject query = new BasicDBObject( JSONKeys.DOCID, docID );
            DBObject obj = coll.findOne( query );
            if ( obj != null )
                return obj.toString();
            else
                throw new FileNotFoundException( "failed to find "
                    +collName+"/"+docID );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * PUT a json file to the database
     * @param collName the name of the collection
     * @param docID the docid of the resource 
     * @param json the json to put there
     * @return the server response
     */
    @Override
    public String putToDb( String collName, String docID, String json ) 
        throws AeseException
    {
        try
        {
            docIDCheck( collName, docID );
            DBObject doc = (DBObject) JSON.parse(json);
            doc.put( JSONKeys.DOCID, docID );
            connect();
            DBCollection coll = getCollectionFromName( collName );
            DBObject query = new BasicDBObject( JSONKeys.DOCID, docID );
            WriteResult result = coll.update( query, doc, true, false );
            //return removeFromDb( path );
            return result.toString();
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Remove a document from the database
     * @param collName name of the collection
     * @param docID the docid of the resource 
     * @param json the json to put there
     * @return the server response
     */
    @Override
    public String removeFromDb( String collName, String docID ) 
        throws AeseException
    {
        try
        {
            connect();
            DBCollection coll = getCollectionFromName( collName );
            DBObject query = new BasicDBObject( JSONKeys.DOCID, docID );
            WriteResult result = coll.remove( query );
            return result.toString();
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Get a list of docIDs or file names corresponding to the regex expr
     * @param collName the collection to query
     * @param expr the regular expression to match against docid
     * @return an array of matching docids, which may be empty
     */
    @Override
    public String[] listDocuments( String collName, String expr )
        throws AeseException
    {
        try
        {
            connect();
            DBCollection coll = getCollectionFromName( collName );
            if ( coll != null )
            {
                BasicDBObject q = new BasicDBObject();
                q.put(JSONKeys.DOCID, Pattern.compile(expr) );
                DBCursor curs = coll.find( q );
                ArrayList<String> docids = new ArrayList<String>();
                Iterator<DBObject> iter = curs.iterator();
                int i = 0;
                while ( iter.hasNext() )
                {
                    String dId = (String)iter.next().get(JSONKeys.DOCID);
                    if ( dId.matches(expr) )
                        docids.add( dId );
                }
                String[] array = new String[docids.size()];
                docids.toArray( array );
                return array;
            }
            else
                throw new AeseException("collection "+collName+" not found");
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * List all the documents in a Mongo collection
     * @param collName the name of the collection
     * @return a String array of document keys
     * @throws AeseException 
     */
    @Override
    public String[] listCollection( String collName ) throws AeseException
    {
        if ( !collName.equals(Database.CORPIX) )
        {
            try
            {
                connect();
            }
            catch ( Exception e )
            {
                throw new AeseException( e );
            }
            DBCollection coll = getCollectionFromName( collName );
            BasicDBObject keys = new BasicDBObject();
            keys.put( JSONKeys.DOCID, 1 );
            DBCursor cursor = coll.find( new BasicDBObject(), keys );
            if ( cursor.length() > 0 )
            {
                String[] docs = new String[cursor.length()];
                Iterator<DBObject> iter = cursor.iterator();
                int i = 0;
                while ( iter.hasNext() )
                    docs[i++] = (String)iter.next().get( JSONKeys.DOCID );
                return docs;
            }
            else
                throw new AeseException( "no docs in collection "+collName );
        }
        else
        {
            GridFS gfs = new GridFS( db, collName );
            DBCursor curs = gfs.getFileList();
            int i = 0;
            List<DBObject> list = curs.toArray();
            HashSet<String> set = new HashSet<String>();
            Iterator<DBObject> iter = list.iterator();
            while ( iter.hasNext() )
            {
                String name = (String)iter.next().get("filename");
                set.add(name);
            }
            String[] docs = new String[set.size()];
            set.toArray( docs );
            return docs;
        }
    }
    /**
     * Get an image from the database
     * @param collName the collection name
     * @param docID the docid of the corpix
     * @return the image data
     */
    @Override
    public byte[] getImageFromDb( String collName, String docID )
    {
        try
        {
            GridFS gfs = new GridFS( db, collName );
            GridFSDBFile file = gfs.findOne( docID );
            if ( file != null )
            {
                InputStream ins = file.getInputStream();
                long dataLen = file.getLength();
                // this only happens if it is > 2 GB
                if ( dataLen > Integer.MAX_VALUE )
                    throw new AeseException( "file too big (size="+dataLen+")" );
                byte[] data = new byte[(int)dataLen];
                int offset = 0;
                while ( offset < dataLen )
                {
                    int len = ins.available();
                    offset += ins.read( data, offset, len );
                }
                return data;
            }
            else
                throw new FileNotFoundException(docID);
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
            return null;
        }
    }
    /**
     * Store an image in the database
     * @param collName name of the image collection
     * @param docID the docid of the resource
     * @param data the image data to store
     * @throws AeseException 
     */
    @Override
    public void putImageToDb( String collName, String docID, byte[] data ) 
        throws AeseException
    {
        docIDCheck( collName, docID );
        GridFS gfs = new GridFS( db, collName );
        GridFSInputFile	file = gfs.createFile( data );
        file.setFilename( docID );
        file.save();
    }
    /**
     * Delete an image from the database
     * @param collName the collection name e.g. "corpix"
     * @param docID the image's docid path
     * @throws AeseException 
     */
    @Override
    public void removeImageFromDb( String collName, String docID ) 
        throws AeseException
    {
        try
        {
            GridFS gfs = new GridFS( db, collName );
            GridFSDBFile file = gfs.findOne( docID );
            if ( file == null )
                throw new FileNotFoundException("file "+collName+"/"+docID
                    +" not found");
            gfs.remove( file );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Check that the database response is OK
     * @param response the json response from the MongoDB
     * @return true if its OK was 1.0 or the response has an _id field
     */
    private boolean checkResponse( String response )
    {
        DBObject doc = (DBObject) JSON.parse(response);
        Object value = doc.get( "ok" );
        if ( value instanceof Double && ((Double)value).doubleValue()==1.0 )
            return true;
        else
        {
            value = doc.get("_id");
            if ( value != null )
                return true;
            else
                return false;
        }
    }
    /**
     * Test a Mongo connection by putting, deleting, getting a JSON 
     * file and an image.
     * @return a String indicating how many tests succeeded
     */
    @Override
    public String test()
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            byte[] imageData = Base64.decodeBase64( testImage );
            connect();
            String response = putToDb( "test", "data/text", testJson );
            if ( checkResponse(response) )
            {
                response = getFromDb( "test", "data/text" );
                if ( !checkResponse(response) )
                    sb.append( "failed put/get test for plain json\n" );
                else
                {
                    response = removeFromDb( "test", "data/text" );
                    if ( !checkResponse(response) )
                        sb.append( "failed to remove plain json\n" );
                }
            }
            putImageToDb( "corpix", "data/image", imageData );
            byte[] data = getImageFromDb( "corpix", "data/image" );
            if ( data == null || data.length != imageData.length )
                sb.append( "failed put/get test for image\n" );
            else
                removeImageFromDb( "corpix", "data/image" );
            DBObject query = new BasicDBObject( JSONKeys.DOCID, "data/image" );
            GridFS gfs = new GridFS( db, "corpix" );
            GridFSDBFile file = gfs.findOne( query );
            if ( file != null )
                sb.append( "removed failed for image\n" );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
        }
        return sb.toString();
    }
}