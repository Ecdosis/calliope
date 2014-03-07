/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.handler.get.timeline;

import org.json.simple.JSONObject;

/**
 * This is required by TimelineJS
 * @author desmond
 */
class Asset
{
    String id;
    String caption;
    String host;
    boolean scale;
    Asset(String caption, String host, String id, boolean scale )
    {
        this.id = id;
        this.caption = caption;
        this.host= host;
        this.scale = scale;
    }
    JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(host);
        if ( !host.endsWith("/") )
            sb.append("/");
        sb.append(id);
        if ( scale )
            sb.append("?scale=true");
        obj.put("media", sb.toString());
        obj.put("caption",caption);
        obj.put("credit","");
        return obj;
    }
}
