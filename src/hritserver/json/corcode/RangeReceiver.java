/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.json.corcode;

/**
 *
 * @author desmond
 */
public class RangeReceiver implements RangeComplete 
{
    @Override
    public void rangeComplete( int absoluteOffset, int len )
    {
        System.out.println("completed range at offset "
            +absoluteOffset+" len="+len );
    }
}
