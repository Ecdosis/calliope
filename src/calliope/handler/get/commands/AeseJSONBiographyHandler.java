/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.get.commands;

import calliope.Connector;
import calliope.constants.Database;
import calliope.constants.Params;
import calliope.db.Connection;
import calliope.exception.AeseException;
import calliope.handler.get.AeseGetHandler;
import calliope.handler.get.timeline.EventType;
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
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            docid = request.getParameter(Params.DOCID);
            if ( docid==null||docid.length()==0)
                docid ="english/harpur";
            Connection conn = Connector.getConnection();
            String[] docs = conn.listDocuments( Database.EVENTS, docid+"/.*" );
            JSONObject biography = new JSONObject();
            JSONArray jArray = new JSONArray();
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
                        jobj.remove("docid");
                        jobj.remove("title");
                        jobj.remove("_id");
                        jobj.remove("type");
                        jArray.add( jobj );  
                    }
                }
            }
            biography.put("biography",jArray);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            String jstring = biography.toJSONString();
            //System.out.println(jstring);
            response.getWriter().print( jstring );
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }
    }
}
