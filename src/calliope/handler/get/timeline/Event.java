/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.timeline;

import calliope.date.FuzzyDate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Store an event from the AustESE server.
 * @author desmond
 */
class Event implements Comparable<Event>
{
    FuzzyDate startDate;
    FuzzyDate endDate;
    JSONArray facsimilies;
    String eventType;
    String description;
    String name;
    String uri;
    String host;
    Event( JSONObject obj, String host )
    {
        startDate = new FuzzyDate((String)obj.get("startDate"),null );
        String eDate = (String)obj.get("endDate");
        if ( eDate != null )
            endDate = new FuzzyDate( eDate,null );
        else
            endDate = startDate;
        eventType = (String)obj.get("eventType");
        description = (String)obj.get("description");
        name = (String)obj.get("name");
        facsimilies = (JSONArray)obj.get("facsimilies");
        uri = (String)obj.get("uri");
        this.host = host;
    }
    /**
     * Convert to a JSON object (string)
     * @return a JSON object as a string
     */
    JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("startDate", startDate.toCommaSep() );
        obj.put("endDate", endDate.toCommaSep());
        obj.put("headline", name);
        if ( description == null || description.length()==0 )
            obj.put("text","no description");
        else
            obj.put("text",description);
        if ( uri != null && facsimilies != null && facsimilies.size()>0 )
        {
            // only room for one facsimile
            int lastSlashPos = uri.lastIndexOf("/");
            if ( lastSlashPos != -1 )
            {
                String host = uri.substring(0,lastSlashPos);
                obj.put("asset",new Asset(name,host,
                    (String)facsimilies.get(0),true));
            }
        }
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
