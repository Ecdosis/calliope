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
package hritserver.handler.get.commands;
import hritserver.exception.HritException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hritserver.handler.get.HritGetHandler;

/**
 * Handle a request for a Newick tree
 * @author desmond
 */
public class HritTreeHandler extends HritGetHandler
{
    /**
     * Handle a request for a Newick tree
     * @param request the original request
     * @param urn the original request urn
     * @throws a HritException if something went wrong
     * @return the Newick tree
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn )
        throws HritException
    {
        throw new HritException( "unimplemented" );
    }
}
