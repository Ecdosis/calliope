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
 * Abstract database API for various databases/repositories
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
    public abstract String[] listCollection( String coll ) 
        throws AeseException;
    public abstract String[] listDocuments( String coll, String expr )
        throws AeseException;
    public abstract String getFromDb( String coll, String docID ) 
        throws AeseException;
    public abstract String putToDb( String coll, String docID, String json ) 
        throws AeseException;
    public abstract String removeFromDb( String coll, String docID ) 
        throws AeseException;
    public abstract byte[] getImageFromDb( String coll, String docID ) 
        throws AeseException;
    public abstract void putImageToDb( String coll, String docID, byte[] data ) 
        throws AeseException;
    public abstract void removeImageFromDb( String coll, String docID ) 
        throws AeseException;
}
