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
import calliope.exception.PathException;
import calliope.path.Path;

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
    public final int getDbPort()
    {
        return dbPort;
    }
    public final int getWsPort()
    {
        return wsPort;
    }
    public final String getHost()
    {
        return host;
    }
    /**
     * A docID is not allowed if there is already a file in its parent "dir"
     * @throws a PathException if that is not the case
     */
    protected void docIDCheck( String coll, String docID ) throws PathException
    {
        try
        {
            String parent = Path.chomp(docID);
            if ( getFromDb(coll,parent)!=null )
                throw new PathException("ambiguous path "+docID);
        }
        catch ( Exception e )
        {
        }
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
    protected String testJson = 
        "{\n\"removals\":\n\t[\n\t\t{\n\t\t\t\"name\": \"app\",\n\t\t\t"
        +"\"versions\": \"none\"\n\t\t},\n\t\t{\n\t\t\t\"name\": \"rd"
        +"g\",\n\t\t\t\"wits\": \"wit\",\n\t\t\t\"versions\": \"freeze"
        +"\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"lem\",\n\t\t\t\"w"
        +"its\": \"wit\",\n\t\t\t\"versions\": \"freeze\"\n\t\t}\n\t\t"
        +",\n\t\t{\n\t\t\t\"name\": \"choice\",\n\t\t\t\"versions\": \""
        +"none\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"abbr\",\n\t\t"
        +"\t\"versions\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"na"
        +"me\": \"expan\",\n\t\t\t\"versions\": \"freeze\"\n\t\t}\n\t\t"
        +",\n\t\t{\n\t\t\t\"name\": \"add\",\n\t\t\t\"strip\": false,"
        +"\n\t\t\t\"versions\": \"extend\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t"
        +"\"name\": \"del\",\n\t\t\t\"strip\": false,\n\t\t\t\"versio"
        +"ns\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"rdg"
        +"Grp\",\n\t\t\t\"versions\": \"none\"\n\t\t}\n\t\t,\n\t\t{\n\t"
        +"\t\t\"name\": \"reg\",\n\t\t\t\"versions\": \"freeze\"\n\t\t"
        +"}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"corr\",\n\t\t\t\"version"
        +"s\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t\t\"name\": \"sic\""
        +",\n\t\t\t\"versions\": \"freeze\"\n\t\t}\n\t\t,\n\t\t{\n\t\t"
        +"\t\"name\": \"mod\",\n\t\t\t\"versions\": \"none\"\n\t\t}\n"
        +"\t\t,\n\t\t{\n\t\t\t\"name\": \"subst\",\n\t\t\t\"versions\""
        +": \"none\"\n\t\t}\n\t]\n}";
        protected String testImage = 
        "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"
        +"0VVmVMpoBGZzVk90CSg4GlSaRFksvZJR8SjJ7h5LP6NYRkFi2Ebad2YzrRUs"
        +"IqtpwuyjOOLMpWtLA7wZLnfNk2GBYORRWSzu7eOyrOAPH1+gWhebqBX025EL"
        +"UIQjiVRDZm2UJucdCAODuySsl0pismLDpiRgheNkUFBCpCUWhlR5+Z8R/JEF"
        +"BDdttZPNnr6fF8rDZZNxW2n8UqQUDD8mRf60JgDdPXmA4ib5bDrFYrbpmVbr"
        +"fwvHP5a/fIPvnr4+rs/Z9/+j7/1X16/+Mqbb/jN1c9+7nfbf3pz7+128/WH3"
        +"/n32j++vHp5+fLq6urq6uXLq6vLy6url1eXV1dXL6+url78hT/4+jd/54s/9"
        +"/kHf/jFz7/17b/47n/1d3/26/zNZ//W518bb//h+T1bHn7j1959+OX/8P1/6"
        +"a8U3v75Nx/ja//+l975wcPd7+Nr3/78b/7bn9n99l+ur//aUaN9d7ySuU7R1"
        +"v9+/1/76+988xdfv/g3f/0v/f7n/o0vnL28/tzDb/+jX/8nF7sH3/z1fzb7x"
        +"6N+Jo5VD3/FDEdBPYzwV7/8/x9OA/Cvz87TCMNy14YGDPH/AidGmwDjJNTWA"
        +"AAAAElFTkSuQmCC";
}
