/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.get.compare;

/**
 *
 * @author desmond
 */
public class Run 
{
    /** absolute offset */
    int offset;
    int len;
    Run( int offset, int len )
    {
        this.offset = offset;
        this.len = len;
    }
    int end()
    {
        return this.offset + this.len;
    }
}
