/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.timeline;

import calliope.FuzzyDate;
import org.json.simple.JSONObject;

/**
 * Store an event from the AustESE server.
 * @author desmond
 */
class Event implements Comparable<Event>
{
    FuzzyDate startDate;
    FuzzyDate endDate;
    String eventType;
    String description;
    String name;
    String uri;
    String host;
    Event( JSONObject obj, String host )
    {
        startDate = new FuzzyDate((String)obj.get("startDate") );
        endDate = new FuzzyDate((String)obj.get("endDate") );
        eventType = (String)obj.get("eventType");
        description = (String)obj.get("description");
        name = (String)obj.get("name");
        uri = "";//(String)obj.get("uri");
        this.host = host;
    }
    JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("startDate", startDate.toCommaSep() );
        obj.put("endDate", endDate.toCommaSep());
        obj.put("headline", name);
        obj.put("text",description);
        //obj.put("asset",new Asset(name,uri,host).toJSONObject());
        return obj;
    }
    /**
     * Compare events for sorting by their start dates
     * @param e the other event
     * @return 1 if we are greater than e, -1 is less else 0
     */
    public int compareTo(Event e)
    {
        return this.startDate.compareTo(e.startDate);
    }
}
