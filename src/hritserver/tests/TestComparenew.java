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
import hritserver.constants.ChunkState;
import hritserver.tests.html.Element;
import hritserver.tests.html.HTMLLiteral;
import hritserver.constants.Params;
import hritserver.constants.HTMLNames;
import hritserver.exception.HritException;
import hritserver.URLEncoder;
import hritserver.Utils;
import hritserver.tests.html.HTML;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 *
 * @author desmond
 */
public class TestComparenew extends Test
{
    String version2;
    static String LONG_NAME1 = "long_name1";
    static String LONG_NAME2 = "long_name2";
    static String css = 
    "form \n{\n\tmargin: 0;\n\tpadding: 0;\n\tborder: 0;\n\tcolor:"
    +" #0F0F0F;\n}\nform a:link, a:visited \n{\n\ttext-decoration:"
    +" underline;\n\tfont-weight: normal;\n\tcolor: #000;\n\toutli"
    +"ne: none;\n\ttext-align: left;\n}\ndiv#twinCentreColumn \n{ "
    +"\n\tborder: 0; \n\tposition: relative; \n\theight: 1000px; \n"
    +"\ttop: 0px; \n\tbackground-color: white; \n\twidth: 978px; "
    +"\n\tmargin-left: auto; \n\tmargin-right: auto; \n\ttext-alig"
    +"n: left \n}\ndiv#leftColumn \n{\n\tbackground-color: white; "
    +"\n\tfloat: left; \n\tleft: auto; \n\tmargin-right: 5px;\n\tp"
    +"adding: 10px;\n\twidth: 45%;\n\toverflow-y: auto;\n\toverflo"
    +"w-x: hidden;\n}\ndiv#leftColumn select\n{\n\ttext-align: cen"
    +"ter;\n}\ndiv#rightColumn select\n{\n\ttext-align: center;\n}"
    +"\ndiv#rightColumn \n{\n\tbackground-color: white; \n\tfloat:"
    +" right; \n\tright: auto; \n\tmargin-left: 5px;\n\tpadding: 1"
    +"0px;\n\twidth: 45%;\n\toverflow-y: auto;\n\toverflow-x: hidd"
    +"en;\n}\ndiv#top\n{\n\ttext-align: center;\n\tfont-family: se"
    +"rif; \n\tfont-weight: bold; \n\tfont-size:16px;\n}\ndiv#left"
    +"Wrapper\n{\n\ttext-align:center;\n\tmargin-left: auto;\n\tma"
    +"rgin-right: auto;\n\tfloat: left; \n\tleft: auto; \n\twidth:"
    +" 45%;\n}\ndiv#rightWrapper\n{\n\ttext-align: center;\n\tmarg"
    +"in-left: auto;\n\tmargin-right: auto;\n\tfloat: right; \n\tr"
    +"ight: auto; \n\twidth: 45%;\n}\nspan.description \n{ \n\ttex"
    +"t-align: center;\n\tfont-weight: normal;\n}\nspan.deleted \n"
    +"{ \n\tcolor: red \n}\nspan.selected\n{\n\tbackground-color: "
    +"LightSkyBlue\n}\nspan.transposed \n{ \n\tcolor: grey \n}\nsp"
    +"an.added \n{ \n\tcolor: blue \n}";
    static String js = 
    "var leftScrollPos,rightScrollPos;\nvar scrolledDiff;\nvar scr"
    +"olledSpan;\nfunction getOffsetTopByElem( elem )\n{\n\tvar of"
    +"fset = 0;\n\twhile ( elem != null )\n\t{\n\t\toffset += elem"
    +".offsetTop;\n\t\telem = elem.offsetParent;\n\t}\n\treturn of"
    +"fset;\n}\nfunction getElementHeight( elem )\n{\n\tif ( elem."
    +"height )\n\t\treturn elem.height;\n\telse\n\t\treturn elem.o"
    +"ffsetHeight;\n}\nfunction do_popup1()\n{\n\tvar desc = docum"
    +"ent.getElementById( \"long_name1\" );\n\tvar popup = documen"
    +"t.getElementById( \"version1\" );\n\tdesc.textContent = popu"
    +"p.options[popup.selectedIndex].title;\n}\nfunction do_popup2"
    +"()\n{\n\tvar desc = document.getElementById( \"long_name2\" "
    +");\n\tvar popup = document.getElementById( \"version2\" );\n"
    +"\tdesc.textContent = popup.options[popup.selectedIndex].titl"
    +"e;\n}\nfunction setDescriptors()\n{\n\tdo_popup1();\n\tdo_po"
    +"pup2();\n\tfitWithinParent(\"leftColumn\");\n\tfitWithinPare"
    +"nt(\"rightColumn\");\n\tfitWithinParent(\"twinCentreColumn\""
    +");\n}\nfunction getHeight( elem, inclBorder )\n{\n\tvar bord"
    +"erHeight = getBorderValue(elem,\"border-top-width\")\n\t\t\t"
    +"+getBorderValue(elem,\"border-bottom-width\");\n\tif ( elem."
    +"clientHeight )\n\t\treturn (inclBorder)?borderHeight+elem.cl"
    +"ientHeight\n\t\t\t:elem.clientHeight;\n\telse\n\t\treturn (i"
    +"nclBorder)?elem.offsetHeight\n\t\t\t:elem.offsetHeight-borde"
    +"rHeight;\n}\nfunction getOffsetTop( id )\n{\n\tvar elem = do"
    +"cument.getElementById(id);\n\treturn getOffsetTopForElem( el"
    +"em );\n}\nfunction getOffsetTopForElem( elem )\n{\n\tvar off"
    +"set = 0;\n\twhile ( elem != null )\n\t{\n\t\toffset += elem."
    +"offsetTop;\n\t\telem = elem.offsetParent;\n\t}\n\treturn off"
    +"set;\n}\nfunction cssToIE( prop )\n{\n\tvar parts = prop.spl"
    +"it(\"-\");\n\tif ( parts.length > 0 )\n\t{\n\t\tvar ccProp ="
    +" parts[0];\n\t\tfor ( var i=1;i<parts.length;i++ )\n\t\t{\n\t"
    +"\t\tif ( parts[i].length > 0 )\n\t\t\t{\n\t\t\t\tccProp += "
    +"parts[i].substr(0,1).toUpperCase()\n\t\t\t\t\t+parts[i].subs"
    +"tr(1,parts[i].length-1);\n\t\t\t}\n\t\t}\n\t\treturn ccProp;"
    +"\n\t}\n\telse\n\t\treturn prop;\n}\nfunction getStyleValue( "
    +"elem, prop )\n{\n\tvar value = getStyle( elem, prop );\n\tif"
    +" ( value )\n\t\treturn parseInt( value );\n\telse\n\t\tretur"
    +"n 0;\n}\nfunction getStyle( elem, prop )\n{\n\tif ( elem.cur"
    +"rentStyle )\n\t\tvar y = elem.currentStyle[cssToIE(prop)];\n"
    +"\telse if ( window.getComputedStyle )\n\t\tvar y = window.ge"
    +"tComputedStyle(elem,null)\n\t\t\t.getPropertyValue(prop);\n\t"
    +"return y;\n}\nfunction getBorderValue( elem, prop )\n{\n\tv"
    +"ar value = getStyleValue( elem, prop );\n\tvar number = 0;\n"
    +"\tif ( value )\n\t{\n\t\tnumber = parseInt( value );\n\t\tif"
    +" ( isNaN(number) )\n\t\t{\n\t\t\tif ( value == \"medium\" )\n"
    +"\t\t\t\tnumber = 3;\n\t\t\telse if ( value == \"thick\" )\n"
    +"\t\t\t\tnumber = 6;\n\t\t\telse if ( value == \"thin\" )\n\t"
    +"\t\t\tnumber = 1;\n\t\t}\n\t}\n\treturn number;\n}\nfunction"
    +" getWindowHeight()\n{\n\tvar myHeight = 0;\n\tif ( typeof( w"
    +"indow.innerWidth ) == 'number' )\n\t myHeight = window.inner"
    +"Height;\n\telse if ( document.documentElement\n\t\t&& ( docu"
    +"ment.documentElement.offsetHeight ) )\n\t\t//IE 6+ in 'stand"
    +"ards compliant mode'\n\t\tmyHeight = document.documentElemen"
    +"t.offsetHeight;\n\telse if ( document.body && document.body."
    +"offsetHeight )\n\t\tmyHeight = document.body.offsetHeight;\n"
    +"\treturn myHeight;\n}\nfunction fitWithinParent( id )\n{\n\t"
    +"var elem = document.getElementById( id );\n\tvar topOffset ="
    +" getOffsetTopForElem( elem );\n\tvar windowHeight = getWindo"
    +"wHeight();\n\t// compute the height, set it\n\tvar vPadding "
    +"= getStyleValue(elem,\"padding-top\")\n\t\t+getStyleValue(el"
    +"em,\"padding-bottom\");\n\tvar vBorder = getBorderValue(elem"
    +",\"border-top-width\")\n\t\t+getBorderValue(elem,\"border-bo"
    +"ttom-width\");\n\tvar tempHeight = windowHeight-(topOffset+v"
    +"Padding+vBorder);\n\telem.style.height = tempHeight+\"px\";\n"
    +"}\nfunction synchroScroll()\n{\n\t// 1. find the side that "
    +"has scrolled most recently\n\t// and the side that has proba"
    +"bly remained static\n\tvar leftDiv = document.getElementById"
    +"(\"leftColumn\");\n\tvar rightDiv = document.getElementById("
    +"\"rightColumn\");\n\tif ( leftDiv.scrollTop != leftScrollPos"
    +" )\n\t{\n\t\tleftScrollPos = leftDiv.scrollTop;\n\t\tscrolle"
    +"dDiv = leftDiv;\n\t\tstaticDiv = rightDiv;\n\t}\n\telse if ("
    +" rightScrollPos != rightDiv.scrollTop )\n\t{\n\t\trightScrol"
    +"lPos = rightDiv.scrollTop;\n\t\tscrolledDiv = rightDiv;\n\t\t"
    +"staticDiv = leftDiv;\n\t}\n\telse\t// nothing to do\n\t\tre"
    +"turn;\n\t// 2. find the most central span in the scrolled di"
    +"v\n\tscrolledDiff = 4294967296;\n\tscrolledSpan = null;\n\tv"
    +"ar scrolledDivTop = getOffsetTopByElem( scrolledDiv );\n\tva"
    +"r staticDivTop = getOffsetTopByElem( staticDiv );\n\tvar cen"
    +"tre = getElementHeight(scrolledDiv)/2\n\t\t+scrolledDiv.scro"
    +"llTop;\n\tfindSpanAtOffset( scrolledDiv, centre, scrolledDiv"
    +"Top );\n\t// 3. find the corresponding span on the other sid"
    +"e\n\tif ( scrolledSpan != null )\n\t{\n\t\tvar staticId = sc"
    +"rolledSpan.getAttribute(\"id\");\n\t\tif ( staticId.charAt(0"
    +")=='a' )\n\t\t\tstaticId = \"d\"+staticId.substring(1);\n\t\t"
    +"else\n\t\t\tstaticId = \"a\"+staticId.substring(1);\n\t\tva"
    +"r staticSpan = document.getElementById( staticId );\n\t\tif "
    +"( staticSpan != null )\n\t\t{\n\t\t\t// 4. compute relative "
    +"topOffset of scrolledSpan\n\t\t\tvar scrolledTopOffset = scr"
    +"olledSpan.offsetTop\n\t\t\t\t-scrolledDivTop;\n\t\t\t// 5. c"
    +"ompute relative topOffset of staticSpan\n\t\t\tvar staticTop"
    +"Offset = staticSpan.offsetTop-staticDivTop;\n\t\t\t// 6. scr"
    +"oll the static div level with scrolledSpan\n\t\t\tvar top = "
    +"staticTopOffset-getElementHeight(staticDiv)/2;\n\t\t\tif ( t"
    +"op < 0 )\n\t\t\t\tstaticDiv.scrollTop = 0;\n\t\t\telse\n\t\t"
    +"\t\tstaticDiv.scrollTop = top;\n\t\t}\n\t}\n}\nfunction find"
    +"SpanAtOffset( elem, pos, divOffset )\n{\n\tif ( elem.nodeNam"
    +"e == \"SPAN\"\n\t\t&& elem.getAttribute('id') != null )\n\t{"
    +"\n\t\tvar idAttr = elem.getAttribute('id');\n\t\tvar spanRel"
    +"Offset = elem.offsetTop-divOffset;\n\t\tif ( Math.abs(spanRe"
    +"lOffset-pos) < scrolledDiff )\n\t\t{\n\t\t\tscrolledSpan = e"
    +"lem;\n\t\t\tscrolledDiff = Math.abs(spanRelOffset-pos);\n\t\t"
    +"}\n\t}\n\telse if ( elem.firstChild != null )\n\t\tfindSpan"
    +"AtOffset( elem.firstChild, pos, divOffset );\n\tif ( elem.ne"
    +"xtSibling != null )\n\t\tfindSpanAtOffset( elem.nextSibling,"
    +" pos, divOffset );\n}\nsetInterval(\"synchroScroll()\",500);"
    +"\nwindow.onload = setDescriptors;";
    public TestComparenew()
    {
        description = "Compare function displays two versions of an MVD side by side";
        if ( doc == null )
            doc = new HTML();
        doc.getHeader().addCSS( css );
        doc.getHeader().addScript( js );
    }
    /**
     * Display the test GUI
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request,
        HttpServletResponse response, String urn ) throws HritException
    {
        version2 = request.getParameter( Params.VERSION2 );
        super.handle( request, response, urn );
    }
    /**
     * Extract the MVD description string from the list HTML
     * @param list the HTML of the list
     * @return a string being the MVD's description
     */
    String getMVDDescription( String list )
    {
        String mvdDesc = "";
        int index = list.indexOf("</span>");
        if ( index != -1 )
        {
            int index2 = list.indexOf(">");
            if ( index2 != -1 )
                mvdDesc = list.substring(index2+1,index);
        }
        return mvdDesc;
    }
    /**
     * Get a list menu
     * @param version1 the version to use as the default
     * @param name the name and ID of the list
     * @param onchange a javascript function to call on list change
     * @param longNameId ID of long name
     * @return a HTML representation of the list as a String
     * @throws Exception 
     */
    String getList( String version1, String name, String onchange, 
        String longNameId ) throws Exception
    {
        String url = "http://localhost:8080/html/list";
        url = URLEncoder.append( url, docID );
        url = URLEncoder.addGetParam( url, Params.NAME, name );
        url = URLEncoder.addGetParam( url, Params.VERSION1, 
            Utils.escape(version1) );
        url = URLEncoder.addGetParam( url, Params.FUNCTION, onchange );
        url = URLEncoder.addGetParam( url, Params.STYLE, "/list/twin-list" );
        url = URLEncoder.addGetParam( url, Params.LONG_NAME_ID, longNameId );
        return URLEncoder.getResponseForUrl( url );
    }
    /**
     * Get the next version based on version1
     * @return the full path to the version
     */
    String getNextVersion() throws Exception
    {
        String url = "http://localhost:8080/cortex/version2";
        url = URLEncoder.append( url, docID );
        url = URLEncoder.addGetParam( url, Params.VERSION1, 
            Utils.escape(version1) );
        return URLEncoder.getResponseForUrl(url).trim();
    }
    /**
     * Get a text version
     * @param v1 the first version
     * @param v2 the second version
     * @param diffKind ChunkState.DELETED or ChunkState.ADDED
     * @return the HTML of the version rendered using the default CorCode
     */ 
    String getTextVersion( String v1, String v2, String diffKind ) throws Exception
    {
        String url = "http://localhost:8080/html/comparison";
        String urn = Utils.escape( docID );
        url = URLEncoder.append( url, urn );
        url = URLEncoder.addGetParam(url,Params.VERSION1, Utils.escape(v1));
        url = URLEncoder.addGetParam(url,Params.VERSION2, Utils.escape(v2));
        url = URLEncoder.addGetParam(url, Params.DIFF_KIND, diffKind );
        return URLEncoder.getResponseForUrl(url).trim();
    }
    /**
     * Get the content of this tab (just default content for now)
     * @return an element
     */
    @Override
    public Element getContent()
    {
        try
        {
            Element form = new Element( HTMLNames.FORM );
            form.addAttribute( HTMLNames.NAME, HTMLNames.DEFAULT );
            form.addAttribute( HTMLNames.ID, HTMLNames.DEFAULT );
            form.addAttribute( HTMLNames.METHOD, HTMLNames.POST );
            form.addAttribute( HTMLNames.ACTION, "/tests/comparenew" );
            Element input = new Element(HTMLNames.INPUT);
            input.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN);
            input.addAttribute( HTMLNames.NAME, Params.DOC_ID );
            input.addAttribute( HTMLNames.ID, Params.DOC_ID );
            input.addAttribute( HTMLNames.VALUE, docID );
            form.addChild( input );
            Element divCentre = new Element( HTMLNames.DIV );
            divCentre.addAttribute( HTMLNames.ID, "twinCentreColumn" );
            Element divLeft = new Element( HTMLNames.DIV );
            Element divRight = new Element( HTMLNames.DIV );
            divLeft.addAttribute( HTMLNames.ID, "leftColumn" );
            divRight.addAttribute( HTMLNames.ID, "rightColumn" );
            String list1 = getList(version1, Params.VERSION1, 
                "document.forms.default.submit();", LONG_NAME1 );
            if ( version2 == null || version1.equals(version2) )
                version2 = getNextVersion();
            String list2 = getList( version2, Params.VERSION2, 
                "document.forms.default.submit();", 
                LONG_NAME2 );
            // top div contains title and two drop-downs
            Element divTop = new Element( HTMLNames.DIV );
            divTop.addAttribute( HTMLNames.ID, "top" );
            divTop.addText( getMVDDescription(list1) );
            // add a row containing the two dropdowns
            Element row = new Element(HTMLNames.DIV);
            Element leftWrapper = new Element(HTMLNames.DIV);
            leftWrapper.addChild( new HTMLLiteral(list1) );
            leftWrapper.addAttribute( HTMLNames.ID, "leftWrapper" );
            row.addChild( leftWrapper );
            Element rightWrapper = new Element(HTMLNames.DIV);
            rightWrapper.addChild( new HTMLLiteral(list2) );
            rightWrapper.addAttribute( HTMLNames.ID, "rightWrapper" );
            row.addChild( rightWrapper );
            divTop.addChild( row );
            // get content for left hand side
            String body = getTextVersion(version1,version2,ChunkState.DELETED);
            addCSSFromBody( body );
            Element lhs = new HTMLLiteral( body );
            divLeft.addChild( lhs );
            // get content for rhs
            body = getTextVersion(version2,version1,ChunkState.ADDED);
            System.out.println(body);
            Element rhs = new HTMLLiteral( body );
            divRight.addChild( rhs );
            // now assemble all the parts
            divCentre.addChild( divTop );
            divCentre.addChild( divLeft );
            divCentre.addChild( divRight );
            form.addChild( divCentre );
            return form;
        }
        catch ( Exception e )
        {
            Element p = new Element( HTMLNames.P );
            p.addText("Error: "+e.getMessage() );
            return p;
        }
    }
}
