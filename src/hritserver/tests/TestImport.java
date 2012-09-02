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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import hritserver.exception.*;
import hritserver.tests.html.HTML;
import hritserver.tests.html.Element;
import hritserver.tests.html.Text;
import hritserver.constants.HTMLNames;
import hritserver.constants.Params;
/**
 * Example interface for importing files
 * @author desmond
 */
public class TestImport extends Test
{
    boolean responding;
    static String UPLOAD_CSS = 
    "div.upload\n{\n\tdisplay: inline-block;\n\t\tvertical-align: "
    +"top;\n}\ndiv.fields\n{\n\tdisplay: inline-block\n}\ndiv.wrap"
    +"per\n{\n\tmargin-left: auto;\n\tmargin-right: auto;\n}\ntabl"
    +"e.fields\n{\n\theight: 180px;\n}\ninput.invisible { display:"
    +" none }";
    static String UPLOAD_JS =
    "function checkform()\n{\n\tvar language = document.getElement"
    +"ById(\"LANGUAGE\");\n\tvar author = document.getElementById("
    +"\"AUTHOR\");\n\tvar work = document.getElementById(\"WORK\")"
    +";\n\tvar section = document.getElementById(\"SECTION\");\n\t"
    +"var subsection = document.getElementById(\"SUBSECTION\");\n\t"
    +"if ( check_files()&&fverify(language)&&fverify(author)&&fve"
    +"rify(work) )\n\t{\n\t\tvar docid = \"%2F\"+language+\"%2F\"+"
    +"author+\"%2F\"+work;\n\t\tif ( section.length > 0 )\n\t\t{\n"
    +"\t\t\tdocid += \"%2F\"+section;\n\t\t\tif ( subsection.lengt"
    +"h>0 )\n\t\t\t\tdocid += \"%2F\"+subsection;\n\t\t}\n\t\tvar "
    +"hidden = document.getElementById(\"DOC_ID\");\n\t\thidden.se"
    +"tValue( docid );\n\t\t\treturn true;\n\t}\n\telse\n\t\tretur"
    +"n false;\n}\nfunction check_files()\n{\n\tvar repo= document"
    +".getElementById(\"repository\");\n\tvar child = repo.firstCh"
    +"ild;\n\tvar numChildren = 0;\n\twhile ( child != null )\n\t{"
    +"\n\t\tif ( child.nodeName==\"INPUT\" )\n\t\t\tnumChildren++;"
    +"\n\t\tchild = child.nextSibling;\n\t}\n\tif ( numChildren =="
    +" 0 )\n\t{\n\t\talert( \"specify at least one file for upload"
    +"\" );\n\t\treturn false;\n\t}\n\telse\n\t\treturn true;\n}\n"
    +"function fverify( item )\n{ \n\tvar name = \"\";\n\tif ( ite"
    +"m !=null )\n\t{\n\t\tname = item.name;\n\t\tif ( item.value "
    +"!= null && item.value.length>0 )\n\t\t{\n\t\t\tvar copy = it"
    +"em.value.toString();\n\t\t\tcopy.replace(/\\s+/g,\"\");\n\t\t"
    +"\tif ( copy.length>0 )\n\t\t\treturn true;\n\t\t}\n\t}\n\te"
    +"lse\n\t\tname = \"required fields\";\n\talert(name+\" may no"
    +"t be empty\");\n\treturn false;\n}\nfunction remove( value )"
    +"\n{\n\tvar repo = document.getElementById(\"repository\");\n"
    +"\tvar child = repo.firstChild;\n\twhile ( child != null )\n\t"
    +"{\n\t\tif ( child.nodeName==\"INPUT\"&& child.value==value "
    +")\n\t\t{\n\t\t\tchild.parentNode.removeChild(child);\n\t\t\t"
    +"break;\n\t\t}\n\t\tchild = child.nextSibling;\n\t}\n\tvar ta"
    +"ble = document.getElementById(\"listing\");\n\tchild = table"
    +".firstChild;\n\tvar finished = false;\n\twhile ( child != nu"
    +"ll && !finished )\n\t{\n\t\tif ( child.nodeName==\"TR\" )\n\t"
    +"\t{\n\t\t\tvar gchild = child.firstChild;\n\t\t\twhile ( gc"
    +"hild != null )\n\t\t\t{\n\t\t\t\tif ( gchild.nodeName==\"TD\""
    +" )\n\t\t\t\t{\n\t\t\t\t\tvar ggchild = gchild.firstChild;\n"
    +"\t\t\t\t\tif ( ggchild!=null&&ggchild.nodeName==\"SPAN\" )\n"
    +"\t\t\t\t\t{ \n\t\t\t\t\t\tif ( ggchild.textContent!=null&&gg"
    +"child.textContent==value )\n\t\t\t\t\t\t{\n\t\t\t\t\t\t\tfin"
    +"ished = true;\n\t\t\t\t\t\t\tchild.parentNode.removeChild(ch"
    +"ild);\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t\t\t\t}\n\t\t"
    +"\t\t}\n\t\t\t\tgchild = gchild.nextSibling;\n\t\t\t}\n\t\t}"
    +"\n\t\tchild = child.nextSibling;\n\t}\n}\nfunction doaddfile"
    +"()\n{\n\tvar input1 = document.getElementById(\"input1\");\n"
    +"\tvar repo = document.getElementById(\"repository\");\n\tvar"
    +" listing = document.getElementById(\"listing\");\n\tvar pare"
    +"nt = input1.parentNode;\n\tvar input2 = document.createEleme"
    +"nt('input');\n\tinput2.setAttribute( \"type\", \"file\" );\n"
    +"\tinput2.setAttribute( \"name\", \"uploadedfile[]\" );\n\tin"
    +"put2.setAttribute( \"onchange\", \"doaddfile()\" );\n\tinput"
    +"1.setAttribute( \"class\", \"invisible\" );\n\tinput1.remove"
    +"Attribute(\"id\");\n\tinput2.setAttribute(\"id\",\"input1\")"
    +";\n\tparent.removeChild( input1 );\n\tparent.appendChild( in"
    +"put2 );\n\trepo.appendChild( input1 );\n\tvar text = documen"
    +"t.createTextNode(input1.value);\n\tvar span = document.creat"
    +"eElement(\"span\");\n\tspan.appendChild(text);\n\tvar row = "
    +"document.createElement(\"tr\");\n\tvar cell1 = document.crea"
    +"teElement(\"td\");\n\tcell1.appendChild(span );\n\trow.appen"
    +"dChild( cell1 );\n\tvar button = document.createElement(\"in"
    +"put\");\n\tbutton.setAttribute(\"type\",\"button\");\n\tbutt"
    +"on.setAttribute(\"onclick\",\"remove('\"+input1.value.replac"
    +"e(/\\\\/g,\"\\\\\\\\\")+\"')\");\n\tbutton.setAttribute(\"cl"
    +"ass\",\"remove\");\n\tbutton.setAttribute(\"value\",\"remove"
    +"\");\n\tvar cell2 = document.createElement(\"td\");\n\trow.a"
    +"ppendChild( cell2 );\n\tcell2.appendChild( button );\n\tif ("
    +" listing.firstChild == null )\n\tlisting.appendChild( row );"
    +"\n\telse \n\t\tlisting.insertBefore( row, listing.firstChild"
    +" );\n}";
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
        super.handle( request, response, urn );
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
        row5.addChild( cell10 );
        
