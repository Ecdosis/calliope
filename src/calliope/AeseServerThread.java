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
package calliope;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Connector;
/**
 *
 * @author desmond
 */
public class AeseServerThread extends Thread 
{
    public void run()
    {
        try
        {
            Server server = new Server(8080);
            Connector[] connectors = server.getConnectors();
            connectors[0].setHost(AeseServer.host);
            connectors[0].setPort(AeseServer.wsPort);
            server.setHandler(new AeseServer());
            System.out.println("starting...");
            server.start();
            server.join();
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
        }
    }
}
