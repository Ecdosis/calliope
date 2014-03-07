/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.date;

/**
 * Qualifiers for fuzzy dates
 * @author desmond
 */
public enum Qualifier 
{
    none,
    early,
    late,
    by,
    perhaps,
    circa;
    /**
     * Turn a qualifier to an enum
     * @param qualifier the string to convert
     * @return the enum value or none if not recognised
     */
    static Qualifier parse( String qualifier )
    {
        Qualifier q = none;
        if ( qualifier != null && qualifier.length()>0 )
        {
            String lcq = qualifier.toLowerCase();
            try
            {
                q = valueOf(lcq);
            }
            catch ( Exception e )
            {
                if ( lcq.equals("c.")||lcq.equals("c") )
                    q = circa;
                else if ( lcq.equals("?") )
                    q = perhaps;
            }
        }
        return q;
    }
}
