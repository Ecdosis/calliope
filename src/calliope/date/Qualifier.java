/* This file is part of calliope.
 *
 *  calliope is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  calliope is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
 */

package calliope.date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
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
    static final Map<String,HashMap<String,Qualifier>> languages;
    static
    {
        languages = new HashMap<String,HashMap<String,Qualifier>>();
        HashMap<String,Qualifier> itMap = new HashMap<String,Qualifier>();
        itMap.put("early",early);
        itMap.put("perhaps",perhaps);
        itMap.put("by",by);
        itMap.put("late",late);
        itMap.put("circa",circa);
        HashMap<String,Qualifier> esMap = new HashMap<String,Qualifier>();
        esMap.put("early",early);
        esMap.put("perhaps",perhaps);
        esMap.put("by",by);
        esMap.put("late",late);
        esMap.put("circa",circa);
        HashMap<String,Qualifier> enMap = new HashMap<String,Qualifier>();
        enMap.put("early",early);
        enMap.put("perhaps",perhaps);
        enMap.put("by",by);
        enMap.put("late",late);
        enMap.put("circa",circa);
        languages.put("it",itMap);
        languages.put("es",esMap);
        languages.put("en",enMap);
    }
    /**
     * Turn a qualifier to an enum
     * @param qualifier the string to convert
     * @param locale the locale of the string
     * @return the enum value or none if not recognised
     */
    static Qualifier parse( String qualifier, Locale locale )
    {
        Qualifier q = none;
        if ( qualifier != null && qualifier.length()>0 )
        {
            String lcq = qualifier.toLowerCase();
            try
            {
                Map<String,Qualifier> m = Qualifier.languages.get(
                    locale.getLanguage());
                q = m.get(lcq);
                if ( q ==null )
                    throw new Exception("invalid qualifier");
            }
            catch ( Exception e )
            {
                if ( lcq.equals("c.")||lcq.equals("c") )
                    q = circa;
                else if ( lcq.equals("?") )
                    q = perhaps;
                else
                    q = none;
            }
        }
        return q;
    }
}
