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
    String media;
    String caption;
    String host;
    Asset(String caption, String uri, String host )
    {
        this.media = uri;
        this.caption = caption;
        this.host= host;
    }
    JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("media", "http://"+host+media);
        obj.put("caption",caption);
        obj.put("credit","");
        return obj;
    }
}
