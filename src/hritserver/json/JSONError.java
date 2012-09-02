/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
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
package hritserver.json;

/**
 * A class to handle simple JSON error messages
 * @author desmond
 */
public class JSONError 
{
    private static String escape( String message )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<message.length();i++ )
        {
            if ( message.charAt(i) == '"' )
                sb.append("\\\"");
            else
                sb.append( message.charAt(i));
        }
        return sb.toString();
    }
    /**
     * Convert to JSON
     * @param message the message to JSONify
     * @return a error string in JSON format
     */
    public static String format( String message )
    {
        return "{ "+"error: \""+escape(message)+"\" }";
    }
}
