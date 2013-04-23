/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.tests.html;
import calliope.Utils;
import calliope.constants.HTMLNames;

/**
 * Represent a HTML OptGroup
 * @author desmond
 */
public class HTMLOptGroup extends Element
{
    /**
     * Create an empty optgroup
     * @param path full path to this option
     */
    public HTMLOptGroup( String path )
    {
        super( HTMLNames.OPTGROUP );
        addAttribute( HTMLNames.LABEL, path );
    }
    /**
     * Get a group 
     * @param name the name to look for
     * @return the sub-opt-group or null
     */
    private HTMLOptGroup get( String name )
    {
        if ( children != null )
        {
            for ( int i=0;i<children.size();i++ )
            {
                Element e = children.get( i );
                if ( e instanceof HTMLOptGroup )
                {
                    HTMLOptGroup g = (HTMLOptGroup)e;
                    String label = g.getAttribute(HTMLNames.LABEL);
                    if ( label != null && label.equals(name) )
                        return g;
                }
            }
        }
        return null;
    }
    /**
     * Create a new child (optgroup or option)
     * @param path the full path to this option
     * @param child the array of nested children
     */
    public void add( String path, String[] child )
    {
        if ( child.length == 1 )
        {
            HTMLOption opt = new HTMLOption( 
                path, child[0] );
            addChild( opt );
        }
        else if ( child.length > 1 )
        {
            String fullPath = Utils.canonisePath(path,child[0]);
            HTMLOptGroup group = this.get(child[0]);
            if ( group == null )
                group = new HTMLOptGroup( Utils.canonisePath(path,child[0]) );
            String[] sub = new String[child.length-1];
            System.arraycopy( child, 1, sub, 0, child.length-1 );
            group.add( fullPath, sub );
            addChild( group );
        }
    }
}
