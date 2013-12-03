/* This file is part of calliope.
 *
 *  calliope is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  calliope is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
 */
package calliope.annotation;

import calliope.DrupalLogin;
import calliope.exception.AnnotationException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.xml.sax.InputSource;
import java.util.ArrayList;

/**
 * Model an annotation service
 * @author desmond
 */
public class AnnotationService 
{
    String host;
    String user;
    String pass;
    String service;
    String cookie;
    /**
     * Create an annotation service object
     * @param host the raw host domain-name only
     * @param user the user name used to create a login
     * @param pass the user's password
     * @param service the path of the service to combine with the host
     */
    AnnotationService(String host, String user, String pass, String service )
    {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.service = service;
    }
    /**
     * Login to the host - remember to logout
     * @throws AnnotationException 
     */
    void login() throws AnnotationException
    {
        try
        {
            DrupalLogin dl = new DrupalLogin();
            cookie = dl.login( host, user, pass );
            if ( cookie == null )
                throw new Exception("service didn't allow login");
        }
        catch (Exception e)
        {
            throw new AnnotationException( e );
        }
    }
    /**
     * Logout from the host
     * @throws AnnotationException 
     */
    void logout() throws AnnotationException
    {
        try
        {
            DrupalLogin dl = new DrupalLogin();
            dl.logout( host, cookie );
        }
        catch (Exception e)
        {
            throw new AnnotationException( e );
        }
    }
    /**
     * Send an annotation object to the host
     * @param a the annotation to send
     * @return the server's response message
     * @throws Exception 
     */
    String postToService( Annotation a ) throws Exception
    {
        String query = a.toString();
        URL url = new URL(getService());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length",  
            String.valueOf(query.getBytes("UTF-8").length));
        connection.setRequestProperty("Content-Encoding", "UTF-8");
        OutputStream os = connection.getOutputStream();
        os.write(query.getBytes("UTF-8"));
        os.close();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));
        String line;
        while ( (line = br.readLine()) != null)
            sb.append(line);
        br.close();
        os.close();
        int rc = connection.getResponseCode();
        if ( rc >= 300 )
            throw new Exception( "Failed to post "+a.getId()+": "+rc );
        return sb.toString();
    }
    /**
     * Compose the service URL
     * @return a String
     */
    private String getService()
    {
        return "http://"+host+service;
    }
    /**
     * Fetch one annotation from the server
     * @param id the id (url) of the annotation
     * @return the text of the annotation
     * @throws Exception 
     */
    String getOneAnnotation( String id ) throws Exception
    {
        URL url = new URL(id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", cookie);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));
        String anns;
        while ( (anns = br.readLine()) != null)
            sb.append(anns);
        br.close();
        return sb.toString();
    }
    /**
     * Turn a textual representation of an annotation into an XML document
     * @param sbs the textual source
     * @return an XML document
     * @throws Exception 
     */
    Document parseAnnotation( String sbs ) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource src = new InputSource(new StringReader(sbs));
        return db.parse(src);
    }
    /**
     * Create an array of Annotation objects that refer to a docID
     * @param docID the document ID
     * @return an array of initialised annotations
     */
    Annotation[] getAnnotationsFor( String docID ) throws Exception
    {
        ArrayList<Annotation> vector = new ArrayList<Annotation>();
        NodeList nodes = getDocList( docID );
        for ( int i=0;i<nodes.getLength();i++ )
        {
            Node n = nodes.item( i );
            if ( n.getNodeType()==Node.ELEMENT_NODE )
            {
                Element e = (Element)n;
                Attr attr = e.getAttributeNode("rdf:about");
                if ( attr != null )
                {
                    String id = attr.getNodeValue();
                    String src = getOneAnnotation( id );
                    Document doc = parseAnnotation( src );
                    vector.add( new Annotation(doc) );
                }
            }
        }
        Annotation[] arr = new Annotation[vector.size()];
        vector.toArray( arr );
        return arr;
    }
    /**
     * Store an array of annotations in the service
     * @param anns the annotation array
     */
    public void doStore( Annotation[] anns )
    {
        try
        {
            for ( int i=0;i<anns.length;i++ )
            {
                String ans = postToService( anns[i] );
                System.out.println(ans );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
        }
    }
    /**
     * Get the XML annotations for the given docid
     * @param docID the docid to fid references to
     * @return an XML Document containing the annotations
     * @throws Exception 
     */
    private Document getXmlDoc( String docID ) throws Exception
    {
        if ( cookie == null )
            throw new Exception("login first!");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        String urn = docID.replace("/","%2F");
        URL url = new URL(getService()+"?annotates=urn%3Aaese%3A"+urn);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", cookie);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));
        String anns;
        while ( (anns = br.readLine()) != null)
            sb.append(anns);
        br.close();
        String sbs = sb.toString();
        InputSource src = new InputSource(new StringReader(sbs));
        return db.parse(src);
    }
    /**
     * Get a list of all the annotations belonging to a given docID
     * @param docID annotations must use this docID
     * @return a NodeList of matching annotations (with IDs)
     */
    NodeList getDocList( String docID ) throws Exception
    {
        Document doc = getXmlDoc( docID );
        XPath xpath = XPathFactory.newInstance().newXPath();
        UniversalNamespaceCache ctx = new UniversalNamespaceCache(doc,false);
        xpath.setNamespaceContext(ctx);
        XPathExpression expr1 = xpath.compile(
            "//rdf:Description[starts-with(@rdf:about,'http://austese.net/lorestore/oa/')]");
        return (NodeList)expr1.evaluate(doc,XPathConstants.NODESET);
    }
    /**
     * Delete all annotations we just created
     * @param docID annotations to delete for
     */
    public void deleteByDocID( String docID )
    {
        try
        {
            NodeList nodes = getDocList( docID );
            for ( int i=0;i<nodes.getLength();i++ )
            {
                Node n = nodes.item(i);
                if ( n.getNodeType()==Node.ELEMENT_NODE )
                {
                    Element e = (Element)n;
                    Attr attr = e.getAttributeNode("rdf:about");
                    if ( attr != null )
                    {
                        URL resource = new URL(attr.getNodeValue());
                        HttpURLConnection conn = (HttpURLConnection) 
                            resource.openConnection();
                        conn.setRequestMethod("DELETE");
                        conn.setRequestProperty("Cookie", cookie);
                        if ( conn.getResponseCode()!=204 )
                            System.out.println("failed to delete "+n.getTextContent());
                        else
                            System.out.println("Deleted "+n.getTextContent());
                    }
                }
            }
        }
        catch ( Exception e )
        {   // handle error here
            System.out.println("Failed to delete "+docID);
            e.printStackTrace(System.out);
        }
    }
}
