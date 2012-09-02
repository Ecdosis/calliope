/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.json.corcode;

/**
 * Callback to allow notifications of completed ranges
 * @author desmond
 */
public interface RangeComplete 
{
    public void rangeComplete( int absoluteOffset, int len );
}
