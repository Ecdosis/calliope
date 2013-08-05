/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.tests.html;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import calliope.constants.HTMLNames;

/**
 * Generate a select element from an _all_docs query result from couchdb
 * @author desmond
 */
public class HTMLDocSelect extends Element
{
    /**
     * Convert an array of document ids to a HTML select element
     * @param docids the text of the _all_docs call
     * @param name the name of the select list
     * @param id the id of the select
     */
    public HTMLDocSelect( String[] docids, String name, String id )
    {
        super( HTMLNames.SELECT );
        if ( id != null )
            this.addAttribute(HTMLNames.ID, id );
        if ( name != null )
            this.addAttribute( HTMLNames.NAME, name );
        load( docids );
    }
    public HTMLDocSelect( Map<String,String> map, String name, String id )
    {
        super( HTMLNames.SELECT );
        if ( id != null )
            this.addAttribute(HTMLNames.ID, id );
        if ( name != null )
            this.addAttribute( HTMLNames.NAME, name );
        load( map );
    }
    private String makeOptLabel( String[] parts )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<parts.length-1;i++ )
        {
            if ( sb.length()>0 )
                sb.append("-");
            sb.append( parts[i] );
        }
        return sb.toString();
    }
    /**
     * Create a select dropdown from a set of human names and values
     * @param map of values to human-readable names
     */
    private void load( Map<String,String> map )
    {
        Set<String> codes = map.keySet();
        Iterator<String> iter = codes.iterator();
        while ( iter.hasNext() )
        {
            String key = iter.next();
            addChild( new HTMLOption(map.get(key),key) );
        }
    }
    /**
     * Load a json document being the output of _all_docs
     * @param docids an array of all the doc IDs 
     */
    private void load( String[] docids )
    {
        for ( int i=0;i<docids.length;i++ )
        {
            String key = docids[i];
            String[] parts = key.split("/");
            if ( parts.length == 1 )
            {
                addChild( new HTMLOption(key,parts[0]) );
            }
            else if ( parts.length > 1 )
            {
                HTMLOptGroup g = null;
                String optLabel = makeOptLabel( parts );
                try
                {
                    for ( int j=0;j<this.numChildren();j++ )
                    {
                        Element e = this.getChild( j );
                        if ( e instanceof HTMLOptGroup )
                        {
                            HTMLOptGroup group = (HTMLOptGroup)e;
                            String label = group.getAttribute(
                                HTMLNames.LABEL);
                            if ( label != null 
                                && label.equals(optLabel) )
                            {
                                g = group;
                                break;
                            }
                        }
                    }
                }
                catch ( Exception e )
                {
                    System.out.println(e.getMessage());
                }
                if ( g == null )
                {
                    g = new HTMLOptGroup( optLabel );
                    addChild( g );
                }
                g.addChild( new HTMLOption(key,parts[parts.length-1]) );
            }
        }
    }
}
