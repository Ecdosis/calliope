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
package hritserver.handler.put;
import hritserver.handler.HritHandler;
import hritserver.exception.HritException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle a delete request
 * @author desmond
 */
public class HritPutHandler extends HritHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        /*String token = st.nextToken();
            if ( path.getDbName().equals("cortex") )
                return doPutCorTex(request,st);
            else if ( token.equals("corcode") )
                return doPutCorCode(request,st);
            else if ( token.equals("corpix") )
                return doPutCorPix(request,st );
            else if ( token.equals("corform") )
                return doPutCorForm(request,st);
            else
                return JSONError.format("unknown request type "+token);
        }
        else
            return JSONError.format("Empty request");*/
        throw new HritException("unimplemented");
    }
}
