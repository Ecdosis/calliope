/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;
import java.util.ArrayList;
import hritserver.tests.Test;
import hritserver.constants.Params;
/**
 * A customised table that looks like a tab-panel
 * @author desmond
 */
public class TabPanel extends Element
{
    ArrayList<String> tabs;
    String current;
    Test tInstance;
    public TabPanel()
    {
        super( "div" );
        addAttribute( new Attribute("class","TabbedPanels") );
        addAttribute( new Attribute( "id","TabbedPanels1") );
    }
    /**
     * Set the test instance to the current test
     * @param tInstance the test instance that spawned us
     */
    public void setTestInstance( Test tInstance )
    {
        this.tInstance = tInstance;
    }
    /**
     * Add a tab to the panel
     * @param tab the name of the tab
     */
    public void addTab( String tab )
    {
        if ( tabs == null )
            tabs = new ArrayList<String>();
        tabs.add( tab );
        if ( current == null )
            current = tab;
    }
    /**
     * Get an instance of a named Test class
     * @param name the short name (after the "Test" part of the class name)
     * @return null or an instance of the class
     */
    private Test getTestInstance( String name )
    {
        try
        {
            Class testClass = Class.forName( "hritserver.tests.Test"+name );
            Test tInstance = (Test)testClass.newInstance();
            return tInstance;
        }
        catch ( Exception e )
        {
            return null; 
        }
    }
    /**
     * Convert the binary structure of objects to text
     * @return a String
     */
    @Override
    public void build()
    {
        // for each entry in the tabs hash generate one header cell
        Element list = new Element("ul");
        addChild(list);
        for ( int i=0;i<tabs.size();i++ )
        {
            String key = tabs.get(i);
            Element item = new Element("li");
            item.addAttribute("class","TabbedPanelsTab" );
            if ( key.equals(current) )
            {
                item.extendAttribute("class","TabbedPanelsTabSelected");
                item.addText( key );
            }
            else
            {
                Element link = new Element( "a" );
                link.addAttribute( "href",
                    "javascript:document.forms.default.setAttribute('action','/tests/"
                    +key.toLowerCase()+"');document.forms.default.submit()");
                link.addText( key );
                Test instance = getTestInstance( key );
                if ( instance != null )
                    item.addAttribute( "title",instance.getDescription() );
                item.addChild( link );
            }
            list.addChild( item );
        }
        // now do the content
        Element group = new Element("div");
        group.addAttribute("class","TabbedPanelsContentGroup");
        Element groupItem = new Element("div");
        groupItem.addAttribute("class","TabbedPanelsContent");
        group.addChild( groupItem );
        addChild( group );
        //
        groupItem.addChild( (tInstance==null)?new Text("Empty")
            :tInstance.getContent() );
    }
    /**
     * Set the currently selected tab 
     * @param newCurrent the new value
     */
    public void setCurrent( String newCurrent )
    {
        for ( int i=0;i<tabs.size();i++ )
            if ( newCurrent.equals(tabs.get(i)) )
                this.current = newCurrent;
        // otherwise don't change it
    }
}