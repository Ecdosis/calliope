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
package calliope.handler.get;
import calliope.path.*;
import calliope.Utils;
import calliope.tests.Test;
import calliope.Connector;
import calliope.handler.AeseHandler;
import calliope.handler.AeseVersion;
import calliope.handler.AeseMVD;
import calliope.json.JSONDocument;
import calliope.exception.*;
import calliope.constants.*;
import calliope.URLEncoder;
import calliope.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.luc.nmerge.mvd.MVD;
import edu.luc.nmerge.mvd.MVDFile;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
/**
 * Handle some get request
 * @param request the original servlet request object
 * @param path the parsed path with dbname, docid versions etc
 * @author desmond
 */
public class AeseGetHandler extends AeseHandler
{
    static String HOME = "<html>\n" +
    "<body>\n" +
    "<h1>Calliope Web Service</h1>\n" +
    "<p>Calliope is running. This interface only has the "+
    "<a href=\""+
    Service.PREFIX+"/tests/\">test suite</a>.</p>\n" +
    "</body>\n" +
    "</html>";
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        String prefix = Path.first( urn );
        if ( prefix != null )
        {
            if ( prefix.length()==0||prefix.equals("/") )
            {
                try
                {
                    response.getWriter().print(HOME);
                }
                catch ( Exception e )
                {
                    throw new AeseException( e );
                }
            }
            else if ( prefix.equals(Services.HTML) )
                new AeseHTMLHandler().handle( request, response, Path.pop(urn) );
            else if ( prefix.equals(Services.TESTS) )
            {
                try
                {
                    String second = Path.second( urn );
                    if ( second == null || second.length()==0 )
                        second = "Home";
                    else if ( second.length()>0 )
                        second = Character.toUpperCase(second.charAt(0))
                            +second.substring(1);
                    String className = "calliope.tests.Test"+second;
                    Class tClass = Class.forName( className );
                    Test t = (Test)tClass.newInstance();
                    t.handle( request, response, Path.pop(urn) );
                }
                catch ( Exception e )
                {
                    throw new AeseException( e );
                }
            }
            else if ( prefix.equals(Services.SEARCH) )
                new AeseSearchHandler().handle( request, response, Path.pop(urn) );
            else if ( prefix.equals(Services.JSON) )
                new AeseJSONHandler().handle( request, response, Path.pop(urn) );
            else if ( prefix.equals(Services.LIST) )
                new AeseTextListHandler().handle( request, response, 
                    Path.pop(urn) );
            else if ( prefix.equals(Services.PDEF) )
                new AesePDEFHandler().handle( request, response, Path.pop(urn) );
            else if ( prefix.equals(Services.TEST) )
                new AeseTestHandler().handle( request, response, Path.pop(urn) );
            else if ( prefix.equals(Database.CORTEX) )
                new AeseGetCorTexHandler().handle( request, response, 
                    Path.pop(urn) );
            else if ( prefix.equals(Database.CORCODE) )
                new AeseGetCorCodeHandler().handle( request, response, 
                    Path.pop(urn) );
            else if ( prefix.equals(Database.CORFORM) )
                new AeseGetCorFormHandler().handle( request, response, 
                    Path.pop(urn) );
            else if ( prefix.equals(Database.CORPIX) )
                new AeseGetCorPixHandler().handle( request, response, 
                    Path.pop(urn) );
            else 
                throw new AeseException("invalid urn: "+urn );
        }
        else
            throw new PathException("Invalid urn (prefix was null) "+urn );
    }
    /**
     * Get the document body of the given urn or null
     * @param db the database where it is
     * @param docID the docID of the resource
     * @return the document body or null if not present
     */
    protected String getDocumentBody( String db, String docID ) 
        throws AeseException
    {
        try
        {
            String jStr = Connector.getConnection().getFromDb(db,docID);
            if ( jStr != null )
            {
                JSONDocument jDoc = JSONDocument.internalise( jStr );
                if ( jDoc != null )
                {
                    Object body = jDoc.get( JSONKeys.BODY );
                    if ( body != null )
                        return body.toString();
                }
            }
            throw new AeseException("document "+db+"/"+docID+" not found");
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Fetch a single style text
     * @param style the path to the style in the corform database
     * @return the text of the style
     */
    protected String fetchStyle( String style ) throws AeseException
    {
        // 1. try to get each literal style name
        String actual = getDocumentBody(Database.CORFORM,style);
        while ( actual == null )
        {
            // 2. add "default" to the end
            actual = getDocumentBody( Database.CORFORM,
                URLEncoder.append(style,Formats.DEFAULT) );
            if ( actual == null )
            {
                // 3. pop off last path component and try again
                if ( style.length()>0 )
                    style = Path.chomp(style);
                else
                    throw new AeseException("no suitable format");
            }
        }
        return actual;
    }
    /**
     * Get the actual styles from the database. Make sure we fetch something
     * @param styles an array of style ids
     * @return an array of database contents for those ids
     * @throws a AeseException only if the database is not set up
     */
    protected String[] fetchStyles( String[] styles ) throws AeseException
    {
        String[] actual = new String[styles.length];
        for ( int i=0;i<styles.length;i++ )
        {
            actual[i] = fetchStyle( styles[i] );
        }
        return actual;
    }
    /**
     * Try to retrieve the CorTex/CorCode version specified by the path
     * @param db the database to fetch from
     * @param path the already parsed URN with a valid db name
     * @param vId the groups/version to get
     * @return the CorTex/CorCode version contents or null if not found
     * @throws AeseException if the resource couldn't be found for some reason
     */
    protected AeseVersion doGetResourceVersion( String db, String docID, 
        String vPath ) throws AeseException
    {
        AeseVersion version = new AeseVersion();
        JSONDocument doc = null;
        byte[] data = null;
        String res = null;
        //System.out.println("fetching version "+vPath );
        try
        {
            res = Connector.getConnection().getFromDb(db,docID);
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
        if ( res != null )
            doc = JSONDocument.internalise( res );
        if ( doc != null && doc.containsKey(JSONKeys.BODY) )
        {
            String format = (String)doc.get(JSONKeys.FORMAT);
            if ( format == null )
                throw new AeseException("doc missing format");
            version.setFormat( format );
            version.setStyle((String)doc.get(JSONKeys.STYLE));
            if ( version.getFormat().equals(Formats.MVD) )
            {
                MVD mvd = MVDFile.internalise( (String)doc.get(
                    JSONKeys.BODY) );
                if ( vPath == null )
                    vPath = (String)doc.get( JSONKeys.VERSION1 );
                String sName = Utils.getShortName(vPath);
                String gName = Utils.getGroupName(vPath);
                int vId = mvd.getVersionByNameAndGroup(sName, gName );
                version.setMVD(mvd);
                if ( vId != 0 )
                {
                    data = mvd.getVersion( vId );
                    if ( data != null )
                        version.setVersion( data );
                    else
                        throw new AeseException("Version "+vPath+" not found");
                }
                else
                    throw new AeseException("Version "+vPath+" not found");
            }
            else
            {
                String body = (String)doc.get( JSONKeys.BODY );
                if ( body == null )
                    throw new AeseException("empty body");
                try
                {
                    data = body.getBytes("UTF-8");
                }
                catch ( Exception e )
                {
                    throw new AeseException( e );
                }
                version.setVersion( data );
            }
        }
        return version;
    }
    /**
     * Get an enumerated set of parameters prefix&lt;N&gt;
     * @param prefix each parameter in the set starts with this
     * @param map the request's param map
     * @return an array of parameter values indexed by param's number
     * @param addDefault add a "default" value if none found
     * @throws a AeseParamException if a parameter was wrongly specified
     */
    protected String[] getEnumeratedParams( String prefix, Map map, 
        boolean addDefault ) throws AeseException
    {
        ArrayList<String> array = new ArrayList<String>();
        Set keys = map.keySet();
        Iterator iter = keys.iterator();
        // get relevant param keys
        while ( iter.hasNext() )
        {
            String key = (String) iter.next();
            if ( key.startsWith(prefix) )
                array.add( (String)map.get(key) );
        }
        String[] params;
        if ( array.isEmpty() )
        {
            if ( addDefault )
                array.add( Formats.DEFAULT ); 
            params = new String[array.size()];
            array.toArray( params );
        }
        else
        {
            params = new String[array.size()];
            // get their values in a properly indexed array
            for ( int i=0;i<array.size();i++ )
            {
                String number = array.get(i).substring(prefix.length());
                int index = 0;
                if ( number.length() == 0 )
                {
                    if ( array.size() != 1 )
                        throw new ParamException(
                            "can't mix unindexed and indexed params of type "
                            +prefix);
                    else
                        params[0] = (String)map.get( array.get(i) );
                }
                else
                {
                    index = Integer.parseInt(number) - 1;
                    if ( index < 0 || index >= array.size() )
                        throw new ParamException("parameter index "
                            +index+" out of range" );
                    else
                        params[index] = (String)map.get( array.get(i) );
                }
            }
            // check for missing params
            for ( int i=0;i<params.length;i++ )
                if ( params[i] == null )
                    throw new ParamException("missing param at index "+(i+1));
        }
        return params;
    }
    protected String getVersionTableForUrn( String urn ) throws AeseException
    {
        try
        {
            JSONDocument doc = loadJSONDocument( Database.CORTEX, urn );
            String fmt = (String)doc.get(JSONKeys.FORMAT);
            if ( fmt != null && fmt.startsWith(Formats.MVD) )
            {
                AeseMVD mvd = loadMVD( Database.CORTEX, urn );
                return mvd.mvd.getVersionTable();
            }
            else if ( fmt !=null && fmt.equals(Formats.TEXT) )
            {
                // concoct a version list of length 1
                StringBuilder sb = new StringBuilder();
                String version1 = (String)doc.get(JSONKeys.VERSION1);
                if ( version1 == null )
                    throw new AeseException("Lacks version1 default");
                sb.append("Single version\n");
                String[] parts = version1.split("/");
                for ( int i=0;i<parts.length;i++ )
                {
                    sb.append(parts[i]);
                    sb.append("\t");
                }
                sb.append(parts[parts.length-1]+" version");
                sb.append("\n");
                return sb.toString();
            }
            else
                throw new AeseException("Unknown of null Format");
        }
        catch ( Exception e )
        {
            throw new AeseException(e);
        }   
    }
    /**
     * Use this method to retrieve the doc just to see its format
     * @param db the database to fetch from
     * @param docID the doc's ID
     * @return a JSON doc as returned by Mongo
     * @throws AeseException 
     */
    JSONDocument loadJSONDocument( String db, String docID ) 
        throws AeseException
    {
        try
        {
            String data = Connector.getConnection().getFromDb(db,docID);
            if ( data.length() > 0 )
            {
                JSONDocument doc = JSONDocument.internalise(data);
                if ( doc != null )
                    return doc;
            }
            throw new AeseException( "Doc not found "+docID );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }     
    /**
     * Convert a loaded Mongo doc that IS an MVD to an MVD
     * @param doc the MOngo doc
     * @return an MVD
     */
    AeseMVD jsonDocToMVD( JSONDocument doc ) throws AeseException
    {
        String fmt = (String)doc.get(JSONKeys.FORMAT);
        if ( fmt != null && fmt.equals(Formats.MVD) )
            return new AeseMVD( doc );
        else
            throw new AeseException( "JSON doc not an MVD" );
    }
    /**
     * Fetch and load an MVD
     * @param db the database 
     * @param docID
     * @return the loaded MVD
     * @throws an AeseException if not found
     */
    protected AeseMVD loadMVD( String db, String docID ) throws AeseException
    {
        try
        {
            String data = Connector.getConnection().getFromDb(db,docID);
            if ( data.length() > 0 )
            {
                JSONDocument doc = JSONDocument.internalise(data);
                if ( doc != null )
                    return new AeseMVD( doc );
            }
            throw new AeseException( "MVD not found "+docID );
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
}
