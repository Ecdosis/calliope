/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;

/**
 * An Integer object that can change its value
 * @author desmond
 */
public class MutableInt 
{
    int value;
    MutableInt( int value )
    {
        this.value = value;
    }
    int intValue()
    {
        return value;
    }
    void set( int value )
    {
        this.value = value;
    }
    void inc()
    {
        this.value++;
    }
}
