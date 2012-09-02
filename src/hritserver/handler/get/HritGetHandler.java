/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */
package hritserver.handler.get;
import hritserver.path.*;
import hritserver.tests.Test;
import hritserver.HritServer;
import hritserver.handler.HritHandler;
import hritserver.handler.HritVersion;
import hritserver.handler.HritMVD;
import hritserver.json.JSONDocument;
import hritserver.exception.*;
import hritserver.constants.*;
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
public class HritGetHandler extends HritHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        String prefix = Path.first( urn );
        if ( prefix != null )
        {
            if ( prefix.equals(Services.HTML) )
                new HritHTMLHandler().handle( request, response, urn );
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
                    String className = "hritserver.tests.Test"+second;
                    Class tClass = Class.forName( className );
                    Test t = (Test)tClass.newInstance();
                    t.handle( request, response, urn );
                }
                catch ( Exception e )
                {
                    throw new HritException( e );
                }
            }
            else if ( prefix.equals(Database.CORTEX) )
                new HritGetCorTexHandler().handle( request, response, urn );
            else if ( prefix.equals(Database.CORCODE) )
                new HritGetCorCodeHandler().handle( request, response, urn );
            else if ( prefix.equals(Database.CORFORM) )
                new HritGetCorFormHandler().handle( request, response, urn );
            else if ( prefix.equals(Database.CORPIX) )
                new HritGetCorPixHandler().handle( request, response, urn );
            else if ( prefix.equals(Services.LIST) )
                new HritTextListHandler().handle( request, response, urn );
            else 
                throw new HritException("invalid urn: "+urn );
        }
        else
            throw new PathException("Invalid urn "+urn );
    }
    /**
     * Get the document body of the given urn or null
     * @param urn the path to the resource
     * @return the document body or null if not present
     */
    protected String getDocumentBody( String urn ) throws HritException
    {
        try
        {
            String jStr = new String(HritServer.getFromDb(urn),"UTF-8");
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
            throw new HritException("document "+urn+" not found");
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
    /**
     * Get the actual styles from the database. Make sure we fetch something
     * @param styles an array of style ids
     * @return an array of database contents for those ids
     * @throws a HritException only if the database is not set up
     */
    protected String[] fetchStyles( String[] styles ) throws HritException
    {
        String[] actual = new String[styles.length];
        String prefix = "/"+Database.CORFORM+"/";
        for ( int i=0;i<styles.length;i++ )
        {
            Path path = new Path( prefix+styles[i] );
            // 1. try to get each literal style name
            actual[i] = getDocumentBody(path.getResource(true));
            while ( actual[i] == null )
            {
                // 2. add "default" to the end
                actual[i] = getDocumentBody( 
                    path.getResource(true,Formats.DEFAULT) );
                if ( actual[i] == null )
                {
                    // 3. pop off last path component and try again
                    if ( !path.isEmpty() )
                        path.chomp();
                    else
                        throw new HritException("no suitable format");
                }
            }
        }
        return actual;
    }
    /**
     * Try to retrieve the CorTex/CorCode version specified by the path
     * @param path the already parsed URN with a valid db name
     * @param suffix append this to the resource path
     * @return the CorTex/CorCode version contents or null if not found
     * @throws HritException if the resource couldn't be found for some reason
     */
    protected HritVersion doGetMVDVersion( VersionPath path, String suffix )
        throws HritException
    {
        HritVersion version = new HritVersion();
        JSONDocument doc = null;
        byte[] data = null;
        String res = null;
        try
        {
            res = new String(HritServer.getFromDb( 
                path.getResource(true,suffix) ), "UTF-8");
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
        if ( res != null )
            doc = JSONDocument.internalise( res );
        if ( doc != null && doc.containsKey(JSONKeys.BODY) )
        {
            MVD mvd = MVDFile.internalise( (String)doc.get(
                JSONKeys.BODY) );
            int vId = mvd.getVersionByNameAndGroup(path.getShortName(),
                path.getGroups());
            version.setFormat((String)doc.get(JSONKeys.FORMAT));
            version.setStyle((String)doc.get(JSONKeys.STYLE));
            if ( vId != 0 )
            {
                data = mvd.getVersion( vId );
                if ( data != null )
                {
                    try
                    {
                        version.setVersion( data );
                    }
                    catch ( Exception e )
                    {
                        throw new HritException( e );
                    }
                }
                else
                    throw new HritException("Version "
                        +path.getGroups()+path.getShortName()
                        +" not found");
            }
            else
            {
                throw new HritException("Version "
                    +path.getGroups()+path.getShortName()+" not found");
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
     * @throws a HritParamException if a parameter was wrongly specified
     */
    protected String[] getEnumeratedParams( String prefix, Map map, 
        boolean addDefault ) 
        throws HritException
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
    /**
     * Fetch and load an MVD
     * @param path the database path to it
     * @return the loaded MVD
     * @throws a HritException if not found
     */
    protected HritMVD loadMVD( String path ) throws HritException
    {
        try
        {
            HritMVD hMvd = new HritMVD();
            String data = new String(HritServer.getFromDb(path),"UTF-8");
            if ( data.length() > 0 )
            {
                JSONDocument doc = JSONDocument.internalise(data);
                if ( doc != null )
                {
                    String body = (String)doc.get(JSONKeys.BODY);
                    if ( body != null )
                    {
                        hMvd.mvd = MVDFile.internalise( body );
                        hMvd.format = (String)doc.get(JSONKeys.FORMAT);
                        return hMvd;
                    }
                }
            }
            throw new HritException( "MVD not found "+path );
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
}
