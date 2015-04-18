/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get.timeline;

import calliope.date.FuzzyDate;
import java.util.Locale;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Represent an event for the readable biography
 * @author desmond
 */
public class BioEvent implements Comparable<BioEvent> 
{
    FuzzyDate date;
    String description;
    String references;
    public BioEvent( JSONObject dateObj, String description,
       String references, Locale locale )
    {
        String spec = FuzzyDate.toSpec( dateObj, locale );
        this.date = new FuzzyDate( spec, locale );
        this.description = description;
        this.references = references;
    }
    /**
     * Compare events for sorting by their start dates
     * @param e the other event
     * @return 1 if we are greater than e, -1 is less else 0
     */
    public int compareTo(BioEvent e)
    {
        return this.date.compareTo(e.date);
    }
    public JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("date", JSONValue.parse(date.toJSON()) );
        obj.put("description",description);
        obj.put("references", references);
        return obj;
    }
}
