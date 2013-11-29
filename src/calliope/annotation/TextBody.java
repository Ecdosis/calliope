package calliope.annotation;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import calliope.annotation.Body;
import java.util.UUID;

/**
 * A textual body such as plain text
 * @author desmond
 */
public class TextBody extends Body
{
    String text;
    TextBody( String body )
    {
        this.text = body;
    }
    public String getId()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": ");
        sb.append("\"urn:uuid:");
        sb.append(UUID.randomUUID().toString());
        sb.append("\"");
        return sb.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t\"@type\": [\"cnt:ContentAsText\", \"dctypes:Text\"],\n"); 
        sb.append("\t\"chars\": \"");
        sb.append(text);
        sb.append("\",\n");
        sb.append("\t\"cnt:characterEncoding\": \"UTF-8\"\n}");
        return sb.toString();
    }
}
