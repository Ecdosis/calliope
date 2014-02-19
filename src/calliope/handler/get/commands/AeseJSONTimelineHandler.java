/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.commands;
import calliope.constants.JSONKeys;
import calliope.exception.AeseException;
import calliope.constants.Formats;
import calliope.constants.Params;
import calliope.handler.get.AeseGetHandler;
import calliope.handler.get.AeseLink;
import calliope.handler.get.timeline.TimelineJS;
import calliope.login.LoginType;
import calliope.json.JSONDocument;
import java.util.ArrayList;
import java.net.URL;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Compose Timeline.js JSON document as fast as possible
 * @author desmond
 */
public class AeseJSONTimelineHandler extends AeseGetHandler
{
    ArrayList<String> eventTypes;
    String projectID;
    String serviceURL;
    String userName;
    String password;
    String text;
    String MAX_PAGE_SIZE = "1000";
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        try
        {
            projectID = request.getParameter( Params.PROJECT );
            if ( projectID==null )
                projectID = "21";
            serviceURL = request.getParameter(Params.SERVICE);
            text = request.getParameter("text");
            if ( text == null )
                text = "Harpur timeline";
            if ( serviceURL == null )
                serviceURL="http://austese.net/sites/all/modules/austese_repository/api/events/";
            URL url = new URL(serviceURL);
            String host = url.getHost();
            String[] eTypes = request.getParameterValues( Params.EVENT_TYPE );
            if ( eTypes==null)
            {
                eTypes = new String[1];
                eTypes[0] = "biography";
            }
            userName = request.getParameter(JSONKeys.USER);
            if ( userName == null )
                userName = "harpur-dev";
            password = request.getParameter(JSONKeys.PASSWORD);
            if ( password == null )
                password = "austese9875!";
            String[] names = new String[4];
            String[] values = new String[4];
            names[0] = "project";
            names[1] = "searchField";
            names[2] = "query";
            names[3] = "pageSize";
            values[0] = projectID;
            values[1] = "eventType";
            values[3] = MAX_PAGE_SIZE;
            JSONParser parser=new JSONParser();
            TimelineJS jobj=new TimelineJS("Project Timeline",host,text);
            for ( int i=0;i<eTypes.length;i++ )
            {
                values[2] = eTypes[i];
                JSONDocument link = AeseLink.makeLink(serviceURL, userName, 
                    password, LoginType.DRUPAL, names, values);
                byte[] data = AeseLink.readLink( link, Formats.JSON );
                if ( data!= null )
                {
                    try
                    {
                        String jdoc = new String(data,"UTF-8");
                        Object obj = parser.parse( jdoc );
                        jobj.merge( (JSONObject)obj );
                    }
                    catch ( Exception e )
                    {
                        // ignore - shouldn't happen
                    }
                }
            }
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print( jobj.toJSONString() );
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }
    }
}
