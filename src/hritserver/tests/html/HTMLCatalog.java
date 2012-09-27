/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
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
package hritserver.tests.html;
import hritserver.json.JSONDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;
import hritserver.Utils;
import hritserver.constants.HTMLNames;
import hritserver.constants.JSONKeys;

/**
 * Generate a twist-down catalog of documents in HTML - a ul
 * @author desmond
 */
public class HTMLCatalog extends Element
{
    HashMap<String,Object> root;
    static String TEST_LIST = "<ul class=\"treeView\"><li>Catalog"
    +"<ul class=\"collapsibleList\">"
    +"<li>Actions<ul><li>Creation<ul><li>apply()</li><li class=\"lastChild\">"
    +"applyTo(node)</li></ul></li><li class=\"lastChild\">Toggling"
    +"<ul><li>Expanding/opening</li><li class=\"lastChild\">"
    +"Collapsing/closing</li></ul></li></ul></li><li class=\"lastChild\">"
    +"Uses<ul><li>Directory listings</li><li>Tree views</li>"
    +"<li class=\"lastChild\">Outline views</li></ul></li></ul></li></ul>";
    
    public HTMLCatalog()
    {
        super( HTMLNames.UL );
        addAttribute("class","treeView" );
        root = new HashMap<String,Object>();
    }
    /**
     * Load a catalog from data returned from the database
     * @param json the json text from all_docs
     * @return true if at least one document was found
     */
    public boolean load( String json )
    {
        boolean found = false;
        JSONDocument doc = JSONDocument.internalise( json );
        if ( doc != null )
        {
            ArrayList docs = (ArrayList) doc.get( JSONKeys.ROWS );
            if ( docs != null )
            {
                for ( int i=0;i<docs.size();i++ )
                {
                    JSONDocument d = (JSONDocument)docs.get(i);
                    String key = (String) d.get( JSONKeys.KEY );
                    digest( key );
                    found = true;
                }
            }
        }
        return found;
    }
    @Override
    public void build()
    {
        Element li = new Element(HTMLNames.LI);
        li.addText( "Document catalog:" );
        addChild( li );
        Element list = new Element(HTMLNames.UL);
        list.addAttribute(HTMLNames.CLASS,"collapsibleList");
        li.addChild( list );
        buildList( list, "", root );
    }
    /**
     * Convert CamelCase names and those containing underscores into a 
     * sequence of spaced words (currently unused)
     * @param input the input string
     * @return the output string
     */
    String decamelise( String input )
    {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split("_");
        for ( int i=0;i<parts.length;i++ )
        {
            for ( int j=0;j<parts[i].length();j++ )
            {
                if ( Character.isUpperCase(parts[i].charAt(j)) )
                    sb.append(' ' );
                sb.append( parts[i].charAt(j) );
            }
            if ( i < parts.length-1 )
                sb.append(' ');
        }
        return sb.toString();
    }
    /**
     * Build the nested list recursively!
     * @param list the ul we are adding li elements to
     * @param path the docId up to this point
     * @param map the map we are getting items from
     */
    @SuppressWarnings("unchecked")
    private void buildList( Element list, String path, HashMap map )
    {
        Set keyset = map.keySet();
        String[] keys = new String[keyset.size()];
        keyset.toArray( keys );
        Arrays.sort( keys );
        for ( int i=0;i<keys.length;i++ )
        {
            Element li = new Element( HTMLNames.LI );
            if ( i==keys.length-1 )
                li.addAttribute( HTMLNames.CLASS, "lastChild" );
            String docId = Utils.canonisePath(path,keys[i]);
            HashMap subMap = (HashMap)map.get(keys[i]);
            if ( subMap != null )
            {
                li.addText( keys[i] );
                Element ul = new Element( HTMLNames.UL );
                buildList( ul, docId, subMap );
                li.addChild( ul );
            }
            else
            {
                Element link = new Element(HTMLNames.A);
                docId = docId.replace("'","\\'");
                link.addAttribute(HTMLNames.HREF, "javascript:setPath('"
                    +docId+"')");
                li.addChild( link );
                link.addText( keys[i] );
            }
            list.addChild( li );
        }
    }
    /**
     * Break up a key into its components as a set of nested hashmaps
     * @param docID the raw docid
     */
    @SuppressWarnings("unchecked")
    void digest( String docID )
    {
        String[] paths = docID.split("/");
        HashMap<String,Object> current = root;
        for ( int i=0;i<paths.length;i++ )
        {
            if ( !current.containsKey(paths[i]) )
            {
                if ( i < paths.length-1 )
                    current.put( paths[i], new HashMap<String,Object>() );
                else
                    current.put( paths[i], null );
            }
            // if this is the last path-component current may be null
            if ( current.get(paths[i])==null && i < paths.length-1 )
            {
                HashMap<String,Object> next = new HashMap<String,Object>();
                current.put( paths[i], next );
            }
            // we know that all objects are of this type
            current = (HashMap<String,Object>)current.get( paths[i] );
        }
    }
}
