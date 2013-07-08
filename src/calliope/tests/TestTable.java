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
import calliope.Utils;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.util.HashSet;
import calliope.tests.html.*;
import calliope.exception.*;
import calliope.constants.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
/**
 * Test the AeseDocument (XML) Class
 * @author desmond
 */
public class TestTable extends Test
{
    boolean hideMerged = false;
    boolean wholeWords = false;
    boolean compact = false;
    boolean someVersions = false;
    String selectedVersions;
    int offset = 0;
    int length = 100;
    static String EXPLANATION = 
        "<h3>Table View</h3>\n<p>Table view is one way to visualise va"
        +"riation in a multi-version text. Alternative expressions in "
        +"parallel are stacked one above another in a strict table for"
        +"mat, with the \"base\" version at the bottom. The base versi"
        +"on is the one against which the other versions are compared."
        +" Differences with the base are highlighted in blue. This all"
        +"ows an editor to make clear comparisons between versions. Th"
        +"e difference display may be further customised via the follo"
        +"wing options:</p>\n<ol>\n<li>Hide merged: wherever the text "
        +"is the same across all versions, do not repeat text in versi"
        +"ons other than the base.</li>\n<li>Whole words: if a differe"
        +"nce is only part of a word, copy the merged text on either s"
        +"ide so that whole words are displayed.</li>\n<li>Compact: wh"
        +"ere versions are more than 90% similar, merge them and displ"
        +"ay the remaining differences in a collapsable/expandable tab"
        +"le with a dotted underlining.</li>\n<li>Length: The length o"
        +"f the range in the base text to compare with other versions."
        +" <em>Warning:</em> not checked for overshoot.</li>\n<li>Star"
        +"t offset: The start position of the range counting from the "
        +"start of the base version. <em>Warning:</em> not checked for"
        +" overshoot.</li>\n<li>Some versions: By default all versions"
        +" encountered in the range are used. Clicking \"some versions"
        +"\" restricts the comparison to those selected via a \"&times"
        +";\".</li>\n</ol>";
    static String TABLE_CSS = 
        "td {white-space: nowrap;padding:0;border:0;}\ntr {border:0}\n"
        +"table {border:0}\ntd.siglum {font-weight: bold; padding-righ"
        +"t: 3px; padding-left: 3px;}\ntd.siglumhidden { display: none"
        +"; }\ntable {border-spacing: 0px 2px;}\ntable.inline:hover {c"
        +"ursor:pointer}\ntable.inline { display: inline-table; positi"
        +"on: relative; }\ntr.shown { display: table-row; }\ntr.hidden"
        +" { display: none; }\nspan.base { border-bottom: 1px dotted #"
        +"ba0000;}\nspan.inserted { color: blue; position: relative }\n"
        +"span.left {position: relative;float:left; }\nspan.right {po"
        +"sition: relative;float:right; }\nspan.inserted-left { positi"
        +"on: relative;float: left; color: blue}\nspan.inserted-right "
        +"{ position: relative;float: right; color: blue}";
    static String TABLE_JS = 
        "function getOffsetTopForElem( elem )\n{\n\tvar offset = 0;\n\t"
        +"var orig = elem;\n\tvar set = new Array();\n\tvar i = 0;\n\t"
        +"while ( elem != null )\n\t{\n\t\tvar temp = elem.offsetTop;"
        +"\n\t\tset[i++] = elem.nodeName+\"[\"+elem.offsetTop+\"]\";\n"
        +"\t\tif ( temp >=0 )\n\t\t\toffset += temp;\n\t\telem = elem."
        +"offsetParent;\n\t}\n\tvar string = \"\";\n\tfor ( var j=0;j<"
        +"i;j++ )\n\t{\n\t\tstring += set[j];\n\t\tstring += \" \";\n\t"
        +"}\n\t\treturn offset;\n}\nfunction findChild( elem, tagName"
        +" )\n{\n\tvar child = elem.firstChild;\n\tvar grandchild;\n\t"
        +"while ( child != null )\n\t{\n\t\tif ( child.nodeType == 1 )"
        +"\n\t\t{\n\t\t\tif ( child.nodeName == tagName )\n\t\t\t\tret"
        +"urn child;\n\t\t\telse\n\t\t\t{\n\t\t\t\tgrandchild = findCh"
        +"ild( child, tagName );\n\t\t\t\tif ( grandchild!= null )\n\t"
        +"\t\t\t\treturn grandchild;\n\t\t\t}\n\t\t}\n\t\tchild = chil"
        +"d.nextSibling;\n\t}\n\treturn null;\n}\nfunction findLastTr("
        +" table )\n{\n\tvar old,tr = findChild( table, \"TR\" );\n\tw"
        +"hile ( tr != null )\n\t{\n\t\tif ( tr.nodeType==1&&tr.nodeNa"
        +"me==\"TR\") \n\t\t\told = tr;\n\t\ttr = tr.nextSibling;\n\t}"
        +"\n\treturn old;\n}\nfunction hasClass( elem, className )\n{\n"
        +"\tvar cname = elem.getAttribute(\"class\");\n\treturn cname"
        +"!=null&&cname==className;\n}\nfunction findDirectChild( elem"
        +", tagName, className )\n{\n\tvar child = elem.firstChild;\n\t"
        +"while ( child != null )\n\t{\n\t\tif ( child.nodeType==1&&c"
        +"hild.nodeName==tagName )\n\t\t{\n\t\t\tif ( className != nul"
        +"l )\n\t\t\t{\n\t\t\t\t\t\t\t\tvar cname = child.getAttribute"
        +"(\"class\");\n\t\t\t\tif ( cname==null||cname!=className )\n"
        +"\t\t\t\t\treturn child;\n\t\t\t}\n\t\t\telse\n\t\t\t\treturn"
        +" child;\n\t\t}\n\t\tchild = child.nextSibling;\n\t}\n\tretur"
        +"n null;\n}\nfunction findParent( elem, tagName )\n{\n\tvar p"
        +"arent = elem.parentNode;\n\tvar grandparent = null;\n\twhile"
        +" ( parent != null )\n\t{\n\t\tif ( parent.nodeType==1 )\n\t\t"
        +"{\n\t\t\tif ( parent.nodeName == tagName )\n\t\t\t\treturn "
        +"parent;\n\t\t\telse\n\t\t\t{\n\t\t\t\tgrandparent = findPare"
        +"nt( parent, tagName );\n\t\t\t\tif ( grandparent != null )\n"
        +"\t\t\t\t\treturn grandparent;\n\t\t\t}\n\t\t}\n\t\tparent = "
        +"parent.nextSibling;\n\t}\n\treturn null;\n}\nfunction findBe"
        +"stTextSibling( elem, avoidClass )\n{\n\tvar best = null;\n\t"
        +"if ( elem.parentNode != null )\n\t{\n\t\tvar temp = elem.pre"
        +"viousSibling;\n\t\twhile ( best == null )\n\t\t{\n\t\t\tif ("
        +" temp != elem && temp.nodeName == elem.nodeName && temp.getA"
        +"ttribute(\"class\")!=avoidClass )\n\t\t\t{\n\t\t\t\tbest = f"
        +"indBestTextChild(temp);\n\t\t\t\tif ( best != null )\n\t\t\t"
        +"\t{\n\t\t\t\t\tbreak;\n\t\t\t\t}\n\t\t\t}\n\t\t\ttemp = temp"
        +".nextSibling; \n\t\t}\n\t}\n\treturn best;\n}\nfunction isSp"
        +"ace( text )\n{\n\tfor ( var i=0;i<text.length;i++ )\n\t\tif "
        +"( text.charCodeAt(i)>32 )\n\t\t\treturn false;\n\treturn tru"
        +"e;s\n}\nfunction findBestTextChild( elem )\n{\n\tvar best = "
        +"null;\n\tvar child = elem.firstChild;\n\twhile ( child != nu"
        +"ll )\n\t{\n\t\tif ( child.nodeType==1 )\n\t\t{\n\t\t\tif ( c"
        +"hild.nodeName!=\"TABLE\" )\n\t\t\t{\n\t\t\t\tvar temp = find"
        +"BestTextChild(child);\n\t\t\t\tif ( temp !=null && (best==nu"
        +"ll||(temp.nodeValue.length>best.nodeValue.length)) )\n\t\t\t"
        +"\t{\n\t\t\t\t\tif ( !isSpace(temp.nodeValue) )\n\t\t\t\t\t\t"
        +"best = temp;\n\t\t\t\t}\n\t\t\t}\n\t\t}\n\t\telse if ( child"
        +".nodeType==3 )\n\t\t{\n\t\t\tif ( best==null||child.nodeValu"
        +"e.length>best.nodeValue.length )\n\t\t\t\tif ( !isSpace(chil"
        +"d.nodeValue) )\n\t\t\t\t\tbest = child;\n\t\t}\n\t\tchild = "
        +"child.nextSibling;\n\t}\n\treturn best;\n}\nfunction wrapWit"
        +"hSpan( text )\n{\n\tvar origParent = text.parentNode;\n\tif "
        +"( origParent.nodeName!=\"SPAN\" )\n\t{\n\t\tvar span = docum"
        +"ent.createElement(\"span\");\n\t\torigParent.insertBefore(sp"
        +"an,text);\n\t\tspan.appendChild( text );\n\t\treturn span;\n"
        +"\t}\n\telse\n\t\treturn origParent;\n}\nfunction alignTable("
        +" table )\n{\n\tif ( table.hasAttribute(\"class\")&&table.get"
        +"Attribute(\"class\")==\"inline\" )\n\t{\n\t\tvar toggleID = "
        +"table.getAttribute(\"id\");\n\t\tif ( toggleID != null )\n\t"
        +"\t\ttable.setAttribute(\"onclick\",\"toggle('\"+toggleID+\"'"
        +")\");\n\t\tvar tr = findLastTr(table);\n\t\tvar parentTd = f"
        +"indParent( table, \"TD\" );\n\t\tvar childTd = null;\n\t\tif"
        +" ( tr != null )\n\t\t\tchildTd = findDirectChild( tr, \"TD\""
        +", \"siglumhidden\" );\n\t\tif ( childTd != null && parentTd "
        +"!= null )\n\t\t{\n\t\t\tvar bestTextChild = findBestTextChil"
        +"d(childTd);\n\t\t\tvar bestTextParent = findBestTextChild(pa"
        +"rentTd);\n\t\t\tif ( bestTextParent==null )\n\t\t\t{\n\t\t\t"
        +"\tbestTextParent = findBestTextSibling( parentTd, \"siglum\""
        +" );\n\t\t\t}\n\t\t\tif ( bestTextChild!=null&&bestTextParent"
        +"!=null)\n\t\t\t{\n\t\t\t\tvar childSpan = wrapWithSpan(bestT"
        +"extChild);\n\t\t\t\tvar parentSpan = wrapWithSpan(bestTextPa"
        +"rent);\n\t\t\t\tvar p = $(childSpan);\n\t\t\t\tvar q = $(par"
        +"entSpan);\n\t\t\t\tvar childTopOffset = p.offset().top;//get"
        +"OffsetTopForElem(childSpan);\n\t\t\t\tvar parentTopOffset = "
        +"q.offset().top;//getOffsetTopForElem(parentSpan);\n\t\t\t\tv"
        +"ar newValue = (parentTopOffset-childTopOffset)+\"px\";\n\t\t"
        +"\t\ttable.style.top = newValue;\n\t\t\t}\n\t\t}\n\t}\n}\nfun"
        +"ction alignTables()\n{\n\tvar tables = document.getElementsB"
        +"yTagName(\"table\");\n\tfor ( var i=0;i<tables.length;i++ )\n"
        +"\t{\n\t\talignTable( tables[i] );\n\t}\n}\nfunction swapChi"
        +"ldClass( node, name1, name2 )\n{\n\tvar child = node.firstCh"
        +"ild;\n\twhile ( child != null )\n\t{\n\t\tif ( child.nodeTyp"
        +"e==1 )\n\t\t{\n\t\t\tvar className = child.getAttribute(\"cl"
        +"ass\");\n\t\t\tif ( className != null )\n\t\t\t{\n\t\t\t\tif"
        +" ( className == name1 )\n\t\t\t\t\tclassName = name2;\n\t\t\t"
        +"\telse if ( className == name2 )\n\t\t\t\t\tclassName = nam"
        +"e1;\n\t\t\t\tchild.setAttribute( \"class\", className );\n\t"
        +"\t\t}\n\t\t}\n\t\tchild = child.nextSibling;\n\t}\n}\nfuncti"
        +"on toggle( id )\n{\n\tvar table = document.getElementById(id"
        +");\n\tvar child = table.firstChild;\n\t\twhile ( child != nu"
        +"ll )\n\t{\n\t\tif ( child.nodeType==1 )\n\t\t{\n\t\t\tif ( c"
        +"hild.tagName == \"TBODY\" )\n\t\t\t{\n\t\t\t\tvar grandChild"
        +" = child.firstChild;\n\t\t\t\twhile ( grandChild != null )\n"
        +"\t\t\t\t{\n\t\t\t\t\tif ( grandChild.nodeType==1)\n\t\t\t\t\t"
        +"{\n\t\t\t\t\t\tvar className = grandChild.getAttribute(\"cl"
        +"ass\");\n\t\t\t\t\t\tif ( className != null )\n\t\t\t\t\t\t{"
        +"\n\t\t\t\t\t\t\tif ( className == \"hidden\" )\n\t\t\t\t\t\t"
        +"\t\tclassName = \"shown\";\n\t\t\t\t\t\t\telse if ( classNam"
        +"e == \"shown\" )\n\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\tclassName"
        +" = \"hidden\";\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t"
        +"\tgrandChild.setAttribute(\"class\",className);\n\t\t\t\t\t"
        +"\t}\n\t\t\t\t\t\tswapChildClass(grandChild,\"siglumhidden\","
        +"\"siglum\");\n\t\t\t\t\t}\n\t\t\t\t\tgrandChild = grandChild"
        +".nextSibling;\n\t\t\t\t}\n\t\t\t}\n\t\t}\n\t\tchild = child."
        +"nextSibling;\n\t}\n}\nfunction checkmark( select )\n{\n\tvar"
        +" pos = select.options[select.selectedIndex].text.lastIndexOf"
        +"(\" ×\");\n\tif ( pos != -1 )\n\t{\n\t\tvar len = select.opt"
        +"ions[select.selectedIndex].text.length;\n\t\tvar copy = sele"
        +"ct.options[select.selectedIndex].text;\n\t\tselect.options[s"
        +"elect.selectedIndex].text = copy.substring(0,pos);\n\t}\n\te"
        +"lse\n\t{\n\t\tselect.options[select.selectedIndex].text += \""
        +" ×\";\n\t}\n}\nfunction presubmit()\n{\n\tvar s = document."
        +"getElementById(\"selector\");\n\tvar value = \"\";\n\tfor ( "
        +"var i=0;i<s.options.length;i++ )\n\t{\n\t\tif ( s.options[i]"
        +".text.lastIndexOf(\" ×\")!=-1 )\n\t\t{\n\t\t\tif ( value.len"
        +"gth>0 )\n\t\t\t\tvalue+=\",\";\n\t\t\tvalue += s.options[i]."
        +"value;\n\t\t}\n\t}\n\tvar versions = document.getElementById"
        +"(\"versions\");\n\tversions.value = value;\n}\nfunction togg"
        +"leVersionSelector( check )\n{\n\tvar s = document.getElement"
        +"ById(\"selector\");\n\tif ( check.value==\"on\" )\n\t{\n\t\t"
        +"var disabled = s.getAttribute(\"disabled\");\n\t\tif ( disab"
        +"led != null )\n\t\t\ts.removeAttribute(\"disabled\");\n\t}\n"
        +"\telse\n\t{\n\t\ts.setAttribute(\"disabled\",\"disabled\");\n"
        +"\t}\n}\nwindow.onload = alignTables;";
    public TestTable()
    {
        description = "Produces a variant table from sections of an MVD";
    }
    private void addCheckBox( Element form, boolean flag, String prompt, 
        String paramName, String onclick )
    {
        Element check = new Element(HTMLNames.INPUT);
        check.addAttribute(HTMLNames.TYPE,HTMLNames.CHECKBOX);
        check.addAttribute( HTMLNames.NAME, paramName );
        if ( flag )
            check.addAttribute( HTMLNames.CHECKED, null );
        if ( onclick != null )
            check.addAttribute( HTMLNames.ONCLICK, onclick );
        Element span = new Element(HTMLNames.SPAN);
        span.addText( prompt );
        form.addChild( span );
        form.addChild( check );
    }
    /**
     * Add the specified option to the selector
     * @param select the selector element
     * @param value the new value
     */
    private void addOption( Element select, String value, boolean selectit )
    {
        Element option = new Element( HTMLNames.OPTION );
        option.addText( value );
        if ( selectit )
            option.addAttribute( HTMLNames.SELECTED, HTMLNames.SELECTED );
        select.addChild( option );
    }
    /**
     * Add a dropdown menu of lengths
     * @param prompt the prompt explaining the menu
     * @param parent the parnet element to attach it to
     */
    private void addLengthSelector( String prompt, Element parent )
    {
        Element select = new Element( HTMLNames.SELECT );
        select.addAttribute( HTMLNames.NAME, Params.LENGTH );
        addOption( select, "100", length==100 );
        addOption( select, "150", length==150 );
        addOption( select, "200", length==200 );
        addOption( select, "250", length==250 );
        parent.addText( prompt );
        parent.addChild( select );
    }
    /**
     * Add a dropdown menu of lengths
     * @param prompt the prompt explaining the menu
     * @param parent the parent element to attach it to
     */
    private void addOffsetSelector( String prompt, Element parent )
    {
        Element select = new Element( HTMLNames.SELECT );
        select.addAttribute( HTMLNames.NAME, Params.OFFSET );
        addOption( select, "0", offset==0 );
        addOption( select, "100", offset==100 );
        addOption( select, "200", offset==200 );
        addOption( select, "300", offset==300 );
        addOption( select, "400", offset==400 );
        addOption( select, "500", offset==500 );
        addOption( select, "600", offset==600 );
        addOption( select, "700", offset==700 );
        parent.addText( prompt );
        parent.addChild( select );
    }
    private String getVersions() throws Exception
    {
        String rawURL = "http://localhost:8080/list";
        String urn = Utils.escape( docID );
        rawURL = calliope.URLEncoder.append( rawURL, urn );
        URL url = new URL( rawURL );
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        StringBuilder sb = new StringBuilder();
        while ( is.available() != 0 )
        {
            byte[] data = new byte[is.available()];
            is.read( data );
            sb.append( new String(data) );
        }
        return sb.toString();
    }
    /**
     * Truncate an array by popping off the leading element
     * @param array an array with at least 2 elements
     * @return a shortened array of length array.length-1
     */
    private String[] popArray( String[] array )
    {
        String[] newCols = new String[array.length-1];
        System.arraycopy( array, 1, newCols, 0, newCols.length );
        return newCols;
    }
    /**
     * See if the dropdown already has a group of that name, else create it
     * @param select the select element
     * @param name the name of the group
     * @return the optgroup or null
     */
    HTMLOptGroup getGroup( Element select, String name )
    {
        for ( int i=0;i<select.numChildren();i++ )
        {
            try
            {
                Element e = select.getChild(i);
                if ( e instanceof HTMLOptGroup )
                {
                    String label = e.getAttribute( HTMLNames.LABEL );
                    if ( label != null && label.equals(name) )
                        return (HTMLOptGroup)e;
                }
            }
            catch ( AeseException he )
            {
                System.out.println(he.getMessage() );
            }
        }
        return null;
    }
    /**
     * Add a dropdown menu containing all the nested versions and groups
     * @param parent the parent element to attach it to
     * @throws Exception 
     */
    void addVersionDropdown( Element parent ) throws Exception
    {
        Element select = new Element(HTMLNames.SELECT);
        String versions = getVersions();
        String[] opts = versions.split(",");
        // remove trailing LF-
        HashSet<String> selected = new HashSet<String>();
        if ( selectedVersions != null )
        {
            String[] shortNames = selectedVersions.split(",");
            for ( int i=0;i<shortNames.length;i++ )
                selected.add( shortNames[i] );
        }
        opts[opts.length-1] = opts[opts.length-1].trim();
        for ( int i=0;i<opts.length;i++ )
        {
            String[] cols = opts[i].split("/");
            if ( cols.length > 0 && cols[0].length()==0 )
                cols = popArray( cols );
            if ( cols.length > 1 )
            {
                HTMLOptGroup group = getGroup(select,cols[0]);
                if ( selected.contains(opts[i]) )
                    cols[cols.length-1] += " ×";
                if ( group == null )
                {
                    group = new HTMLOptGroup( cols[0] );
                    select.addChild( group );
                }
                group.add( opts[i], popArray(cols) );

            }
            else if ( cols.length == 1 )
            {
                Element option = new Element(HTMLNames.OPTION);
                select.addChild( option );
                String content = cols[cols.length-1];
                if ( selected.contains(opts[i]) )
                    content += " \327";
                option.addText( content );
                option.addAttribute( HTMLNames.VALUE, opts[i] );
            }              
        }
        parent.addChild( select );
        if ( !someVersions )
            select.addAttribute( HTMLNames.DISABLED, null );
        select.addAttribute(HTMLNames.ONCHANGE, "checkmark(this)" );
        select.addAttribute(HTMLNames.ID, "selector" );
        Element hidden = new Element(HTMLNames.INPUT);
        hidden.addAttribute(HTMLNames.TYPE,HTMLNames.HIDDEN);
        hidden.addAttribute(HTMLNames.ID, "versions" );
        hidden.addAttribute(HTMLNames.NAME, Params.SELECTED_VERSIONS );
        parent.addChild( hidden );
    }
    /**
     * Get the content of this test: a table of versions
     * @return a select element object with appropriate attributes and children
     */
    @Override
    public Element getContent()
    {
        try
        {
            Element div = new Element("div");
            String rawURL = "http://localhost:8080/html/table";
            String urn = Utils.escape( docID );
            rawURL = calliope.URLEncoder.append( rawURL, urn );
            // add required params
            rawURL = addGetParam( rawURL, Params.HIDE_MERGED, 
                (hideMerged)?"1":"0" );
            rawURL = addGetParam( rawURL, Params.COMPACT,(compact)?"1":"0" );
            rawURL = addGetParam( rawURL, Params.WHOLE_WORDS, 
                (wholeWords)?"1":"0" );
            rawURL = addGetParam( rawURL, Params.LENGTH, 
                Integer.toString(length) );
            rawURL = addGetParam( rawURL, Params.OFFSET, 
                Integer.toString(offset) );
            rawURL = addGetParam( rawURL, Params.SOME_VERSIONS, 
                (someVersions)?"1":"0" );
            if ( someVersions && selectedVersions != null )
                rawURL = addGetParam( rawURL, Params.SELECTED_VERSIONS, 
                    selectedVersions );
            URL url = new URL( rawURL );
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            StringBuilder sb = new StringBuilder();
            while ( is.available() != 0 )
            {
                byte[] data = new byte[is.available()];
                is.read( data );
                sb.append( new String(data,"UTF-8") );
            }
            div.addChild(new HTMLLiteral(sb.toString()) );
            Element panel = new Element(HTMLNames.DIV);
            Element form = formElement( "/tests/table/" );
            Element p1 = new Element(HTMLNames.P);
            form.addChild( p1 );
            addCheckBox( p1, hideMerged, "hide merged", Params.HIDE_MERGED, 
                null );
            addCheckBox( p1, wholeWords, "&nbsp;&nbsp;whole words", 
                Params.WHOLE_WORDS, null );
            addCheckBox( p1, compact, "&nbsp;&nbsp;compact", Params.COMPACT, 
                null );
            // submit button
            Element submit = new Element(HTMLNames.INPUT);
            submit.addAttribute(HTMLNames.TYPE,HTMLNames.SUBMIT);
            submit.addAttribute(HTMLNames.ONCLICK,"presubmit()");
            p1.addChild( submit );
            // next row of buttons
            Element p2 = new Element(HTMLNames.P);
            form.addChild( p2 );
            // length and offset
            addLengthSelector( "length:", p2 );
            addOffsetSelector( "&nbsp;&nbsp;start offset:", p2 );
            // all versions
            addCheckBox( p2, someVersions, "&nbsp;&nbsp;some versions",
                Params.SOME_VERSIONS, "toggleVersionSelector(this)" );
            addVersionDropdown( p2 );
            panel.addChild( form );
            div.addChild( panel );
            Element explanation = new Element(HTMLNames.DIV );
            Element p3 = new HTMLLiteral( EXPLANATION );
            explanation.addChild( p3 );
            div.addChild( explanation );
            return div;
        }
        catch ( Exception e )
        {
            return new Text( "Failed Table (HTML) test: "
                +e.getMessage());
        }
    }
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
        doc.getHeader().addScriptSrc( "http://"+Globals.JQUERY_SITE
            +"/jquery-latest.js" );
        doc.getHeader().addScript(TABLE_JS);
        doc.getHeader().addCSS( TABLE_CSS );
        String hMerged = request.getParameter(Params.HIDE_MERGED);
        if ( hMerged != null && hMerged.equals(HTMLNames.ON) )
            hideMerged = true;
        String wWords = request.getParameter(Params.WHOLE_WORDS);
        if ( wWords != null && wWords.equals(HTMLNames.ON) )
            wholeWords = true;
        String compacted = request.getParameter(Params.COMPACT);
        if ( compacted != null && compacted.equals(HTMLNames.ON) )
            compact = true;
        String len = request.getParameter(Params.LENGTH);
        if ( len != null )
            length = Integer.parseInt(len);
        String off = request.getParameter(Params.OFFSET);
        if ( off != null )
            offset = Integer.parseInt(off);
        String some = request.getParameter(Params.SOME_VERSIONS);
        someVersions = ( some != null && some.equals(HTMLNames.ON) );
        String selected = request.getParameter(Params.SELECTED_VERSIONS);
        if ( selected != null )
            selectedVersions = selected;
        super.handle( request, response, urn );
    }
}
