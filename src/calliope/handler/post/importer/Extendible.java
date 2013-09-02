/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;
import java.util.ArrayList;

/**
 * Represent the names of witnesses (versions) to which we may NOT belong
 * @author desmond
 */
public class Extendible 
{
    /** attempts to include these wits will be refused */
    ArrayList<String> excludedWits;
    /** if specified included wits are the ONLY extensions accepted */
    ArrayList<String> includedWits;
    /**
     * Exclude a witness from appearing in this extendible's wit list
     * @param wit the excluded witness name
     */
    void exclude( String wit )
    {
        if ( wit.length()> 0 )
        {
            if ( excludedWits == null )
                excludedWits = new ArrayList<String>();
            String[] wits = wit.split(" ");
            for ( int i=0;i<wits.length;i++ )
            {
                if ( !excludedWits.contains(wits[i]) )
                    excludedWits.add( wits[i] );
            }
        }
    }
    /**
     * Include an exclusive witness that will reject all others
     * @param wit the included witness name
     */
    void include( String wit )
    {
        if ( wit.length()> 0 )
        {
            if ( includedWits == null )
                includedWits = new ArrayList<String>();
            String[] wits = wit.split(" ");
            for ( int i=0;i<wits.length;i++ )
            {
                if ( !includedWits.contains(wits[i]) )
                    includedWits.add( wits[i] );
            }
        }
    }
    /**
     * Modify the witness list to exclude excluded versions
     * @param wit the witness to test
     * @return the modified witlist
     */
    String allows( String wit )
    {
        if ( includedWits != null )
        {
            StringBuilder sb = new StringBuilder();
            String[] wits = wit.split(" ");
            for ( int i=0;i<wits.length;i++ )
            {
                if ( includedWits.contains(wits[i]) )
                {
                    if ( sb.length()>0 )
                        sb.append(" ");
                    sb.append( wits[i] );
                }
            }
            return sb.toString();
        }
        else if ( excludedWits == null )
            return wit;
        else // include overrides exclude
        {
            StringBuilder sb = new StringBuilder();
            String[] wits = wit.split(" ");
            for ( int i=0;i<wits.length;i++ )
            {
                if ( !excludedWits.contains(wits[i]) )
                {
                    if ( sb.length()>0 )
                        sb.append(" ");
                    sb.append( wits[i] );
                }
            }
            return sb.toString();
        }
    }
}
