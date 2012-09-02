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

package hritserver.handler.get.compare;
import java.util.ArrayList;
import hritserver.json.JSONDocument;
import hritserver.constants.JSONKeys;
import hritserver.exception.HritException;
import hritserver.json.corcode.ProgressiveParser;
import hritserver.json.corcode.RangeComplete;
/**
 * Represent a set of overlapping ranges as a set of non-overlapping runs
 * @author desmond
 */
public class RunSet implements RangeComplete
{
    ArrayList<Run> runs;
    /** current run index during parse */
    int index;
    /** true if we are appending runs, else we are erasing them */
    boolean appending;
    /**
     * Create a RunSet
     */
    RunSet()
    {
        runs = new ArrayList<Run>();
    }
    /**
     * Skim the overlapping ranges creating non-overlapping runs from them.
     * @param runs an array of runs
     * @param cc the core code containing the ranges
     * @param append if true append to runs else erase
     */
    void add( CorCode cc, boolean append )
    {
        int absolute = 0;
        appending = append;
        index = 0;
        for ( int i=0;i<cc.ranges.size();i++ )
        {
            JSONDocument r = cc.ranges.get( i );
            int rOff = (Integer)r.get(JSONKeys.RELOFF);
            int len = (Integer)r.get(JSONKeys.LEN);
            absolute += rOff;
            int end = absolute+len;
            rangeComplete( absolute, len );
        }
    }
    /**
     * Add runs from a corcode expressed as a STIL JSON document
     * @param corCode the corcode as a JSON string
     * @param append if true append the new runs, else erase
     * @throws HritException if there was a parsing exception
     */
    void add( String corCode, boolean append ) throws HritException
    {
        // assume that the runs (if any) are already sorted
        index = 0;
        appending = append;
        ProgressiveParser pp = new ProgressiveParser( this );
        try
        {
            pp.parseData( corCode.toCharArray() );
            // this was a debug routine
            /*Run prev = null;
            for ( int i=0;i<runs.size();i++ )
            {
                Run r = runs.get( i );
                if ( r.len == 0 )
                    throw new HritException("empty run");
                if ( prev != null )
                {
                    if ( prev.end()>r.offset )
                        throw new HritException("runs overlap");
                }
                else
                    prev = r;
            }
            System.out.println("runs do NOT overlap");*/
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
    /**
     * Split a run at the given position, if possible
     * @param at the index of the run to split
     * @param pos the absolute position in the run to split
     */
    private void splitAt( int at, int pos )
    {
        Run r = runs.get( at );
        int diff = pos-r.offset;
        Run s = new Run( r.offset, diff );
        r.len -= diff;
        r.offset = pos;
        runs.add( at, s );
    }
    /**
     * A range in the CorCode has been completed
     * @param offset the absolute offset
     * @param len the range's length
     */
    @Override
    public void rangeComplete( int offset, int len )
    {
        if ( len > 0 )
        {
            // split runs already in the array at start and end of the new range
            int i = index;
            int end = offset+len;
            while ( i<runs.size() )
            {
                Run r = runs.get( i );
                if ( end > r.offset )
                {
                    if ( offset >= r.offset && offset < r.end() )
                        // index of the first run starting with offset
                        index = i;
                    if ( offset > r.offset && offset < r.end()  )
                        splitAt( i, offset );
                    else if ( end > r.offset && end < r.end() )
                        splitAt( i, end );
                }
                else
                    break;
                i++;
            }
            if ( appending )
            {
                // add a new run if it overshoots
                if ( runs.size() > 0 )
                {
                    Run r = runs.get(runs.size()-1 );
                    if ( offset+len > r.end() )
                    {
                        int start = Math.max( offset, r.end() );
                        runs.add( new Run(start,end) );
                    }
                }
                else
                {
                    Run r = new Run( offset, len );
                    runs.add( r );
                }
            }
            else
            {
                // remove all runs within the new range
                int j = index;
                while ( j < runs.size() )
                {
                    Run r = runs.get( j );
                    if ( r.offset <= end )
                    {
                        if ( r.offset >= offset && r.end() <= end )
                            runs.remove( j );
                        else
                            j++;
                    }
                    else
                        break;
                }
            }
        }
    }
    /**
     * Convert to an array
     * @return an array of Run
     */
    Run[] toArray()
    {
        Run[] array = new Run[runs.size()];
        return runs.toArray( array );
    }
}
