/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.handler.get.compare;

/**
 * Generate a series of unique IDs. Complicated.
 * @author desmond
 */
public class IdGenerator 
{
    private int id;
    public IdGenerator()
    {
        id = 1;
    }
    public int next()
    {
        return id++;
    }
}
