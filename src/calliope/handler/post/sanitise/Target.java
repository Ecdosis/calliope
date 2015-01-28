/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.sanitise;

/**
 * A set of matching attributes and the format to convert it to
 * @author desmond
 */
public class Target {
    /** attribute keys */
    String[] keys;
    /** attribute values (length == keys.length) */
    String[] values;
    String[] output;
    public Target( String[] keys, String[] values, String[] output )
        throws Exception
    {
        this.keys = keys;
        this.values = values;
        this.output = output;
        if ( keys != null && values !=null && keys.length != values.length )
            throw new Exception("key and values arrays must be same size!");
    }
    /**
     * Does this target match the given set of key-value pairs?
     * @param keys the keys to match
     * @param values their corresponding values
     * @return the matching properties or null if no match
     */
    String[] matches( String[] keys, String[] values )
    {
        if ( keys==null || values==null )
            return output;
        else if ( this.keys != null && this.values != null )
        {
            boolean matched = true;
            // each target key-value pair must be represented in the source
            for ( int i=0;i<this.keys.length;i++ )
            {
                int j=0;
                for ( ;j<keys.length;j++ )
                {
                    if ( keys[j].equals(this.keys[i]) 
                        && values[j].equals(this.values[i]) )
                        break;
                }
                // must be at least one match for all key-value pairs
                if ( j==this.keys.length )
                {
                    matched = false;
                    break;
                }
            }
            return (matched)?output:null;
        }
        else
            return this.output;
    }
}
