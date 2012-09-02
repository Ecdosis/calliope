/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.json.corcode;
import hritserver.json.JSONDocument;
import hritserver.exception.JSONException;
/**
 * Represent a simple attribute
 * @author desmond
 */
public class Annotation 
{
    String name;
    Object value;
    public Annotation( String name, Object value )
    {
        this.name = name;
        this.value = value;
    }
    public JSONDocument toJSONDocument() throws JSONException
    {
        JSONDocument doc = new JSONDocument();
        doc.add( name, (String)value, false );
        return doc;
    }
    public String getName()
    {
        return name;
    }
    public Object getValue()
    {
        return value;
    }
}
