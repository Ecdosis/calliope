/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.db;

import calliope.exception.AeseException;

/**
 * Interface for factory and other methods
 * @author desmond
 */
public abstract class Connection 
{
    String user;
    String password;
    String host;
    int dbPort;
    int wsPort;
    
    public Connection( String user, String password, String host, 
        int dbPort, int wsPort )
    {
        this.user = user;
        this.password = password;
        this.host = host;
        this.dbPort = dbPort;
        this.wsPort = wsPort;
    }
    /**
     * Get the docID component of the path
     * @param path the path including the collection
     * @return the path with the collection popped off
     */
    protected String getDocID( String path )
    {
        String docID = null;
        int pos = path.indexOf("/");
        if ( pos == -1 )
            return null;
        else if ( pos == 0 )
        {
            pos = path.indexOf("/",1);
            docID = path.substring( pos+1 );
        }
        else
            docID = path.substring( pos+1 );
        return docID;
    }
    /**
     * Get the name of the collection (aka database in old money)
     * @param path the full path to the resource
     * @return the first part of the file-like path
     */
    protected String getCollName( String path )
    {
        String collName = null;
        int pos = path.indexOf("/");
        if ( pos == 0 )
        {
            pos = path.indexOf("/",1);
            collName = path.substring( 1, pos );
        }
        else if ( pos != -1 )
            collName = path.substring( 0, pos );
        return collName;
    }
    public abstract String getFromDb( String path );
    public abstract String putToDb( String path, String json ) 
        throws AeseException;
    public abstract String removeFromDb( String path ) throws AeseException;
    public abstract byte[] getImageFromDb( String path ) throws AeseException;
    public abstract void putImageToDb( String path, byte[] data ) 
        throws AeseException;
    public abstract void removeImageFromDb( String path ) throws AeseException;
}
