/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.json.corcode;
import java.util.ArrayList;
/**
 *
 * @author desmond
 */
public class Range implements Comparable
{
    // absolute offset
    public int offset;
    public int len;
    public boolean hasText;
    public boolean removed;
    public String name;
    public static int UNSET = -1;
    public ArrayList<Annotation> annotations;
    static int rangeID = 1;
    public Range( String name )
    {
        this.name = name;
        this.offset = Range.UNSET;
    }
    public Range( String name, int offset, int len )
    {
        this.name = name;
        this.offset = offset;
        this.len = len;
    }
    public static int nextID()
    {
        return rangeID++;
    }
    /**
     * Add some more length to this range
     * @param len the additional length
     */
    public void extend( int len )
    {
        this.len += len;
    }
    /**
     * Get the name of this range
     * @return the name as a String
     */
    public String getName()
    {
        return name;
    }
    /**
     * Set the name of this range
     * @param the new name 
     */
    public void setName( String name )
    {
        this.name = name;
    }
    /**
     * Add an annotation to the range
     * @param name the attribute (aka annotation) name
     * @param value its value
     */
    public void addAnnotation( String name, Object value )
    {
        if ( annotations == null )
            annotations = new ArrayList<Annotation>();
        annotations.add( new Annotation(name,value) );
    }
    /**
     * Returns a negative integer, zero, or a positive integer as this 
     * object is less than, equal to, or greater than the specified object
     * @param other the other range to compare it to
     * @return 1, 0 or -1
     */
    public int compareTo( Object other )
    {
        Range r = (Range)other;
        if ( this.offset < r.offset )
            return -1;
        else if ( this.offset > r.offset )
            return 1;
        else if ( this.len>r.len )
            return -1;
        else if ( this.len<r.len )
            return 1;
        else
            return 0;
    }
    public int end()
    {
        return this.offset+this.len;
    }
}
