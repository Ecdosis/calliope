/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.timeline;

import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.*;
import java.util.Arrays;
import java.util.Locale;

/**
 * A revised timeline js converter for the new event-type data
 * @author desmond
 */
public class TimelineJSNew  extends JSONObject
{
    String[] events;
    String title;
    ArrayList<NewEvent> items;
    JSONObject timeline;
    public TimelineJSNew( String title, String subtitle, EventType et, 
        String[] events, String langCode )
    {
        this.events = events;
        this.title = title;
        timeline = new JSONObject();
        this.put( "timeline", timeline );
        this.items = new ArrayList<NewEvent>();
        timeline.put("headline", title );
        // prevent resorting of equal dates 
        timeline.put("type","default");
        timeline.put("text",subtitle);
        Locale locale = new Locale( langCode );
        System.out.println("Fetching events of type "+et.toString());
        for ( int i=0;i<events.length;i++ )
        {
            JSONObject item = (JSONObject)JSONValue.parse(events[i]);
            String eventTitle = (String)item.get("title");
            JSONObject eventDate = (JSONObject)item.get("date");
            String eventDescription = (String)item.get("description");
            int intVal = ((Number)item.get("type")).intValue();
            EventType eventType = EventType.fromInt(intVal);
            if ( et == EventType.all || et == eventType )
            {
                NewEvent ne = new NewEvent( eventTitle, eventDate, 
                    eventDescription, eventType.toInt(), locale );
                items.add( ne );
            }
        }
        System.out.println("Found "+items.size()+" events");
    }
    public String toJSONString()
    {
        sort();
        return super.toJSONString();
    }
    public void sort()
    {
        NewEvent[] arr = new NewEvent[items.size()];
        items.toArray(arr);
        Arrays.sort( arr );
        JSONArray date = new JSONArray();
        for ( int i=0;i<arr.length;i++ )
        {
            date.add( arr[i].toJSONObject() );
        }
        //if ( arr.length > 0 )
        //    timeline.put("startDate", arr[0].startDate.toCommaSep());
        timeline.put( "date", date );
    }
}
