package calliope.annotation;

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
import java.util.UUID;

/**
 * A textual body such as plain text
 * @author desmond
 */
public class TextBody extends Body
{
    String text;
    UUID uuid;
    TextBody( String body )
    {
        this.text = body;
    }
    public void setUuid( String uuid )
    {
        if ( uuid.startsWith("urn:uuid:") )
        {
            this.uuid = UUID.fromString(uuid.substring(9));
        }
    }
    public String getId()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": ");
        sb.append("\"urn:uuid:");
        if ( uuid == null )
            uuid = UUID.randomUUID();
        sb.append(uuid.toString());
        sb.append("\"");
        return sb.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": \"");
        sb.append("urn:uuid:");
        sb.append(uuid.toString());
        sb.append("\",\n"); 
        sb.append("\"@type\": [\"http://www.w3.org/2011/content#ContentAsText\", ");
        sb.append("\"http://purl.org/dc/dcmitype/Text\"],\n"); 
        sb.append("\"cnt:chars\": \"");
        sb.append(text);
        sb.append("\",\n");
        sb.append("\"cnt:characterEncoding\": \"UTF-8\"");
        return sb.toString();
    }
}
