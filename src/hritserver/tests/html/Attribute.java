/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;

/**
 * Represent a HTML attribute
 * @author desmond
 */
public class Attribute 
{
    String key;
    String value;
    /**
     * An attribute is just a key-value pair
     * @param key the key
     * @param value the value
     */
    public Attribute( String key, String value )
    {
        this.key = key;
        this.value = value;
    }
    /**
     * Convert this attribute to a String
     * @return a String
     */
    public String toString()
    {
        if ( value !=null )
            return " "+key+"=\""+value+"\"";
        else
            return " "+key;
    }
}
