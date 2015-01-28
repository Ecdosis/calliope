/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.sanitise;
import java.io.File;
import java.io.FileInputStream;
import org.json.simple.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

/**
 * Re a strict and convert a set of standoff properties to a conforming set
 * @author desmond
 */
public class Sanitiser {
    HashMap<String,Target[]> map;
    private String[] toStringArray( JSONArray arr )
    {
        if ( arr != null )
        {
            String[] strs = new String[arr.size()];
            arr.toArray(strs);
            return strs; 
        }
        else
            return null;
    }
    /**
     * Create a sanitiser
     * @param json the options that control which tags get accepted.
     * Structure is: [ { "from": { "element": "tag", "keys": ["a","b"], 
     * "values": ["x","y"] }, "to": [ "g", "h", "i" ] }
     * ]
     */
    public Sanitiser( String json )
    {
        try
        {
            map = new HashMap<String,Target[]>();
            JSONObject jObj = (JSONObject)JSONValue.parse(json);
            JSONArray rules = (JSONArray) jObj.get("rules");
            if ( rules instanceof JSONArray )
            {
                for ( int i=0;i<rules.size();i++ )
                {
                    JSONObject obj = (JSONObject)rules.get(i);
                    JSONObject from = (JSONObject)obj.get("from");
                    if ( from != null )
                    {
                        JSONArray to = (JSONArray)obj.get("to");
                        String element = (String) from.get("element");
                        String[] keys = toStringArray((JSONArray)from.get("keys"));
                        String[] values = toStringArray((JSONArray)from.get("values"));
                        String[] outputs = toStringArray(to);
                        Target t = new Target(keys,values,outputs);
                        Target[] tArr = map.get(element);
                        if ( tArr == null )
                        {
                            Target[] nArr = new Target[1];
                            nArr[0] = t;
                            map.put( element, nArr);
                        }
                        else
                        {
                            Target[] nArr = new Target[tArr.length+1];
                            nArr[tArr.length] = t;
                            for ( int j=0;j<tArr.length;j++ )
                                nArr[j] = tArr[j];
                            map.put( element, nArr );
                        }
                    }
                }
            }
            else
                throw new Exception("JSON object not an array");
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
    }
    static String readFile( String file ) throws Exception
    {
        File f = new File(file);
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int)f.length()];
        fis.read(data);
        fis.close();
        return new String(data);
    }
    /**
     * Extract the keys from an array of key-value objects in JSON
     * @param list the array
     * @return an array of keys
     */
    private String[] getKeys( JSONArray list ) throws Exception
    {
        String[] arr = new String[list.size()];
        for ( int i=0;i<list.size();i++ )
        {
            Set<String> keys = ((JSONObject)list.get(i)).keySet();
            String[] names = new String[keys.size()];
            keys.toArray(names);
            if ( names.length==1 )
                arr[i] = names[0];
            else
                throw new Exception("invalid number of keys");
        }
        return arr;
    }
    /**
     * Extract the values from an array of key-value objects in JSON
     * @param list the array
     * @return an array of values
     */
    private String[] getValues( JSONArray list ) throws Exception
    {
        String[] arr = new String[list.size()];
        for ( int i=0;i<list.size();i++ )
        {
            Set<String> keys = ((JSONObject)list.get(i)).keySet();
            String[] names = new String[keys.size()];
            keys.toArray(names);
            if ( names.length==1 )
            {
                JSONObject obj = (JSONObject)list.get(i);
                arr[i] = (String)obj.get(names[0]);
            }
            else
                throw new Exception("invalid number of keys");
        }
        return arr;
    }
    /**
     * Convert a raw STIL document to its sanitised form using our options
     * @param stil the raw STIL input
     * @return the filtered and converted STIL output
     */
    public String translate( String stil ) throws Exception
    {
        JSONObject jDoc = (JSONObject)JSONValue.parse( stil );
        if ( jDoc == null )
            throw new Exception("Invalid STIL input");
        JSONArray ranges = (JSONArray)jDoc.get("ranges");
        // filter ranges into list
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        for ( int i=0;i<ranges.size();i++ )
        {
            JSONObject range = (JSONObject)ranges.get(i);
            String element = (String)range.get("name");
            if ( map.containsKey(element) )
            {
                Target[] targets = map.get(element);
                for ( int j=0;j<targets.length;j++ )
                {
                    JSONArray annotations = (JSONArray)range.get("annotations");
                    String[] keys  = null;
                    String[] values = null;
                    if ( annotations != null )
                    {
                        keys = getKeys(annotations);
                        values = getValues(annotations);
                    }
                    String[] outputs = targets[j].matches(keys,values);
                    if ( outputs != null )
                    {
                        for ( int k=0;k<outputs.length;k++ )
                        {
                            JSONObject newRange = new JSONObject();
                            newRange.put("name",outputs[k] );
                            newRange.put("reloff",range.get("reloff"));
                            newRange.put("len",range.get("len"));
                            if ( range.containsKey("removed") )
                                newRange.put("removed",range.get("removed"));
                            if ( range.containsKey("content") )
                                newRange.put("content",range.get("content"));
                            // we remove all annotations
                            list.add( newRange );
                        }
                    }
                }
            }
            // hard-wired cases
            else if ( element.equals("pg") )
            {
                JSONArray atts = (JSONArray)range.get("annotations");
                for ( int j=0;j<atts.size();j++ )
                {
                    JSONObject obj = (JSONObject)atts.get(j);
                    if ( obj.containsKey("facs") )
                    {
                        String page = (String)obj.get("facs");
                        int pos = page.lastIndexOf("/");
                        if ( pos != -1 )
                            page = page.substring(pos+1);
                        JSONObject newRange = new JSONObject();
                        newRange.put("name","page");
                        newRange.put("reloff",range.get("reloff"));
                        newRange.put("len",range.get("len"));
                        JSONArray annotations = new JSONArray();
                        JSONObject pageNo = new JSONObject();
                        pageNo.put("n", page);
                        annotations.add(pageNo);
                        newRange.put("annotations",annotations);
                        list.add( newRange );
                    }
                }
            }
            else if ( element.equals("graphic") )
            {
                // inline graphics are a special fixed case
                JSONArray atts = (JSONArray)range.get("annotations");
                JSONArray annotations = new JSONArray();
                JSONObject newRange = new JSONObject();
                for ( int j=0;j<atts.size();j++ )
                {
                    JSONObject obj = (JSONObject)atts.get(j);
                    if ( obj.containsKey("url") )
                    {
                        String url = (String)obj.get("url");
                        int pos = url.lastIndexOf("/");
                        if ( pos != -1 )
                            url = url.substring(pos+1);
                        newRange.put("name","graphic");
                        newRange.put("reloff",range.get("reloff"));
                        newRange.put("len",range.get("len"));
                        JSONObject graphic = new JSONObject();
                        graphic.put("url", url);
                        annotations.add(graphic);
                    }
                    if ( obj.containsKey("alt") )
                    {
                        String alt = (String)obj.get("alt");
                        JSONObject altText = new JSONObject();
                        altText.put("alt",alt);
                        annotations.add( altText);
                    }
                    if ( obj.containsKey("title") )
                    {
                        String title = (String)obj.get("title");
                        JSONObject titleText = new JSONObject();
                        titleText.put("title",title);
                        annotations.add( titleText);
                    }
                }
                newRange.put("annotations",annotations);
                list.add( newRange );
            }
        }
        JSONObject newStil = new JSONObject();
        if ( jDoc.containsKey("style" ))
            newStil.put("style",jDoc.get("style"));
        jDoc.put( "ranges", list );
        return jDoc.toJSONString();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if ( args.length==2 )
        {
            try
            {
                String opts = readFile( args[0] );
                String properties = readFile( args[1]);
                Sanitiser s = new Sanitiser(opts);
                String json = s.translate( properties );
                System.out.println(json);
            }
            catch ( Exception e )
            {
                e.printStackTrace(System.out);
            }
        }
        else
            System.out.println("usage: java Sanitsier <opts-file> "
                +"<properties-file>");
    }
   
}
