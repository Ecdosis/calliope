/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.timeline;

import org.json.simple.JSONObject;
import calliope.date.FuzzyDate;
import java.text.DateFormatSymbols;
import java.util.Locale;
/**
 *
 * @author desmond
 */
public class NewEvent implements Comparable<NewEvent>
{
    FuzzyDate date;
    String title;
    String description;
    int itemType;
    /**
     * Get a locale specific month name
     * @param month the 0-based month value
     * @param locale the locale
     * @return the name of the month
     */
    public String getMonth(int month, Locale locale) 
    { 
        return DateFormatSymbols.getInstance(locale).getMonths()[month]; 
    } 
    public NewEvent( String title, JSONObject dateObj, String description,
       int itemType, Locale locale )
    {
        this.description = description;
        this.title = title;
        this.itemType = itemType;
        StringBuilder spec= new StringBuilder();
        String qualifier = (String)dateObj.get("qualifier");
        int day = ((Number)dateObj.get("day")).intValue();
        if ( qualifier != null && !qualifier.equals("none") )
        {
            spec.append((String)dateObj.get("qualifier"));
            spec.append(" ");
        }
        if ( dateObj.get("day") != null && day > 0 && day < 32 )
        {
            spec.append(((Number)dateObj.get("day")).toString());
            spec.append(" ");
        }
        if ( dateObj.get("month") != null )
        {
            int m = ((Number)dateObj.get("month")).intValue();
            String mName = "";
            if ( m > -1 && m < 12 )
                mName = getMonth( m, locale );
            if ( mName.length()>0 )
            {
                spec.append(mName);
                spec.append(" ");
            }
        }
        if ( dateObj.get("year") != null )
        {
            spec.append(dateObj.get("year"));
        }
        this.date = new FuzzyDate( spec.toString(), locale );
        if ( this.date.getYear() == 0 )
            System.out.println("Year is 0!");
    }
    JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("startDate", date.toCommaSep() );
        obj.put("endDate", date.toCommaSep());
        obj.put("headline", title);
        if ( description == null || description.length()==0 )
            obj.put("text","no description");
        else
            obj.put("text",description);
        return obj;
    }
    /**
     * Compare events for sorting by their start dates
     * @param e the other event
     * @return 1 if we are greater than e, -1 is less else 0
     */
    public int compareTo(NewEvent e)
    {
        return this.date.compareTo(e.date);
    }
}
