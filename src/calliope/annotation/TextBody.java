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
    UUID uuid;
    TextBody( String body )
    {
        this.text = body;
    }
    public String getId()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": ");
        sb.append("\"urn:uuid:");
        uuid = UUID.randomUUID();
        sb.append(uuid.toString());
        sb.append("\"");
        return sb.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"@id\": \"");
        sb.append("urn:uuid:");
        sb.append(uuid.toString());
        sb.append("\",\n"); 
        sb.append("\"@type\": [\"http://www.w3.org/2011/content#ContentAsText\", ");
        sb.append("\"http://purl.org/dc/dcmitype/Text\"],\n"); 
        sb.append("\"cnt:chars\": \"");
        sb.append(text);
        sb.append("\",\n");
        sb.append("\"cnt:characterEncoding\": \"UTF-8\"");
        return sb.toString();
    }
}
