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

package hritserver.importer.filters;
import java.util.ArrayList;
import hritserver.json.corcode.Range;
import hritserver.json.corcode.STILDocument;
import hritserver.json.JSONDocument;
import hritserver.exception.JSONException;
/**
 *
 * @author desmond
 */
public class MarkupSet extends ArrayList<Range>
{
    /**
     * Shellsort the array of ranges
     */
    public void sort()
    {
        int i, j, k, h;
        Range v;
        int[] cols = {1391376, 463792, 198768, 86961, 33936, 13776, 4592,
                        1968, 861, 336, 112, 48, 21, 7, 3, 1};
        for (k=0; k<16; k++)
        {
            h=cols[k];
            for (i=h; i<size(); i++)
            {
                v=get(i);
                j=i;
                while (j>=h && get(j-h).compareTo(v)==1)
                {
                    set(j,get(j-h));
                    j=j-h;
                }
                set(j,v);
            }
        }
    }
    /**
     * Add a range to the array
     * @param name the name of the range
     * @param offset its offset
     * @param len its length
     */
    public void add( String name, int offset, int len )
    {
        Range r = new Range( name, offset, len );
        add( r );
    }
    /**
     * Adjust the range so that it avoids white space at start and end
     * @param name the name of the range
     * @param offset the offset at the start of text
     * @param text the content with with space
     */
    public void addTrimmed( String name, int offset, String text )
    {
        String trimmed = text.trim();
        int pos = text.indexOf( trimmed );
        add( name, offset+pos, trimmed.length() );
    }
    /**
     * Print an array of queued end-tags (as ranges) in reverse order
     * @param tags the queued tags
     */
    void printEndTags( ArrayList<Range> tags )
    {
        // first insert sort ranges
        Range[] ranges = new Range[tags.size()];
        tags.toArray( ranges );
        for ( int i=1;i<ranges.length;i++ )
        {
            Range key = ranges[i];
            int j = i - 1;
            while ( j >= 0 && ranges[j].offset<key.offset )
            {
                ranges[j+1] = ranges[j];
                j = j-1;
            }
            ranges[j+1] = key;
        }
        for ( int i=0;i<ranges.length;i++ )
        {
            Range r = tags.get( i );
            System.out.print( "</"+ranges[i].name+">");
        }
        tags.clear();
    }
    /**
     * Update and print the end tags
     * @param endTags the end tags array list
     * @param pos the position before which the end-tag must go
     * @param eindex the starting range index for end-tags
     */
    void doEndTags(ArrayList<Range> endTags, int pos, int eindex )
    {
        // process trailing end-tags
        int temp = eindex;
        Range r = get( temp );
        while ( r.offset < pos )
        {
            if ( r.end() == pos )
            {
                endTags.add( r );
                //System.out.print("</"+r2.name+">");
            }
            if ( temp < size()-1 )
                r = get( ++temp );
            else
                break;
        }
        printEndTags( endTags );
    }
    /**
     * Convert to a STIL (json) document
     * @return the document in JSON form
     */
    JSONDocument toSTILDocument()
    {
        STILDocument doc = new STILDocument("");
        // ensure we are sorted
        try
        {
            sort();
            for ( int i=0;i<size();i++ )
                doc.add( get(i) );
        }
        catch ( JSONException je )
        {
            System.out.println(je.getMessage());
        }
        return doc;
    }
    /**
     * Print out a set as pseudo-XML for debugging
     * @param text the base text we are markup of
     */
    public void print( String text )
    {
        Range r1,r2;
        int sindex=0,eindex=0;
        ArrayList<Range> endTags = new ArrayList<Range>();
        for ( int i=0;i<text.length();i++ )
        {
            // first do the end-tags
            doEndTags( endTags, i, eindex );
            // update eindex
            r2 = get( eindex );
            while ( r2.end() < i )
            {
                if ( eindex < size()-1 )
                    r2 = get( ++eindex );
                else
                    break;
            }
            // now the start tags
            r1 = get( sindex );
            while ( r1.offset == i )
            {
                System.out.print("<"+r1.name+">");
                if ( sindex < size()-1 )
                    r1 = get( ++sindex );
                else
                    break;
            }
            System.out.print(text.charAt(i) );
        }
        int pos = text.length();
        doEndTags( endTags, pos, eindex );
    }
    /**
     * Get the offset of the first range, assume sorted
     * @return the first offset or -1 if no ranges
     */
    int getFirstOffset()
    {
        if ( size()>0 )
            return get(0).offset;
        else
            return -1;
    }
}
