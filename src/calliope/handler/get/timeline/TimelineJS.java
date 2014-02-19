/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.timeline;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * Maintain a TimeJS object
 * @author desmond
 */
public class TimelineJS extends JSONObject
{
    HashSet<String> map;
    ArrayList<Event> items;
    String host;
    JSONObject timeline;
    public TimelineJS( String headline, String host, String text )
    {
        timeline = new JSONObject();
        this.put( "timeline", timeline );
        this.host = host;
        timeline.put("headline", headline );
        timeline.put("type","default");
        timeline.put("text",text);
        map = new HashSet<String>();
        items = new ArrayList<Event>();
    }
    /**
     * Given a set of events, add them, obliterating duplicates as you go
     * @param obj 
     */
    public void merge( JSONObject obj )
    {
        JSONArray results = (JSONArray)obj.get("results");
        for ( int i=0;i<results.size();i++ )
        {
            JSONObject item = (JSONObject)results.get(i);
            String id = (String)item.get("id");
            if ( id != null )
            {
                if ( !map.contains(id) )
                {
                    map.add(id);
                    items.add( new Event(item,host) );
                }
            }
        }
    }
    public void sort()
    {
        Event[] arr = new Event[items.size()];
        items.toArray(arr);
        Arrays.sort( arr );
        JSONArray date = new JSONArray();
        for ( int i=0;i<arr.length;i++ )
        {
            date.add( arr[i].toJSONObject() );
        }
        if ( arr.length > 0 )
            timeline.put("startDate", arr[0].startDate.toCommaSep());
        timeline.put( "date", date );
    }
    public String toJSONString()
    {
        sort();
        return super.toJSONString();
    }
}
