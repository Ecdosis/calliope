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
package hritserver.exception;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
/**
 * represent a nice-looking error message in HTML
 * @author desmond
 */
public class HritErrorMessage 
{
    HritException he;
    static String styles = "h1 {color:darkgrey;font-variant: small-caps}\n"
        +"div {padding:10px; border-bottom: solid darkgrey 1px;border-left:"
        +"solid darkgrey 1px; border-right: solid darkgrey 1px}\n"
        +"div#d1{background-color:pink;border-top: solid darkgrey 1px}\n"
        +"div#d2{background-color:cccccc}";
    public HritErrorMessage( HritException e )
    {
        this.he = e;   
    }
    /**
     * Convert to string
     * @return a string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>" );
        sb.append("<style type=\"text/css\">");
        sb.append(styles);
        sb.append("</style></head>");
        sb.append("<body><h1>HritServer Error</h1>");
        sb.append("<div id=\"d1\"><h3>Message</h3>");
        sb.append("<p>Unfortunately, an error occurred during the previous ");
        sb.append( "operation. The following message describes the problem:</p>");
        sb.append( he.getMessage() );
        sb.append("<p><form><input type=\"button\" value=\"Back\" onClick=\"history.go(-1);return true;\">" );
        sb.append("<input type=\"button\" value=\"Home\" onClick=\"parent.location='/tests/'\"></form></p>" );
        sb.append( "</div>");
        sb.append( "<div id=\"d2\"><h3>Stack trace</h3>" );
        sb.append("<p>The following information may be used ");
        sb.append( "by a programmer seeking to diagnose the problem.</p>");
        Writer result = new StringWriter();
        PrintWriter pw = new PrintWriter(result);
        he.printStackTrace(pw);
        sb.append( result.toString().replace("\n","<br>") );
        sb.append( "</div>" );
        sb.append( "</body></html>" );
        return sb.toString();
    }
}
