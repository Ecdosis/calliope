/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */
package hritserver;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Connector;
/**
 *
 * @author desmond
 */
public class HritServerThread extends Thread 
{
    public void run()
    {
        try
        {
            Server server = new Server(8080);
            Connector[] connectors = server.getConnectors();
            connectors[0].setHost(HritServer.host);
            connectors[0].setPort(HritServer.wsPort);
            server.setHandler(new HritServer());
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
