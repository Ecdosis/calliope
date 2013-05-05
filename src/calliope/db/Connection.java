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

/**
 * Abstract connection class for various databases/repositories
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
        path = path.replaceAll( "%2F", "/" );
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
    public abstract String[] listCollection( String collName ) 
        throws AeseException;
    public abstract String[] listDocuments( String collName, String expr )
        throws AeseException;
    public abstract String getFromDb( String path ) throws AeseException;
    public abstract String putToDb( String path, String json ) 
        throws AeseException;
    public abstract String removeFromDb( String path ) throws AeseException;
    public abstract byte[] getImageFromDb( String path ) throws AeseException;
    public abstract void putImageToDb( String path, byte[] data ) 
        throws AeseException;
    public abstract void removeImageFromDb( String path ) throws AeseException;
}
