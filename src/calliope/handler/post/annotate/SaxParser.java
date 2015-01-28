/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.annotate;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.PrintStream;

/**
 *
 * @author desmond
 */
public class SaxParser extends DefaultHandler
{
    SAXParser parser;
    XMLReader xmlReader;
    private ArrayList<Annotation> notes;
    private Annotation note;
    String vid;
    String docid;
    StringBuilder body;
    /** current position in file */
    private int pos;
    SaxParser( String vid ) throws Exception
    {
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            //spf.;
            spf.setNamespaceAware(true);
            this.vid = vid;
            this.notes = new ArrayList<Annotation>();
            this.body = new StringBuilder();
            parser = spf.newSAXParser();
            xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(new MyErrorHandler(System.err));
        }
        catch ( Exception e )
        {
            throw new Exception(e);
        }
    }
    public ArrayList<Annotation> getNotes()
    {
        return notes;
    }
    /**
     * Digest a single XML file
     * @param data the data read from the XML file
     */
    void digest( byte[] data ) throws Exception
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        InputSource input = new InputSource(bis);
        xmlReader.parse(input);     
    }
    public void startElement( String namespaceURI, String localName,
        String qName, Attributes atts ) throws SAXException 
    {
        if ( localName.equals("note") )
        {
            // String vid, String docid, int offset, String resp, 
            // boolean link, boolean propagateDown
            note = new Annotation( this.vid, pos, 
                atts.getValue("resp"), false, true, 0 );
        }
        else
        {
            body.append("<");
            body.append(localName);
            for ( int i=0;i<atts.getLength();i++ )
            {
                body.append(" ");
                body.append( atts.getLocalName(i) );
                body.append("=\"");
                body.append( atts.getValue(i) );
                body.append("\"");
            }
            body.append(">");
        }
    }
    /**
     * Ensure that the string doesn't contain unescaped " chars
     * @param src the source string
     * @return the same string with all '"' chars escaped
     */
    private String escape( String src )
    {
        char[] ch = src.toCharArray();
        StringBuilder sb = new StringBuilder();
        int state = 0;
        for ( int i=0;i<ch.length;i++ )
        {
            switch ( state )
            {
                case 0: // looking for " or '\\'
                    if ( ch[i] == '"' )
                        sb.append("\\\"");
                    else if ( ch[i]=='\\' )
                        state = 1;
                    else 
                        sb.append(ch[i]);
                    break;
                case 1: // seen '\\'
                    if ( ch[i] == '"' )
                        sb.append("\\\"");
                    else
                    {
                        sb.append("\\");
                        sb.append(ch[i]);
                    }
                    state = 0;
                    break;
            }
        }
        // if last character was a backslash...
        if ( state == 1 )
            sb.append("\\");
        return sb.toString();
    }
    public void characters(char[] ch, int start, int length)
    {
        if ( note != null )
            note.addToBody( escape(new String(ch,start,length)) );
        else if ( length == 1 && ch[start]=='&' )
        {   
            body.append("&amp;");
            pos += 5;
        }
        else 
        {
            body.append( ch, start, length );
            pos += length;
        }
    }
    /**
     * End tag handler
     * @param uri
     * @param localName
     * @param qName 
     */
    public void endElement(String uri, String localName, String qName)
    {
        if ( localName.equals("note") && note != null )
        {
            notes.add( note );
            note = null;
        }
        else
        {
            body.append("</");
            body.append(localName);
            body.append(">");
        }
    }
    public String getBody()
    {
        return body.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for ( int i=0;i<notes.size();i++ )
        {
            sb.append(notes.get(i).toString());
            if ( i<notes.size()-1 )
                sb.append(",\n");
        }
        sb.append(" ]");
        return sb.toString();
    }
    private static class MyErrorHandler implements ErrorHandler 
    {
        private PrintStream out;

        MyErrorHandler(PrintStream out) 
        {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();

            if (systemId == null) {
                systemId = "null";
            }

            String info = "URI=" + systemId + " Line=" 
                + spe.getLineNumber() + ": " + spe.getMessage();

            return info;
        }
        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
