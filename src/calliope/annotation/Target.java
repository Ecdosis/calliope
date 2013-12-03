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

package calliope.annotation;

import java.util.UUID;
import calliope.exception.AnnotationException;
import java.awt.Dimension;
/**
 * The target for an annotation
 * @author desmond
 */
public class Target 
{
    UUID uuid;
    Selector sel;
    String src;
    public Target()
    {
    }
    public Target( Selector sel, String source )
    {
        this.sel = sel;
        this.src = source;
    }
    void setSrc( String src )
    {
        this.src = src;
    }
    void setSelector( Selector sel )
    {
        this.sel = sel;
    }
    Selector getSelector()
    {
        return sel;
    }
    void updateStart( int from )
    {
        sel.updateStart( from );
    }
    void updateLen( int len )
    {
        sel.updateLen( len );
    }
    void update() throws AnnotationException
    {
        sel.update();
    }
    boolean isValid()
    {
        return sel.end()>sel.start();
    }
    public String getId()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": ");
        sb.append("\"urn:uuid:");
        if ( uuid == null )
            uuid = UUID.randomUUID();
        sb.append(uuid);
        sb.append("\"");
        return sb.toString();
    }
    public String toString()
    {
        /*"@id": "urn:uuid:182002b1-25d1-41dc-a3ad-53c93704e613",
            "@type": "http://www.w3.org/ns/oa#SpecificResource",
            "oa:hasSelector": {
                "@id": "urn:uuid:14637d42-c647-40cb-a18d-6dbe287011f4"
            },
            "oa:hasSource": {
                "@id": "urn:aese:english/desmond/test"
            }*/
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": \"urn:uuid:");
        sb.append(uuid.toString());
        sb.append("\",\n");
        sb.append("\"@type\": ");
        sb.append("\"http://www.w3.org/ns/oa#SpecificResource\",\n");
        sb.append("\"oa:hasSelector\": {\n\t");
        sb.append(sel.getId());
        sb.append("\n},\n");
        sb.append("\"oa:hasSource\": {\n");
        sb.append("\t\"@id\": ");
        sb.append("\"urn:aese:");
        sb.append(src);
        sb.append("\"\n}");
        return sb.toString();
    }
    Dimension getRange()
    {
        return new Dimension( sel.start(), sel.end());
    }
    public static void main( String[] args )
    {
        TextSelector ts = new TextSelector( 10, 20 );
        Target t = new Target( ts, "The quick brown fox jumps over the lazy dog");
        System.out.println( t );
    }
}
