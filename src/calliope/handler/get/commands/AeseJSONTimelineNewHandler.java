/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.commands;
import calliope.Connector;
import calliope.constants.Database;
import calliope.exception.AeseException;
import calliope.constants.Params;
import calliope.handler.get.AeseGetHandler;
import calliope.db.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import calliope.handler.get.timeline.TimelineJSNew;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.handler.get.timeline.EventType;
/**
 * Compose Timeline.js JSON document as fast as possible
 * @author desmond
 */
public class AeseJSONTimelineNewHandler extends AeseGetHandler
{
    String docid;
    String title;
    String subtitle;
    EventType eventType;
    String lang;
    static HashMap<String,String> map;
    static
    {
        map = new HashMap<String,String>();
        map.put("english","en");
        map.put("italian","it");
        map.put("spanish","es");
    }
    /**
     * Extract the language code from the docid
     * @return a 2-char language code
     */
    String languageFromDocId( )
    {
        String[] parts = docid.split("/");
        if ( map.containsKey(parts[0]) )
            return map.get(parts[0]);
        else
            return "en";
    }
    private EventType readEventType( HttpServletRequest request )
    {
        EventType result = EventType.all;
        String et = request.getParameter(Params.EVENT_TYPE);
        System.out.println("event_type="+et);
        if ( et != null && et.length()>0 )
        {
            try
            {
                result = EventType.valueOf(et.toLowerCase());
            }
            catch ( Exception e )
            {
            }
        }
        return result;
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            title= request.getParameter(Params.TITLE);
            if ( title==null||title.length()==0)
                title ="Project Timeline";
            subtitle= request.getParameter(Params.SUBTITLE);
            if ( subtitle==null||subtitle.length()==0)
                subtitle ="Biographical events";
            docid = request.getParameter(Params.DOCID);
            if ( docid==null||docid.length()==0)
                docid ="english/harpur";
            eventType = readEventType(request);
            System.out.println("event type="+eventType.toString());
            lang = languageFromDocId();
            Connection conn = Connector.getConnection();
            String[] docs = conn.listDocuments( Database.EVENTS, docid+"/.*" );
            ArrayList<String> events = new ArrayList<String>();
            for ( int i=0;i<docs.length;i++ )
            {
                String res = conn.getFromDb( Database.EVENTS, docs[i] );
                if ( res != null && res.length()>0 )
                {
                    events.add( res );
                }
            }
            String[]array = new String[events.size()];
            events.toArray(array);
            TimelineJSNew jobj = new TimelineJSNew(title,subtitle,eventType,
                array,lang);
            response.setCharacterEncoding("UTF-8");
            String jstring = jobj.toJSONString();
            //System.out.println(jstring);
            response.getWriter().print( jstring );
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }
    }
}
