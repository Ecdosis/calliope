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
package calliope.tests;
import calliope.Service;
import calliope.Connector;
import calliope.Utils;
import calliope.constants.Database;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import calliope.exception.*;
import calliope.tests.html.HTML;
import calliope.tests.html.Element;
import calliope.tests.html.Text;
import calliope.tests.html.HTMLLiteral;
import calliope.tests.html.HTMLDocSelect;
import calliope.constants.HTMLNames;
import calliope.constants.Params;
import calliope.constants.Globals;
import calliope.AeseSpeller;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
/**
 * Example interface for importing files
 * @author desmond
 */
public class TestImport extends Test
{
    static String UPLOAD_CSS = 
    "div.upload\n{\n\tdisplay: inline-block;\n\t\tvertical-align: "
    +"top;\n}\ndiv.fields\n{\n\tdisplay: inline-block\n}\ndiv.wrap"
    +"per\n{\n\tmargin-left: auto;\n\tmargin-right: auto;\n}\ntabl"
    +"e.fields\n{\n\theight: 180px;\n}\ninput.invisible \n{ \n\tdi"
    +"splay: none \n} \ndiv.log \n{ \n\tbackground-color: white; \n"
    +"\tborder: 1px solid #EEE; padding: 5px\n}";
    static String UPLOAD_JS =
    "function checkform()\n{\n\tvar language = document.getElement"
    +"ById(\"LANGUAGE\");\n\tvar author = document.getElementById("
    +"\"AUTHOR\");\n\tvar work = document.getElementById(\"WORK\")"
    +";\n\tvar section = document.getElementById(\"SECTION\");\n\t"
    +"var subsection = document.getElementById(\"SUBSECTION\");\n\t"
    +"if ( check_files()&&fverify(language)&&fverify(author)&&fve"
    +"rify(work) )\n\t{\n\t\tvar docid = language.value+\"/\"+auth"
    +"or.value+\"/\"+work.value;\n\t\tif ( section.value.length > "
    +"0 )\n\t\t{\n\t\t\tdocid += \"/\"+section.value;\n\t\t\tif ( "
    +"subsection.value.length>0 )\n\t\t\t\tdocid += \"/\"+subsecti"
    +"on.value;\n\t\t}\n\t\tvar hidden = document.getElementById(\""
    +"DOC_ID\");\n\t\thidden.value =docid;\n\t\t\tvar demo = docu"
    +"ment.getElementById(\"demo\");\n\t\tif ( demo != null )\n\t\t"
    +"{\n\t\t\tvar password=prompt(\"Password\",\"\");\n\t\t\tdem"
    +"o.value = password;\n\t\t}\n\t\treturn true;\n\t}\n\telse\n\t"
    +"\treturn false;\n}\nfunction check_files()\n{\n\tvar repo= "
    +"document.getElementById(\"repository\");\n\tvar child = repo"
    +".firstChild;\n\tvar numChildren = 0;\n\twhile ( child != nul"
    +"l )\n\t{\n\t\tif ( child.nodeName==\"INPUT\" )\n\t\t\tnumChi"
    +"ldren++;\n\t\tchild = child.nextSibling;\n\t}\n\tif ( numChi"
    +"ldren == 0 )\n\t{\n\t\talert( \"specify at least one file fo"
    +"r upload\" );\n\t\treturn false;\n\t}\n\telse\n\t\treturn tr"
    +"ue;\n}\nfunction fverify( item )\n{ \n\tvar name = \"\";\n\t"
    +"if ( item !=null )\n\t{\n\t\tname = item.id;\n\t\tif ( item."
    +"value != null && item.value.length>0 )\n\t\t{\n\t\t\tvar cop"
    +"y = item.value.toString();\n\t\t\tcopy.replace(/\\s+/g,\"\")"
    +";\n\t\t\tif ( copy.length>0 )\n\t\t\treturn true;\n\t\t}\n\t"
    +"}\n\telse\n\t\tname = \"required fields\";\n\talert(name+\" "
    +"may not be empty\");\n\treturn false;\n}\nfunction remove( v"
    +"alue )\n{\n\tvar repo = document.getElementById(\"repository"
    +"\");\n\tvar child = repo.firstChild;\n\twhile ( child != nul"
    +"l )\n\t{\n\t\tif ( child.nodeName==\"INPUT\"&& child.value=="
    +"value )\n\t\t{\n\t\t\tchild.parentNode.removeChild(child);\n"
    +"\t\t\tbreak;\n\t\t}\n\t\tchild = child.nextSibling;\n\t}\n\t"
    +"var table = document.getElementById(\"listing\");\n\tchild ="
    +" table.firstChild;\n\tvar finished = false;\n\twhile ( child"
    +" != null && !finished )\n\t{\n\t\tif ( child.nodeName==\"TR\""
    +" )\n\t\t{\n\t\t\tvar gchild = child.firstChild;\n\t\t\twhil"
    +"e ( gchild != null )\n\t\t\t{\n\t\t\t\tif ( gchild.nodeName="
    +"=\"TD\" )\n\t\t\t\t{\n\t\t\t\t\tvar ggchild = gchild.firstCh"
    +"ild;\n\t\t\t\t\tif ( ggchild!=null&&ggchild.nodeName==\"SPAN"
    +"\" )\n\t\t\t\t\t{ \n\t\t\t\t\t\tif ( ggchild.textContent!=nu"
    +"ll&&ggchild.textContent==value )\n\t\t\t\t\t\t{\n\t\t\t\t\t\t"
    +"\tfinished = true;\n\t\t\t\t\t\t\tchild.parentNode.removeCh"
    +"ild(child);\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t\t\t\t"
    +"}\n\t\t\t\t}\n\t\t\t\tgchild = gchild.nextSibling;\n\t\t\t}\n"
    +"\t\t}\n\t\tchild = child.nextSibling;\n\t}\n}\nfunction alr"
    +"eadySelected( path, listing )\n{\n\tvar row = listing.firstC"
    +"hild;\n\twhile ( row != null )\n\t{\n\t\tif ( row.firstChild"
    +".textContent==path )\n\t\t\treturn true;\n\t\trow = row.next"
    +"Sibling;\n\t}\n\treturn false;\n}\nfunction clearFileInput( "
    +"input1 )\n{\n\tvar parent = input1.parentNode;\n\tvar input2"
    +" = document.createElement('input');\n\tinput2.setAttribute( "
    +"\"type\", \"file\" );\n\tinput2.setAttribute( \"name\", \"up"
    +"loadedfile[]\" );\n\tinput2.setAttribute( \"onchange\", \"do"
    +"addfile()\" );\n\tinput1.setAttribute( \"class\", \"invisibl"
    +"e\" );\n\tinput1.removeAttribute(\"id\");\n\tinput2.setAttri"
    +"bute(\"id\",\"input1\");\n\tparent.removeChild( input1 );\n\t"
    +"parent.appendChild( input2 );\n}\nfunction doaddfile()\n{\n"
    +"\tvar input1 = document.getElementById(\"input1\");\n\tvar r"
    +"epo = document.getElementById(\"repository\");\n\tvar listin"
    +"g = document.getElementById(\"listing\");\n\tif ( !alreadySe"
    +"lected(input1.value,listing) )\n\t{\n\t\tclearFileInput( inp"
    +"ut1 );\n\t\trepo.appendChild( input1 );\n\t\t// now create t"
    +"he row in the listing table\n\t\tvar text = document.createT"
    +"extNode(input1.value);\n\t\tvar span = document.createElemen"
    +"t(\"span\");\n\t\tspan.appendChild(text);\n\t\tvar row = doc"
    +"ument.createElement(\"tr\");\n\t\tvar cell1 = document.creat"
    +"eElement(\"td\");\n\t\tcell1.appendChild(span );\n\t\trow.ap"
    +"pendChild( cell1 );\n\t\t// add a button to remove the entry"
    +"\n\t\tvar button = document.createElement(\"input\");\n\t\tb"
    +"utton.setAttribute(\"type\",\"button\");\n\t\tbutton.setAttr"
    +"ibute(\"onclick\",\"remove('\"+input1.value.replace(/\\\\/g,"
    +"\"\\\\\\\\\")+\"')\");\n\t\tbutton.setAttribute(\"class\",\""
    +"remove\");\n\t\tbutton.setAttribute(\"value\",\"remove\");\n"
    +"\t\tvar cell2 = document.createElement(\"td\");\n\t\trow.app"
    +"endChild( cell2 );\n\t\tcell2.appendChild( button );\n\t\tif"
    +" ( listing.firstChild == null )\n\t\t\tlisting.appendChild( "
    +"row );\n\t\telse \n\t\t listing.insertBefore( row, listing.f"
    +"irstChild );\n\t}\n\telse\n\t{\n\t\tclearFileInput( input1 )"
    +";\n\t\talert(\"You have already chosen that file!\");\n\t}\n"
    +"}\nfunction update_group()\n{\n\tvar corform = document.getE"
    +"lementById(\"CORFORM\");\n\tvar groupSpan = document.getElem"
    +"entById(\"GROUP\");\n\tif ( groupSpan != undefined )\n\t{\n\t"
    +"\tvar path = corform.options[corform.selectedIndex].value;\n"
    +"\t\tvar parts = path.split(\"/\");\n\t\tvar group = \"\";\n"
    +"\t\tfor ( var i=0;i<parts.length-1;i++ )\n\t\t{\n\t\t\tif ( "
    +"group.length > 0 )\n\t\t\t\tgroup += \"-\";\n\t\t\tgroup += "
    +"parts[i];\n\t\t}\n\t\tif ( group.length > 0 )\n\t\t\tgroup +"
    +"= \": \";\n\t\tgroupSpan.textContent = group;\n\t}\n}";
    private static String LANG_TIP = "Specify a language for intelligent"
        +" hyphenation";
    private static String STYLE_TIP = "Specify a CSS format name to"
        +" associate with the document";
    private static String FILTER_TIP = "Specify a filter to add "
        +"markup to plain text documents";
    private static String LANGUAGE_TIP = "Specify the main language of the "
        +"work: (required)";	
    private static String AUTHOR_TIP = "Specify the author's name (required)"; 	
    private static String WORK_TIP = "Supply the name of the work (required)";	
    private static String SECTION_TIP = "Subdivide the work by uploading only "
        +"a section. Give it a name (optional)";	
    private static String SUBSECTION_TIP = "Subdivide the work further by "
        +"only uploading a sub-section. Give it a name (optional)";
    private static String BROWSE_TIP = "Choose XML or plain text files "
        +"belonging to ONE work (dissimilar files will be rejected)";
    private static String UPLOAD_TIP = "Click here when all versions of the "
        +"work or part-work have been selected";
    private static String LENGTH_TIP = "Turn on/off restrictions on length. "
        +"If set, files differing greatly in length will be rejected.";
    private String log;
    /**
     * Display the test GUI, selecting the default Home tab
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException
    {
        doc = new HTML();
        doc.getHeader().addCSS( UPLOAD_CSS );
        doc.getHeader().addScript( UPLOAD_JS );
        String contentType = request.getContentType();
        if ( contentType != null && contentType.startsWith("multipart") )
            log = redirect( request );
        super.handle( request, response, urn );
    }
    /**
     * Redirect the upload data to the mixed import handler
     * @param request the request in question
     * @return the log output form the upload
     * @throws AeseException 
     */
    String redirect( HttpServletRequest request ) throws AeseException
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            URL mixedImport = new URL("http://localhost"+Service.PREFIX+"/import/mixed/");
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) mixedImport.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", request.getContentType());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream wr = connection.getOutputStream();
            InputStream in = request.getInputStream();
            byte[] buffer = new byte[8192]; 
            int read = in.read(buffer,0, buffer.length);
            while (read >= 0) 
            {
               wr.write(buffer,0, read);
               read = in.read(buffer,0,buffer.length);
            }
            wr.flush();
            wr.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
            String inputLine;
            while ((inputLine = br.readLine()) != null) 
                sb.append(inputLine);
            return Utils.getHTMLBody(sb.toString());
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Make the upload div
     * @return a div Element
     */
    private Element makeUploadBox()
    {
        Element upload = new Element( "div" );
        upload.addAttribute(HTMLNames.CLASS, "upload" );
        Element prompt = new Element("div");
        upload.addChild( prompt );
        prompt.addText("Choose a file to upload: ");
        Element input1 = new Element( "input" );
        input1.addAttribute( HTMLNames.TYPE, HTMLNames.FILE );
        input1.addAttribute( HTMLNames.NAME, HTMLNames.FILE );
        input1.addAttribute( HTMLNames.ID, "input1" );
        input1.addAttribute( HTMLNames.ONCHANGE, "doaddfile()" );
        input1.addAttribute( "title", BROWSE_TIP );
        prompt.addChild( input1 );
        Element table = new Element("table");
        table.addAttribute(HTMLNames.ID, "listing");
        upload.addChild( table );
        Element repo = new Element("div");
        repo.addAttribute(HTMLNames.ID, "repository");
        upload.addChild( repo );
        Element input2 = new Element( "input" );
        input2.addAttribute( HTMLNames.TYPE, "submit");
        input2.addAttribute( HTMLNames.VALUE, "Upload files" );
        input2.addAttribute( "title", UPLOAD_TIP );
        upload.addChild( input2 );
        return upload;
    }
    /**
     * Make all the input fields and the filter dropdown.
     * @return an enclosing div Element
     */
    private Element makeTextFields()
    {
        Element div = new Element( "div" );
        div.addAttribute( HTMLNames.CLASS, "fields" );
        
        Element table = new Element( "table" );
        Element row1 = new Element( "tr" );
        Element cell1 = new Element( "td" );
        table.addChild( row1 );
        row1.addChild( cell1 );
        cell1.addText("Language*: ");
        Element language = new Element("input");
        language.addAttribute(HTMLNames.TYPE,"text");
        language.addAttribute(HTMLNames.ID,Params.LANGUAGE);
        Element cell2 = new Element( "td" );
        cell2.addChild( language );
        cell2.addAttribute("title",LANGUAGE_TIP);
        row1.addChild( cell2 );
        
        Element row2 = new Element( "tr" );
        Element cell3 = new Element( "td" );
        table.addChild( row2 );
        row2.addChild( cell3 );
        cell3.addText(" Author*: ");
        Element cell4 = new Element( "td" );
        Element author = new Element("input");
        author.addAttribute(HTMLNames.TYPE,"text");
        author.addAttribute(HTMLNames.ID,Params.AUTHOR);
        cell4.addChild( author );
        cell4.addAttribute("title",AUTHOR_TIP);
        row2.addChild( cell4 );
        
        Element row3 = new Element( "tr" );
        Element cell5 = new Element( "td" );
        table.addChild( row3 );
        row3.addChild( cell5 );
        cell5.addText(" Work*: ");
        Element cell6 = new Element( "td" );
        Element work = new Element("input");
        work.addAttribute(HTMLNames.TYPE,"text");
        work.addAttribute(HTMLNames.ID,Params.WORK);
        cell6.addChild( work );
        cell6.addAttribute("title",WORK_TIP);
        row3.addChild( cell6 );
        
        Element row4 = new Element( "tr" );
        Element cell7 = new Element( "td" );
        table.addChild( row4 );
        row4.addChild( cell7 );
        cell7.addText("Section: ");
        Element cell8 = new Element( "td" );
        Element section = new Element("input");
        section.addAttribute(HTMLNames.TYPE,"text");
        section.addAttribute(HTMLNames.ID,Params.SECTION);
        cell8.addChild( section );
        cell8.addAttribute("title",SECTION_TIP);
        row4.addChild( cell8 );
        
        Element row5 = new Element( "tr" );
        Element cell9 = new Element( "td" );
        table.addChild( row5 );
        row5.addChild( cell9 );
        cell9.addText(" Subsection: ");
        Element cell10 = new Element( "td" );
        Element subsection = new Element("input");
        subsection.addAttribute(HTMLNames.TYPE,"text");
        subsection.addAttribute(HTMLNames.ID,Params.SUBSECTION);
        cell10.addChild( subsection );
        cell10.addAttribute("title",SUBSECTION_TIP);
        row5.addChild( cell10 );
        
        Element row6 = new Element( "tr" );
        Element cell11 = new Element( "td" );
        table.addChild( row6 );
        row6.addChild( cell11 );
        cell11.addText( "Filter: " );
        cell11.addAttribute("title",FILTER_TIP);
        Element filters = new Element( "select" );
        filters.addAttribute( HTMLNames.NAME, Params.FILTER );
        Element option1 = new Element( "option" );
        Element option2 = new Element( "option" );
        Element option3 = new Element( "option" );
        Element option4 = new Element( "option" );
        Element option5 = new Element( "option" );
        filters.addChild( option1 );
        filters.addChild( option2 );
        filters.addChild( option3 );
        filters.addChild( option4 );
        filters.addChild( option5 );
        option1.addAttribute( HTMLNames.VALUE, "Empty" );
        option1.addText( "Empty" );
        option2.addAttribute( HTMLNames.VALUE, "CCE" );
        option2.addText( "CCE" );
        option3.addAttribute( HTMLNames.VALUE, "Poem" );
        option3.addText( "Poem" );
        option4.addAttribute( HTMLNames.VALUE, "Play" );
        option4.addText( "Play" );
        option5.addAttribute( HTMLNames.VALUE, "Novel" );
        option5.addText( "Novel" );
        Element cell12 = new Element( "td" );
        cell12.addAttribute("title",FILTER_TIP);
        cell12.addChild( filters );
        row6.addChild( cell12 );
        div.addChild( table );
        table.addAttribute( HTMLNames.CLASS, "fields" );
        
        Element row7 = new Element( "tr" );
        Element cell13 = new Element( "td" );
        table.addChild( row7 );
        row7.addChild( cell13 );
        cell13.addText( "Style: " );
        cell13.addAttribute("title",STYLE_TIP);
        Element cell14 = new Element("td" );
        Element group4 = new Element(HTMLNames.SPAN);
        group4.addAttribute(HTMLNames.ID,"GROUP");
        cell14.addChild( group4 );
        cell14.addChild( makeCorformDropdown() );
        cell14.addAttribute("title",STYLE_TIP);
        row7.addChild( cell14 );
        
        Element row8 = new Element( "tr" );
        Element cell15 = new Element( "td" );
        table.addChild( row8 );
        row8.addChild( cell15 );
        cell15.addText( "Check length: " );
        cell15.addAttribute("title",LENGTH_TIP);
        Element cell16 = new Element("td" );
        Element checkbox = new Element( HTMLNames.INPUT );
        checkbox.addAttribute( HTMLNames.NAME, Params.SIMILARITY );
        checkbox.addAttribute( HTMLNames.TYPE, "checkbox" );
        checkbox.addAttribute( HTMLNames.VALUE, "1" );
        checkbox.addAttribute( "checked", "checked" );
        cell16.addChild( checkbox );
        cell16.addAttribute("title",LENGTH_TIP);
        row8.addChild( cell16 );
        
        Element row9 = new Element( "tr" );
        table.addChild( row9 );
        Element cell17 = new Element( "td" );
        cell17.addText( "Language: " );
        row9.addChild( cell17 );
        Element cell18 = new Element("td" );
        cell18.addChild( makeDictionaryDropdown() );
        cell18.addAttribute("title",LANG_TIP);
        row9.addChild( cell18 );
        
        return div;
    }
    Element makeHeader()
    {
        Element div = new Element( HTMLNames.DIV );
        div.addAttribute( HTMLNames.CLASS, "header" );
        Element header = new Element( "h3" );
        header.addText( "IMPORT" );
        Element p = new Element( "p" );
        p.addText("Select versions of a work or sections thereof "
            +"in plain text or XML");
        div.addChild( header );
        div.addChild( p );
        return div;
    }
    /**
     * Add a hidden DOC_ID parameter to the form
     * @return the prebuilt element
     */
    Element makeDocID()
    {
        Element docid = new Element( HTMLNames.INPUT );
        docid.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN );
        docid.addAttribute( HTMLNames.NAME, Params.DOC_ID );
        docid.addAttribute( HTMLNames.ID, Params.DOC_ID );
        return docid;
    }
    /**
     * Add a hidden DEMO parameter to the form to stop imports
     * @return the prebuilt element
     */
    Element makeDemoTag()
    {
        Element demo = new Element( HTMLNames.INPUT );
        demo.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN );
        demo.addAttribute( HTMLNames.NAME, Params.DEMO );
        demo.addAttribute( HTMLNames.ID, Params.DEMO );
        return demo;
    }
    /**
     * Make a dropdown menu of CorForms available on server
     * @return a select dropdown (possibly empty if it failed)
     */
    Element makeCorformDropdown()
    {
        try
        {
            String[] data = Connector.getConnection().listCollection(
                Database.CORFORM);
            // ensure tei default corform is the first selected
            int teiDefaultPos = 0;
            for ( int i=0;i<data.length;i++ )
            {
                if ( data[i].equals("TEI/default") )
                {
                    teiDefaultPos = i;
                    break;
                }
            }
            if ( teiDefaultPos != 0 )
            {
                String temp = data[0];
                data[0] = data[teiDefaultPos];
                data[teiDefaultPos] = temp;
            }
            if ( data != null )
            {
                HTMLDocSelect sel = new HTMLDocSelect( data, 
                    Params.STYLE, Params.STYLE);
                sel.addAttribute(HTMLNames.ONCHANGE,"update_group()");
                return sel;
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
            return new Text( "Failed to get CorForm catalog: "
                +e.getMessage() );
        }
        return new Element( HTMLNames.SELECT );
    }
    /**
     * Make a dropdown menu of dictionaries available on server
     * @return a select dropdown (possibly empty if it failed)
     */
    Element makeDictionaryDropdown()
    {
        try
        {
            String dictName = Locale.getDefault().toString();
            AeseSpeller aes = new AeseSpeller( dictName );
            String[] dicts = aes.listDicts();
            if ( dicts != null )
            {
                Map<String,String> map = new HashMap<String,String>();
                for ( int i=0;i<dicts.length;i++ )
                {
                    String[] parts = dicts[i].split("\t");
                    if ( parts.length==2 )
                    {
                        String language = Utils.languageName(parts[0]);
                        map.put( language+" "+ parts[1], parts[1] );
                    }
                }
                HTMLDocSelect sel = new HTMLDocSelect( map, Params.DICT, 
                    Params.DICT );
                sel.setDefaultValue( Globals.DEFAULT_DICT );
                return sel;
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
            return new Text( "Failed to get CorForm catalog: "
                +e.getMessage() );
        }
        return new Element( HTMLNames.SELECT );
    }
    /**
     * Get the content of this test: a form with an upload button 
     * and a dropdown menu for choosing a filter. 
     * @return the element enclosing the dialog
     */
    @Override
    public Element getContent()
    {
        try
        {
            Element form = new Element( HTMLNames.FORM );
            form.addAttribute( HTMLNames.NAME, HTMLNames.DEFAULT );
            form.addAttribute( HTMLNames.METHOD, "POST" );
            form.addAttribute( HTMLNames.ACTION, "/tests/import/" );
            form.addAttribute( HTMLNames.ENCTYPE, "multipart/form-data" );
            form.addAttribute( HTMLNames.ONSUBMIT, "return checkform()" );
            Element outer = new Element("div");
            outer.addAttribute( HTMLNames.CLASS, "wrapper");
            Element header = makeHeader();
            form.addChild( header );
            Element upload = makeUploadBox();
            Element fields = makeTextFields();
            Element docid = makeDocID();
            outer.addChild( fields );
            outer.addChild( upload );
            if ( log != null )
            {
                Element logDiv = new Element(HTMLNames.DIV );
                logDiv.addAttribute(HTMLNames.CLASS,"log");
                logDiv.addChild( new HTMLLiteral(log) );
                outer.addChild( logDiv );
            }
            outer.addChild( docid );
            if ( Globals.DEMO )
                outer.addChild( makeDemoTag() );
            form.addChild( outer );
            return form;
        }
        catch ( Exception e )
        {
            return new Text( "Failed HTML Import test: "
                +e.getMessage());
        }
    }
}
