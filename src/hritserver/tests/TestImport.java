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
package hritserver.tests;
import hritserver.HritServer;
import hritserver.Utils;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import hritserver.exception.*;
import hritserver.tests.html.HTML;
import hritserver.tests.html.Element;
import hritserver.tests.html.Text;
import hritserver.tests.html.HTMLLiteral;
import hritserver.tests.html.HTMLDocSelect;
import hritserver.constants.HTMLNames;
import hritserver.constants.Params;
import hritserver.constants.Globals;
import java.net.URL;
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
    +"rify(work) )\n\t{\n\t\tvar docid = language.value+\"%2F\"+au"
    +"thor.value+\"%2F\"+work.value;\n\t\tif ( section.value.lengt"
    +"h > 0 )\n\t\t{\n\t\t\tdocid += \"%2F\"+section.value;\n\t\t\t"
    +"if ( subsection.value.length>0 )\n\t\t\t\tdocid += \"%2F\"+"
    +"subsection.value;\n\t\t}\n\t\tvar hidden = document.getEleme"
    +"ntById(\"DOC_ID\");\n\t\thidden.value =docid;\n\t\t\tvar dem"
    +"o = document.getElementById(\"demo\");\n\t\tif ( demo != nul"
    +"l )\n\t\t\tdemo.value = \"true\";\n\t\treturn true;\n\t}\n\t"
    +"else\n\t\treturn false;\n}\nfunction check_files()\n{\n\tvar"
    +" repo= document.getElementById(\"repository\");\n\tvar child"
    +" = repo.firstChild;\n\tvar numChildren = 0;\n\twhile ( child"
    +" != null )\n\t{\n\t\tif ( child.nodeName==\"INPUT\" )\n\t\t\t"
    +"numChildren++;\n\t\tchild = child.nextSibling;\n\t}\n\tif ("
    +" numChildren == 0 )\n\t{\n\t\talert( \"specify at least one "
    +"file for upload\" );\n\t\treturn false;\n\t}\n\telse\n\t\tre"
    +"turn true;\n}\nfunction fverify( item )\n{ \n\tvar name = \""
    +"\";\n\tif ( item !=null )\n\t{\n\t\tname = item.id;\n\t\tif "
    +"( item.value != null && item.value.length>0 )\n\t\t{\n\t\t\t"
    +"var copy = item.value.toString();\n\t\t\tcopy.replace(/\\s+/"
    +"g,\"\");\n\t\t\tif ( copy.length>0 )\n\t\t\treturn true;\n\t"
    +"\t}\n\t}\n\telse\n\t\tname = \"required fields\";\n\talert(n"
    +"ame+\" may not be empty\");\n\treturn false;\n}\nfunction re"
    +"move( value )\n{\n\tvar repo = document.getElementById(\"rep"
    +"ository\");\n\tvar child = repo.firstChild;\n\twhile ( child"
    +" != null )\n\t{\n\t\tif ( child.nodeName==\"INPUT\"&& child."
    +"value==value )\n\t\t{\n\t\t\tchild.parentNode.removeChild(ch"
    +"ild);\n\t\t\tbreak;\n\t\t}\n\t\tchild = child.nextSibling;\n"
    +"\t}\n\tvar table = document.getElementById(\"listing\");\n\t"
    +"child = table.firstChild;\n\tvar finished = false;\n\twhile "
    +"( child != null && !finished )\n\t{\n\t\tif ( child.nodeName"
    +"==\"TR\" )\n\t\t{\n\t\t\tvar gchild = child.firstChild;\n\t\t"
    +"\twhile ( gchild != null )\n\t\t\t{\n\t\t\t\tif ( gchild.no"
    +"deName==\"TD\" )\n\t\t\t\t{\n\t\t\t\t\tvar ggchild = gchild."
    +"firstChild;\n\t\t\t\t\tif ( ggchild!=null&&ggchild.nodeName="
    +"=\"SPAN\" )\n\t\t\t\t\t{ \n\t\t\t\t\t\tif ( ggchild.textCont"
    +"ent!=null&&ggchild.textContent==value )\n\t\t\t\t\t\t{\n\t\t"
    +"\t\t\t\t\tfinished = true;\n\t\t\t\t\t\t\tchild.parentNode.r"
    +"emoveChild(child);\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t"
    +"\t\t\t}\n\t\t\t\t}\n\t\t\t\tgchild = gchild.nextSibling;\n\t"
    +"\t\t}\n\t\t}\n\t\tchild = child.nextSibling;\n\t}\n}\nfunct"
    +"ion alreadySelected( path, listing )\n{\n\tvar row = listing"
    +".firstChild;\n\twhile ( row != null )\n\t{\n\t\tif ( row.fir"
    +"stChild.textContent==path )\n\t\t\treturn true;\n\t\trow = r"
    +"ow.nextSibling;\n\t}\n\treturn false;\n}\nfunction clearFile"
    +"Input( input1 )\n{\n\tvar parent = input1.parentNode;\n\tvar"
    +" input2 = document.createElement('input');\n\tinput2.setAttr"
    +"ibute( \"type\", \"file\" );\n\tinput2.setAttribute( \"name\""
    +", \"uploadedfile[]\" );\n\tinput2.setAttribute( \"onchange\""
    +", \"doaddfile()\" );\n\tinput1.setAttribute( \"class\", \"i"
    +"nvisible\" );\n\tinput1.removeAttribute(\"id\");\n\tinput2.s"
    +"etAttribute(\"id\",\"input1\");\n\tparent.removeChild( input"
    +"1 );\n\tparent.appendChild( input2 );\n}\nfunction doaddfile"
    +"()\n{\n\tvar input1 = document.getElementById(\"input1\");\n"
    +"\tvar repo = document.getElementById(\"repository\");\n\tvar"
    +" listing = document.getElementById(\"listing\");\n\tif ( !al"
    +"readySelected(input1.value,listing) )\n\t{\n\t\tclearFileInp"
    +"ut( input1 );\n\t\trepo.appendChild( input1 );\n\t\t// now c"
    +"reate the row in the listing table\n\t\tvar text = document."
    +"createTextNode(input1.value);\n\t\tvar span = document.creat"
    +"eElement(\"span\");\n\t\tspan.appendChild(text);\n\t\tvar ro"
    +"w = document.createElement(\"tr\");\n\t\tvar cell1 = documen"
    +"t.createElement(\"td\");\n\t\tcell1.appendChild(span );\n\t\t"
    +"row.appendChild( cell1 );\n\t\t// add a button to remove th"
    +"e entry\n\t\tvar button = document.createElement(\"input\");"
    +"\n\t\tbutton.setAttribute(\"type\",\"button\");\n\t\tbutton."
    +"setAttribute(\"onclick\",\"remove('\"+input1.value.replace(/"
    +"\\\\/g,\"\\\\\\\\\")+\"')\");\n\t\tbutton.setAttribute(\"cla"
    +"ss\",\"remove\");\n\t\tbutton.setAttribute(\"value\",\"remov"
    +"e\");\n\t\tvar cell2 = document.createElement(\"td\");\n\t\t"
    +"row.appendChild( cell2 );\n\t\tcell2.appendChild( button );\n"
    +"\t\tif ( listing.firstChild == null )\n\t\t\tlisting.append"
    +"Child( row );\n\t\telse \n\t\t listing.insertBefore( row, li"
    +"sting.firstChild );\n\t}\n\telse\n\t{\n\t\tclearFileInput( i"
    +"nput1 );\n\t\talert(\"You have already chosen that file!\");"
    +"\n\t}\n}\nfunction update_group()\n{\n\tvar corform = docume"
    +"nt.getElementById(\"CORFORM\");\n\tvar groupSpan = document."
    +"getElementById(\"GROUP\");\n\tif ( groupSpan != undefined )\n"
    +"\t{\n\t\tvar path = corform.options[corform.selectedIndex]."
    +"value;\n\t\tvar parts = path.split(\"/\");\n\t\tvar group = "
    +"\"\";\n\t\tfor ( var i=0;i<parts.length-1;i++ )\n\t\t{\n\t\t"
    +"\tif ( group.length > 0 )\n\t\t\t\tgroup += \"-\";\n\t\t\tgr"
    +"oup += parts[i];\n\t\t}\n\t\tif ( group.length > 0 )\n\t\t\t"
    +"group += \": \";\n\t\tgroupSpan.textContent = group;\n\t}\n}";
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
    private String log;
    /**
     * Display the test GUI, selecting the default Home tab
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws HritException
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
     * @throws HritException 
     */
    String redirect( HttpServletRequest request ) throws HritException
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            URL mixedImport = new URL("http://localhost/import/mixed/");
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
            throw new HritException( e );
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
        filters.addChild( option1 );
        filters.addChild( option2 );
        filters.addChild( option3 );
        option1.addAttribute( HTMLNames.VALUE, "Empty" );
        option1.addText( "Empty" );
        option2.addAttribute( HTMLNames.VALUE, "Poem" );
        option2.addText( "Poem" );
        option3.addAttribute( HTMLNames.VALUE, "Play" );
        option3.addText( "Play" );
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
     * Ensure that we get a response from the server
     * @return a non-null server response or raise an exception
     * @throws HritException 
     */
    byte[] pollServer() throws HritException
    {
        byte[] data = null;
        int count = 0;
        while ( data == null && count < 5 )
        {
            data = HritServer.getFromDb("/corform/_all_docs/");
            count++;
        }
        if ( data == null )
            throw new HritException("No response from database");
        return data;
    }
    /**
     * Make a dropdown menu of CorForms available on server
     * @return a select dropdown (possibly empty if it failed)
     */
    Element makeCorformDropdown()
    {
        try
        {
            byte[] data = pollServer();
            if ( data != null )
            {
                HTMLDocSelect sel = new HTMLDocSelect(new String(data), 
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
