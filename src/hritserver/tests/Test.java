/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests;
import hritserver.HritServer;
import hritserver.handler.HritHandler;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import hritserver.constants.Params;
import hritserver.constants.HTMLNames;
import hritserver.constants.JSONKeys;
import hritserver.exception.*;
import hritserver.json.JSONDocument;
import hritserver.tests.html.*;
import java.util.ArrayList;
/**
 *
 * @author desmond
 */
public abstract class Test extends HritHandler
{
    // names of tabs we support
    public static String tabs = "Home Compare Html Import Table";
    /** contains a leading slash */
    protected String version1;
    protected String description;
    protected HTML doc;
    /** ID of the document to test with */
    protected String docID;
    static String SPRY_PANEL_CSS = 
    ".TabbedPanels { margin: 0px; padding: 0px; float: left; clear"
    +": none; width: 100%; }\n.TabbedPanelsTabGroup { margin: 0px;"
    +" padding: 0px;}\n.TabbedPanelsTab { position: relative; top:"
    +" 1px; float: left;padding: 4px 10px; margin: 0px 1px 0px 0px"
    +"; font: bold 0.7em sans-serif;background-color: #DDD; list-s"
    +"tyle: none; border-left: solid 1px #CCC;border-bottom: solid"
    +" 1px #999; border-top: solid 1px #999;border-right: solid 1p"
    +"x #999; -moz-user-select: none;-khtml-user-select: none; cur"
    +"sor: pointer; }\n.TabbedPanelsTabHover { background-color: #"
    +"CCC; }.TabbedPanelsTabSelected { background-color: #EEE; bor"
    +"der-bottom: 1px solid #EEE;}\n.TabbedPanelsTab a { color: bl"
    +"ack; text-decoration: none; }\n.TabbedPanelsContentGroup { c"
    +"lear: both; border-left: solid 1px #CCC;border-bottom: solid"
    +" 1px #CCC; border-top: solid 1px #999;border-right: solid 1p"
    +"x #999; background-color: #EEE; }\n.TabbedPanelsContent { pa"
    +"dding: 4px; }\n.VTabbedPanels .TabbedPanelsTabGroup { float:"
    +" left; width: 10em;height: 20em; background-color: #EEE; pos"
    +"ition: relative;border-top: solid 1px #999; border-right: so"
    +"lid 1px #999;border-left: solid 1px #CCC; border-bottom: sol"
    +"id 1px #CCC;}\n.VTabbedPanels .TabbedPanelsTab { float: none"
    +"; margin: 0px;border-top: none; border-left: none; border-ri"
    +"ght: none; }\n.VTabbedPanels .TabbedPanelsTabSelected { back"
    +"ground-color: #EEE;border-bottom: solid 1px #999;}\n.VTabbed"
    +"Panels .TabbedPanelsContentGroup { clear: none; float: left;"
    +"padding: 0px; width: 30em; height: 20em;}"
    /*    +"\nh1 {color:darkgrey;font-variant: small-caps}"*/;
    public Test()
    {
        // set default docID
        docID = "english/shakespeare/kinglear/act1/scene1";
    }
    private String extractTabName( String urn )
    {
        int pos = tabs.indexOf(" ");
        String defTab = tabs.substring(0,pos);
        String tab = defTab;
        pos = urn.indexOf('/', 1);
        if ( pos != -1 )
        {
            tab = urn.substring(pos+1);
            if ( tab.length()>0 )
            {
                if ( tab.endsWith("/") )
                    tab = tab.substring(0,tab.length()-1);
                if ( tab.length()>0 )
                    tab = Character.toUpperCase(tab.charAt(0))+tab.substring(1);
                else
                    tab = defTab;
            }
            else
                tab = defTab;
        }
        return tab;
    }
    /**
     * Save the given param as a hidden input field within the form if any
     * @param doc the doc with a form to save it in
     * @param key the param's key
     * @param value its value
     */
    void rememberParam( Element doc, String key, String value )
    {
        Element form = doc.getElementByTagName( HTMLNames.FORM );
        Element child = doc.getElementById( key );
        if ( form != null && child == null )
        {
            Element elem = new Element( HTMLNames.INPUT );
            elem.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN );
            elem.addAttribute( HTMLNames.NAME, key );
            elem.addAttribute( HTMLNames.ID, key );
            elem.addAttribute( HTMLNames.VALUE, value );
            form.addChild( elem );
        }
        // else not required or already set
    }
    /**
     * Get the docID of the first document you find in the database
     * @return its docID
     * @throws HritException if there were no docIDs
     */
    private String getDefaultDocID() throws HritException
    {
        try
        {
            byte[] data = HritServer.getFromDb("/cortex/_all_docs/");
            if ( data != null )
            {
                String json = new String( data, "UTF-8" );
                JSONDocument jdoc = JSONDocument.internalise( json );
                if ( jdoc == null )
                    throw new HritException("Failed to internalise all docs");
                ArrayList docs = (ArrayList) jdoc.get( JSONKeys.ROWS );
                if ( docs.size()>0 )
                {
                    JSONDocument d = (JSONDocument)docs.get(0);
                    return (String) d.get( JSONKeys.KEY );
                }
                else
                    throw new HritException("document list is empty");
            }
            else 
                throw new HritException("no docs in database");
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
    /**
     * Replace slashes and spaces with escaped equivalents
     * @param docID the raw docID
     * @return the escaped version suitable for the database
     */
    String docIDCanonise( String docID )
    {
        if ( docID.startsWith("/") )
            docID = docID.substring(1);
        docID = docID.replace("/","%2F");
        return docID.replace(" ","%20");
    }
    /**
     * Get the default version1 for the current docID
     * @return the name of version1 
     * @throws HritException if there was no such field
     */
    private String getDefaultVersion1() throws HritException
    {
        try
        {
            byte[] data = HritServer.getFromDb("/cortex/"+docIDCanonise(docID));
            if ( data != null )
            {
                String json = new String( data, "UTF-8" );
                JSONDocument doc = JSONDocument.internalise( json );
                if ( !doc.containsKey(JSONKeys.VERSION1) )
                    throw new HritException("Doc "+docID
                        +" does not contain a "+JSONKeys.VERSION1+" field");
                return (String)doc.get(JSONKeys.VERSION1);
            }
            else 
                throw new HritException("no docs in database");
        }
        catch ( Exception e )
        {
            throw new HritException( e );
        }
    }
    /**
     * Set the docID from the request
     * @param request the http request
     */
    void setDocID( HttpServletRequest request ) throws HritException
    {
        String prevDocID = docID;
        if ( request.getParameter(Params.DOC_ID) != null )
            docID = request.getParameter(Params.DOC_ID);
        else
            docID = getDefaultDocID();
        if ( request.getParameter(Params.VERSION1) != null 
            && prevDocID!=null && docID.equals(prevDocID) )
            version1 = request.getParameter(Params.VERSION1);
        else
            version1 = getDefaultVersion1();
    }
    /**
     * Display the test GUI, selecting the default Home tab
     * @param request the request to read from
     * @param urn the original URN - ignored
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
    {
        try
        {
            TabPanel panel = new TabPanel();
            panel.setTestInstance( this );
            String[] children = Test.tabs.split(" ");
            for ( int i=0;i<children.length;i++ )
            {
                panel.addTab( children[i] );
            }
            // default to first subclass
            String name = children[0];// seems redundant
            name = extractTabName( urn );
            panel.setCurrent( name );
            setDocID( request );
            if ( doc == null )
                doc = new HTML();
            /*Element h1 = new Element( "h1" );
            h1.addText( "HritServer Sample GUIs and Tests");
            doc.add( h1 );
            */
            doc.getHeader().addCSS(SPRY_PANEL_CSS);
            doc.add( panel );
            doc.build();
            //if ( version1 != null )
            //    rememberParam( doc, Params.VERSION1, version1 );
            if ( docID != null )
                rememberParam( doc, Params.DOC_ID, docID );
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(doc.toString());
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
            HTML doc = new HTML();
            Element p = new Element("p");
            p.addText( "Error: "+e.getMessage() );
            response.setContentType("text/html;charset=UTF-8");
            try 
            {
                response.getWriter().println(doc.toString());
            }
            catch ( Exception e2 )
            {
                throw new HritException( e2 );
            }
        }
    }
    /**
     * Get the content of this tab (mostly handled in subclasses)
     * If any nodes are added in overrides of this method call build on them 
     * @return an element
     */
    public Element getContent()
    {
        return new Text(description);
    }
    /**
     * Get the description string
     * @return a String
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * Add a get param to an existing URL in string form
     * @param url the url in question
     * @param name the name of the parameter
     * @param value its value
     * @return the full url
     */
    protected String addGetParam( String url, String name, String value )
    {
        StringBuilder sb = new StringBuilder(url);
        if ( !url.contains("?") )
            sb.append("?");
        else
            sb.append("&");
        sb.append(name);
        sb.append("=");
        sb.append(value);
        return sb.toString();       
    }
}
