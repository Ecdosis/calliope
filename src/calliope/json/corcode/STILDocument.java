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

package calliope.json.corcode;
import calliope.json.JSONDocument;
import calliope.constants.JSONKeys;
import calliope.exception.JSONException;
import calliope.constants.Formats;
import java.io.File;
import java.util.Set;
import java.util.ArrayList;
/**
 *
 * @author desmond
 */
public class STILDocument extends JSONDocument
{
    ArrayList<JSONDocument> ranges;
    /** array of actual ranges with absolute offsets */
    ArrayList<Range> rangeArray;
    int lastOffset;
    
    public STILDocument()
    {
        super();
        put( JSONKeys.STYLE, Formats.DEFAULT );
        ranges = new ArrayList<JSONDocument>();
        put( JSONKeys.RANGES, ranges );
        put( JSONKeys.FORMAT, Formats.STIL );
        lastOffset = 0;
    }
    /**
     * Add a range to the STIL Document. Must be added in sequence
     * @param r the range to add
     * @return the added document
     */
    public JSONDocument add( Range r ) throws JSONException
    {
        JSONDocument doc = new JSONDocument();
        int reloff = r.offset - lastOffset;
        lastOffset = r.offset;
        doc.put( JSONKeys.NAME, r.name );
        doc.put( JSONKeys.RELOFF, reloff );
        doc.put( JSONKeys.LEN, r.len );
        if ( r.removed )
            doc.put( JSONKeys.REMOVED, true );
        if ( r.annotations != null && r.annotations.size() > 0 )
        {
            ArrayList<Object> attrs = new ArrayList<Object>();
            for ( int i=0;i<r.annotations.size();i++ )
            {
                Annotation a = r.annotations.get( i );
                attrs.add( a.toJSONDocument() );
            }
            doc.put( JSONKeys.ANNOTATIONS, attrs );
        }
        ranges.add( doc );
        return doc;
    }
    /**
     * Read in a CorCode document
     * @param src the source document
     * @return the document
     */
    public static STILDocument internalise( File src ) throws Exception
    {
        JSONDocument doc = JSONDocument.internalise( src, "UTF-8" );
        STILDocument stil = new STILDocument();
        stil.rangeArray = new ArrayList<Range>();
        ArrayList list = (ArrayList)doc.get( JSONKeys.RANGES );
        int currentOffset = 0;
        for ( int i=0;i<list.size();i++ )
        {
            JSONDocument subDoc = (JSONDocument) list.get( i );
            Integer len = (Integer)subDoc.get( JSONKeys.LEN );
            Integer relOff = (Integer)subDoc.get( JSONKeys.RELOFF );
            String name = (String) subDoc.get( JSONKeys.NAME );
            ArrayList annotations = (ArrayList) subDoc.get( 
                JSONKeys.ANNOTATIONS );
            currentOffset += relOff.intValue();
            Range r = new Range( name, currentOffset, len.intValue() );
            if ( annotations != null )
            {
                for ( int j=0;j<annotations.size();j++ )
                {
                    JSONDocument annotation = (JSONDocument)annotations.get(j);
                    // there is always only one key-value pair
                    Set<String> keys = annotation.keySet();
                    String[] array = new String[keys.size()];
                    keys.toArray( array );
                    r.addAnnotation( array[j], annotation.get(array[j]) );
                }
            }
            stil.rangeArray.add( r );
        }
        return stil;
    }
    /**
     * Get the range information from a loaded document
     * @param key the property name desired
     * @param offset the absolute offset
     * @param length the length of the range
     * @return a range object
     */
    public Range get( String key, int offset, int length ) throws Exception
    {
        if ( rangeArray != null )
        {
            int top,bottom,mid;
            top = 0;
            bottom = rangeArray.size()-1;
            while ( top <= bottom )
            {
                mid = (bottom+top)/2;
                Range r = rangeArray.get(mid);
                if ( r.offset+r.len<offset )
                    top = mid+1;
                else if ( r.offset >= offset+length )
                    bottom = mid-1;
                else
                {
                    // overlap
                    // 1. move start to first overlapping range
                    int start = mid;
                    while ( start > 0 && r.offset+r.len>offset )
                    {
                        start--;
                        r = rangeArray.get(start);
                    }
                    if ( start<rangeArray.size()-1&& r.offset+r.len<=offset )
                        start++;
                    // 2. move end to last overlapping range
                    int end = mid;
                    do
                    {
                        r = rangeArray.get( end );
                        if ( r.offset<offset+length )
                            end++;
                    }
                    while ( end < rangeArray.size()-1 
                        && r.offset<offset+length );
                    if ( end>0&&r.offset>=offset+length )
                        end--;
                    // look in the range between start and end
                    for ( int k=start;k<=end;k++ )
                    {
                        r = rangeArray.get( k );
                        if ( r.name.equals(key) )
                            return r;
                    }
                    break;
                }
            }
        }
        else
            throw new Exception("STILDocument not loaded");
        return null;
    }
}
