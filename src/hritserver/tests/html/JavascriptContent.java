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
package hritserver.tests.html;
import java.util.ArrayList;
/**
 * Represent the content of a script element flexibly
 * @author desmond
 */
public class JavascriptContent extends Text
{
    ArrayList<String> scripts;
    public void add( String script)
    {
        if ( scripts == null )
            scripts = new ArrayList<String>();
        scripts.add( script );
    }
    /**
     * Convert the content to a series of Javascript functions
     * @return a String
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<scripts.size();i++ )
        {
            sb.append( scripts.get(i) );
            //sb.append( "\n" );
        }
        return sb.toString();
    }
}
