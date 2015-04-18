/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get.commands;

import calliope.Connector;
import calliope.Utils;
import calliope.constants.Database;
import calliope.constants.Params;
import calliope.db.Connection;
import calliope.exception.AeseException;
import calliope.handler.get.AeseGetHandler;
import calliope.handler.get.timeline.EventType;
import calliope.handler.get.timeline.BioEvent;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * JSON Biography handler
 * @author desmond
 */
public class AeseJSONBiographyHandler extends AeseGetHandler
{
    String docid;
    JSONObject biography;
    String lang;
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            docid = request.getParameter(Params.DOCID);
            if ( docid==null||docid.length()==0)
                docid ="english/harpur";
            lang = Utils.languageFromDocId(docid);
            Connection conn = Connector.getConnection();
            String[] docs = conn.listDocuments( Database.EVENTS, docid+"/.*" );
            biography = new JSONObject();
            Locale locale = new Locale( lang );
            ArrayList<BioEvent> events = new ArrayList<BioEvent>();
            for ( int i=0;i<docs.length;i++ )
            {
                String res = conn.getFromDb( Database.EVENTS, docs[i] );
                if ( res != null && res.length()>0 )
                {
                    JSONObject jobj = (JSONObject)JSONValue.parse(res);
                    int it = ((Long)jobj.get("type")).intValue();
                    EventType et = EventType.fromInt(it);
                    if ( et == EventType.biography )
                    {
                        JSONObject eventDate = (JSONObject)jobj.get("date");
                        String eventDesc = (String)jobj.get("description");
                        String eventRefs = (String)jobj.get("references");
                        BioEvent be = new BioEvent( eventDate, eventDesc, 
                            eventRefs, locale );
                        events.add( be );
                    }
                }
            }
            sort( events );
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            String jstring = biography.toJSONString();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().print( jstring );
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }
    }
    public void sort( ArrayList<BioEvent> events )
    {
        BioEvent[] arr = new BioEvent[events.size()];
        events.toArray(arr);
        Arrays.sort( arr );
        JSONArray bio = new JSONArray();
        for ( int i=0;i<arr.length;i++ )
        {
            bio.add( arr[i].toJSONObject() );
        }
        biography.put( "biography", bio );
    }
}