        Element row6 = new Element( "tr" );
        Element cell11 = new Element( "td" );
        table.addChild( row6 );
        row6.addChild( cell11 );
        cell11.addText( "Filter: " );
        Element filters = new Element( "select" );
        filters.addAttribute( HTMLNames.NAME, Params.FILTER );
        Element option1 = new Element( "option" );
        Element option2 = new Element( "option" );
        Element option3 = new Element( "option" );
        filters.addChild( option1 );
        filters.addChild( option2 );
        option1.addAttribute( HTMLNames.VALUE, "Empty" );
        option1.addText( "Empty" );
        option2.addAttribute( HTMLNames.VALUE, "Poem" );
        option2.addText( "Poem" );
        option3.addAttribute( HTMLNames.VALUE, "Play" );
        option3.addText( "Play" );
        Element cell12 = new Element( "td" );
        cell12.addChild( filters );
        row6.addChild( cell12 );
        div.addChild( table );
        table.addAttribute( HTMLNames.CLASS, "fields" );
        return div;
    }
    Element makeHeader()
    {
        Element div = new Element( HTMLNames.DIV );
        div.addAttribute( HTMLNames.CLASS, "header" );
        Element header = new Element( "h3" );
        header.addText( "IMPORT" );
        Element p = new Element( "p" );
        p.addText("Select versions of a work or sections thereof");
        div.addChild( header );
        div.addChild( p );
        return div;
    }
    Element makeDocID()
    {
        Element docid = new Element( HTMLNames.INPUT );
        docid.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN );
        docid.addAttribute( HTMLNames.NAME, "DOC_ID" );
        return docid;
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
            if ( responding )
            {
                Element log = new Element( "div" );
                Element heading = new Element( "h3" );
                heading.addText( "LOG" );
                log.addChild( heading );
                //log.addChild( content );
                return log;
            }
            else
            {
                Element form = new Element( HTMLNames.FORM );
                form.addAttribute( HTMLNames.NAME, HTMLNames.DEFAULT );
                form.addAttribute( HTMLNames.METHOD, "POST" );
                form.addAttribute( HTMLNames.ACTION, "/import/" );
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
                outer.addChild( docid );
                form.addChild( outer );
                return form;
            }
        }
        catch ( Exception e )
        {
            return new Text( "Failed HTML Import test: "
                +e.getMessage());
        }
    }
}
