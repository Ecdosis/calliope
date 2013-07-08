/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.tests;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import calliope.exception.*;
import calliope.tests.html.Element;
import calliope.tests.html.HTMLLiteral;
import calliope.tests.html.Text;
import calliope.tests.html.HTML;
import calliope.constants.*;
import calliope.Utils;
import calliope.URLEncoder;
import calliope.tests.html.HTMLOptGroup;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
/**
 * View of a single version, with horizontally scrolling table apparatus
 * @author desmond
 */
public class TestEdition extends Test
{
    boolean hideMerged = false;
    boolean wholeWords = false;
    boolean compact = false;
    boolean someVersions = false;
    String colour;
    String selectedVersions;
    int length = 100;
    static String JQUERY_URL = "http://"+Globals.JQUERY_SITE+"/jquery-latest.js";
    static String VERSION_POPUP_SCRIPT = 
    "function do_popup1(){\n\tdocument.forms.default.submit();\n}";
    static String EDITION_CSS = 
    "div#table { margin-left:2em; overflow-x: auto }\ndiv#innertab"
    +"le { width:2000px }\ndiv#buttons { padding:2px }\ndiv#prefs "
    +"{ width: 450px; display: none }\ndiv#text { border-bottom: i"
    +"nset 3px grey; overflow: auto; height: 500px }\ndiv#centre {"
    +" margin-left: auto; margin-right: auto; width: 450px; height"
    +":700px; border:0px; position: relative }";
    static String TABLE_JS = 
    "function findSimpleSibling( obj )\n{\n\tvar sibling = obj.pre"
    +"v(\"td\");\n\twhile ( sibling.length!= 0 )\n\t{\n\t\tvar chi"
    +"ldren = sibling.children();\n\t\tif ( !isSpace(sibling.text("
    +")) )\n\t\t{\n\t\t\tif ( (children.length==0 && sibling.conte"
    +"nts().length>0)\n\t\t\t|| (children.length>0&&children.first"
    +"().is(\"span\")) )\n\t\t\t{\n\t\t\t\tbreak;\n\t\t\t}\n\t\t}\n"
    +"\t\tsibling = sibling.prev(\"td\");\n\t}\n\treturn sibling;"
    +"\n}\nfunction isSpace( text )\n{\n\tfor ( var i=0;i<text.len"
    +"gth;i++ )\n\t\tif ( text.charCodeAt(i)>32 )\n\t\t\treturn fa"
    +"lse;\n\treturn true;\n}\nfunction findBestTextChild( obj )\n"
    +"{\n\tvar best = undefined;\n\tobj.contents().each(function()"
    +"\n\t{\n\t\tif (this.nodeType==1)\n\t\t{\n\t\t\tif ( !$(this)"
    +".is(\"table\") )\n\t\t\t{\n\t\t\t\tvar temp = findBestTextCh"
    +"ild($(this));\n\t\t\t\tif ( temp != undefined \n\t\t\t\t&& ("
    +"best==undefined||(temp.text().length>best.text().length)) )\n"
    +"\t\t\t\t{\n\t\t\t\t\tif ( !isSpace(temp.text()) )\n\t\t\t\t"
    +"\t\tbest = temp;\n\t\t\t\t}\n\t\t\t}\n\t\t}\n\t\telse if ( t"
    +"his.nodeType==3 )\n\t\t{\n\t\t\tif ( best==undefined||$(this"
    +").text().length>best.text().length )\n\t\t\t\tif ( !isSpace("
    +"$(this).text()) )\n\t\t\t\t{\n\t\t\t\t\tbest = $(this);\n\t\t"
    +"\t\t}\n\t\t}\n\t});\n\treturn best;\n}\nfunction wrapWithSp"
    +"an( text )\n{\n\tvar origParent = text.parent().first();\n\t"
    +"if ( !origParent.is(\"span\") )\n\t{\n\t\torigParent.html(\""
    +"<span>\"+text.text()+\"</span>\");\n\t\torigParent = origPar"
    +"ent.children().first();\n\t}\n\treturn origParent;\n}\nfunct"
    +"ion alignTables()\n{\n\t$(\"table[class='inline']\").each(fu"
    +"nction(i)\n\t{\n\t\tif ( $(this).parents(\"table[class='inli"
    +"ne']\").length==0 )\n\t\t{\n\t\t\tvar toggleID = $(this).att"
    +"r(\"id\");\n\t\t\tif ( toggleID!=\"yzu22w3cjkx8vv41r8952j3a9"
    +"hkc5r27\")\n\t\t\t{\n\t\t\t\tif ( toggleID != undefined )\n\t"
    +"\t\t\t\t$(this).click({toggleid:toggleID},toggle);\n\t\t\t\t"
    +"var tr = $(\"#\"+toggleID+\" tr[class1!='hidden']\").last()"
    +";\n\t\t\t\tvar parent = $(this).parents(\"td\").first();\n\t"
    +"\t\t\tvar parentTd = findSimpleSibling( parent );\n\t\t\t\tv"
    +"ar childTd = undefined;\n\t\t\t\tif ( tr.length==1 )\n\t\t\t"
    +"\t\tchildTd = tr.children(\"td[class!='siglumhidden']\").fir"
    +"st();\n\t\t\t\t// childTd is the first real td child of the "
    +"last tr in the inline table\n\t\t\t\t// parentTd is the td o"
    +"f the immediate parent of this inline table\n\t\t\t\t// we m"
    +"ust vertically align their baselines\n\t\t\t\tif ( parentTd "
    +"!= undefined && childTd != undefined && childTd.length==1 &&"
    +" parentTd.length==1 )\n\t\t\t\t{\n\t\t\t\t\tvar bestTextChil"
    +"d = findBestTextChild(childTd);\n\t\t\t\t\tvar bestTextParen"
    +"t = findBestTextChild(parentTd);\n\t\t\t\t\tif ( bestTextChi"
    +"ld!=undefined&&bestTextParent!=undefined)\n\t\t\t\t\t{\n\t\t"
    +"\t\t\t\tvar childSpan = wrapWithSpan(bestTextChild);\n\t\t\t"
    +"\t\t\tvar parentSpan = wrapWithSpan(bestTextParent);\n\t\t\t"
    +"\t\t\tvar childTopOffset = childSpan.offset().top;\n\t\t\t\t"
    +"\t\tvar parentTopOffset = parentSpan.offset().top;\n\t\t\t\t"
    +"\t\tvar oldTop = $(this).offset().top;\n\t\t\t\t\t\tvar oldL"
    +"eft = $(this).offset().left;\n\t\t\t\t\t\tvar newValue = old"
    +"Top+(parentTopOffset-childTopOffset);\n\t\t\t\t\t\t$(this).o"
    +"ffset({'top' : newValue, 'left': oldLeft});\n\t\t\t\t\t\tpar"
    +"ent.children(\"span\").each(function(i)\n\t\t\t\t\t\t{\n\t\t"
    +"\t\t\t\t\tvar thisTop = $(this).offset().top;\n\t\t\t\t\t\t\t"
    +"var thisLeft = $(this).offset().left;\n\t\t\t\t\t\t\tnewVal"
    +"ue = thisTop+(parentTopOffset-thisTop);\n\t\t\t\t\t\t\t$(thi"
    +"s).offset({'top' : newValue, 'left': thisLeft});\n\t\t\t\t\t"
    +"\t});\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t}\n\t\t}\n\t});\n}\nfun"
    +"ction findLastTr( table )\n{\n\tvar children = table.childre"
    +"n();\n\tif ( children.is(\"tbody\") )\n\t\tchildren = childr"
    +"en.children();\n\treturn children.last();\n}\nfunction swapC"
    +"hildClass( obj, name1, name2 )\n{\n\tobj.each(function(i)\n\t"
    +"{\n\t\tvar className = $(this).attr(\"class\");\n\t\tif ( c"
    +"lassName != null )\n\t\t{\n\t\t\tif ( className == name1 )\n"
    +"\t\t\t\tclassName = name2;\n\t\t\telse if ( className == nam"
    +"e2 )\n\t\t\t\tclassName = name1;\n\t\t\t$(this).attr( \"clas"
    +"s\", className );\n\t\t}\n\t});\n}\nfunction toggle( id )\n{"
    +"\n\t$(\"#\"+id.data.toggleid).children().each(function(i)\n\t"
    +"{\n\t\tif ( $(this).is(\"tbody\") )\n\t\t{\n\t\t\t$(this).c"
    +"hildren().each(function(i)\n\t\t\t{\n\t\t\t\tvar row = $(thi"
    +"s);\n\t\t\t\tif ( row.attr(\"class\")!= null )\n\t\t\t\t{\n\t"
    +"\t\t\t\tif ( row.attr(\"class\") == \"hidden\" )\n\t\t\t\t\t"
    +"{\n\t\t\t\t\t\trow.attr(\"class\",\"shown\");\n\t\t\t\t\t\t"
    +"from = \"siglumhidden\";\n\t\t\t\t\t\tto = \"siglum\";\n\t\t"
    +"\t\t\t}\n\t\t\t\t\telse if ( row.attr(\"class\") == \"shown\""
    +" )\n\t\t\t\t\t{\n\t\t\t\t\t\trow.attr(\"class\",\"hidden\")"
    +";\n\t\t\t\t\t\tfrom = \"siglum\";\n\t\t\t\t\t\tto = \"siglum"
    +"hidden\";\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t\tswapChildClass(ro"
    +"w.children(\"td.\"+from),from,to);\n\t\t\t});\n\t\t}\n\t});\n"
    +"}\nfunction checkmark( select )\n{\n\tvar pos = select.opti"
    +"ons[select.selectedIndex].text.lastIndexOf(\" ×\");\n\tif ( "
    +"pos != -1 )\n\t{\n\t\tvar len = select.options[select.select"
    +"edIndex].text.length;\n\t\tvar copy = select.options[select."
    +"selectedIndex].text;\n\t\tselect.options[select.selectedInde"
    +"x].text = copy.substring(0,pos);\n\t}\n\telse\n\t{\n\t\tsele"
    +"ct.options[select.selectedIndex].text += \" ×\";\n\t}\n}\nfu"
    +"nction presubmit()\n{\n\tvar value = \"\";\n\t$(\"#selector "
    +"option\").each(function()\n\t{\n\t\tif ( $(this).text().last"
    +"IndexOf(\" ×\")!=-1 )\n\t\t{\n\t\t\tif ( value.length>0 )\n\t"
    +"\t\t\tvalue+=\",\";\n\t\t\tvalue += $(this).val();\n\t\t}\n"
    +"\t});\n\tif ( value.length==0 )\n\t\tvalue = \"all\";\n\t$("
    +"\"#SELECTED_VERSIONS\").val(value);\n}\nvar centralDiff;\nva"
    +"r centralSpan;\nvar oldCentralSpan;\nvar midDiff;\nvar midTd"
    +";\nvar oldMidTd;\nvar vScrolling;\nvar hScrolling;\nfunction"
    +" baseid( id )\n{\n\tfor ( var i=1;i<id.length;i++ )\n\t{\n\t"
    +"\tif ( id.charAt(i)>'9'||id.charAt(i)<'0' )\n\t\t\treturn id"
    +".substr(1,i-1);\n\t}\n\treturn id.substr(1);\n}\nfunction fi"
    +"ndSpanAtOffset( obj, pos )\n{\n\tif ( obj.is(\"span\") && ob"
    +"j.attr('id') != undefined )\n\t{\n\t\tvar spanRelOffset = ob"
    +"j.offset().top-$(\"#content\").offset().top;\n\t\tif ( Math."
    +"abs(spanRelOffset-pos) < centralDiff )\n\t\t{\n\t\t\tcentral"
    +"Span = obj;\n\t\t\tcentralDiff = Math.abs(spanRelOffset-pos)"
    +";\n\t\t}\n\t}\n\telse if ( obj.children().length>0 )\n\t{\n\t"
    +"\tfindSpanAtOffset( obj.children().first(), pos);\n\t}\n\ti"
    +"f ( obj.next().length>0 )\n\t{\n\t\tfindSpanAtOffset( obj.ne"
    +"xt(), pos );\n\t}\n}\nfunction isText( str )\n{\n\tfor ( var"
    +" i=0;i<str.length;i++ )\n\t\tif ( str.charAt(i)<'a'||str.cha"
    +"rAt(i)>'z' )\n\t\t\treturn false;\n\treturn true;\n}\nfuncti"
    +"on backgroundify( id, colour, prefix, elem )\n{\n\tif ( id.l"
    +"ength > 0 )\n\t{\n\t\tvar baseId = prefix+id;\n\t\t$(elem+\""
    +"[id^='\"+baseId+\"']\").each(function(i)\n\t\t{\n\t\t\tvar p"
    +"artId = prefix+baseid($(this).attr(\"id\"));\n\t\t\tif ( par"
    +"tId.length == baseId.length )\n\t\t\t\t$(this).css(\"backgro"
    +"und-color\",colour);\n\t\t});\n\t}\n}\nfunction calcMidVPoin"
    +"t( obj, maxVScroll )\n{\n\tvar scrollPos = obj.scrollTop();\n"
    +"\treturn scrollPos+((obj.height()*scrollPos)/maxVScroll);\n"
    +"}\nfunction setCol1Width()\n{\n\tvar max = 0;\n\t$(\"td[clas"
    +"s='siglumleft']\").each(function()\n\t{\n\t\tif ( $(this).wi"
    +"dth()>max )\n\t\t\tmax = $(this).width();\n\t});\n\tmax += 8"
    +";\n\t$(\"#table\").css(\"margin-left\",max+\"px\");\n}\nfunc"
    +"tion setDivHeights()\n{\n\t// set total height of centre div"
    +"\n\tvar botmargin = ($(\"#centre\").parent().outerHeight(tru"
    +"e)-$(\"#centre\").parent().outerHeight())/2;\n\tvar cheight "
    +"= $(window).height()-($(\"#centre\").offset().top+botmargin)"
    +";\n\t$(\"#centre\").css(\"height\",cheight+\"px\");\n\t// se"
    +"t real width of apparatus\n\t$(\"#innertable\").css(\"width\""
    +",$(\"#apparatus\").width()+\"px\");\n\tvar rest = $(\"#tabl"
    +"e\").outerHeight(true)+$(\"#buttons\").outerHeight(true)+$(\""
    +"#versions\").outerHeight(true);\n\tvar theight = cheight-re"
    +"st;\n\t$(\"#text\").css(\"height\",theight+\"px\");\n}\nfunc"
    +"tion addButtonHandlers()\n{\n\t// add button event handlers\n"
    +"\t$(\"button[name='prefs']\").click(function() \n\t{\n\t\tv"
    +"ar bheight = $(\"#buttons\").outerHeight(true);\n\t\tvar vhe"
    +"ight = $(\"#versions\").outerHeight(true);\n\t\tvar cheight="
    +"$(\"#centre\").height();\t\n\t\tif ( $(\"#table\").is(\":vis"
    +"ible\") )\n\t\t{\n\t\t\t$(\"#table\").hide();\n\t\t}\n\t\t$("
    +"\"#prefs\").animate(\n\t\t\t{height: 'show'},\n\t\t\t{\n\t\t"
    +" duration: \"slow\",\n\t\t\t step: function(now, fx) \n\t\t\t"
    +" {\n\t\t\t\t var th = cheight-(now+bheight+vheight);\n\t\t\t"
    +"\t $(\"#text\").css(\"height\",th+\"px\");\n\t\t\t }\n\t\t}"
    +");\n\t\treturn false;\n\t});\n\t$(\"button[name='table']\")."
    +"click(function() \n\t{\n\t\tvar bheight = $(\"#buttons\").ou"
    +"terHeight(true);\n\t\tvar vheight = $(\"#versions\").outerHe"
    +"ight(true);\n\t\tvar cheight=$(\"#centre\").height();\t\n\t\t"
    +"if ( $(\"#prefs\").is(\":visible\") )\n\t\t{\n\t\t\t$(\"#pr"
    +"efs\").hide();\n\t\t}\n\t\t$(\"#table\").animate(\n\t\t\t{he"
    +"ight: 'show'},\n\t\t\t{\n\t\t duration: \"slow\",\n\t\t\t co"
    +"mplete: function()\n\t\t\t {\n\t\t\t\tvar th = cheight-($(\""
    +"#table\").outerHeight(true)+bheight+vheight);\n\t\t\t\t$(\"#"
    +"text\").css(\"height\",th+\"px\");\n\t\t\t },\n\t\t\t step: "
    +"function(now, fx) \n\t\t\t {\n\t\t\t\t var th = cheight-(now"
    +"+bheight);\n\t\t\t\t $(\"#text\").css(\"height\",th+\"px\");"
    +"\n\t\t\t }\n\t\t});\n\t\treturn false;\n\t});\n\t$(\"button["
    +"name='none']\").click(function() \n\t{\n\t\tvar bheight = $("
    +"\"#buttons\").outerHeight(true);\n\t\tvar cheight=$(\"#centr"
    +"e\").height();\t\n\t\tvar vheight = $(\"#versions\").outerHe"
    +"ight(true);\n\t\tif ( $(\"#prefs\").is(\":visible\") )\n\t\t"
    +"{\n\t\t\t$(\"#prefs\").hide();\n\t\t}\n\t\tif ( $(\"#table\""
    +").is(\":visible\") )\n\t\t{\n\t\t\t$(\"#table\").hide();\n\t"
    +"\t}\n\t\tvar th = cheight-(bheight+vheight);\n\t\t$(\"#text\""
    +").css(\"height\",th+\"px\");\n\t\treturn false;\n\t});\n\t/"
    +"/ handler for colour chooser\n\t$(\"select[name='colour']\")"
    +".change(function()\n\t{\n\t\tvar colour = $(\"select#colour\""
    +").val();\n\t\t$(\"#target\").css(\"background-color\",colou"
    +"r);\n\t\tif ( centralSpan != null )\n\t\t{\n\t\t\tvar cid = "
    +"baseid(centralSpan.attr(\"id\"));\n\t\t\tbackgroundify( cid,"
    +" colour, \"v\", \"span\" );\n\t\t\tbackgroundify( cid, colou"
    +"r, \"t\", \"td\" );\n\t\t}\n\t\treturn false;\n\t});\n\t// s"
    +"ome versions checkbox (controlling disablment of selctor)\n\t"
    +"$(\"input[name='SOME_VERSIONS']\").click(function()\n\t{\n\t"
    +"\tif ( $(this).val()==\"on\" )\n\t\t{\n\t\t\tif ( $(\"#sele"
    +"ctor\").attr(\"disabled\") != null )\n\t\t\t\t$(\"#selector\""
    +").removeAttr(\"disabled\");\n\t\t}\n\t\telse\n\t\t\t$(\"#se"
    +"lector\").attr(\"disabled\",\"disabled\");\n\t});\n}\n// ini"
    +"t function\n$(function() \n{\n\tvar startTime = new Date().g"
    +"etTime();\n\tsetCol1Width();\n\taddButtonHandlers();\n\talig"
    +"nTables();\n\tsetDivHeights();\n\tvar midpoint = calcMidVPoi"
    +"nt($(\"#text\"),$(\"#content\").height()-$(\"#text\").height"
    +"());\n\tcentralDiff = 4294967296;\n\tcentralSpan = null;\n\t"
    +"vScrolling = hScrolling = false;\n\tfindSpanAtOffset( $(\"#t"
    +"ext\"), midpoint );\n\tif ( centralSpan != null )\n\t{\n\t\t"
    +"var colour = $(\"select#colour\").val();\n\t\tvar cid = base"
    +"id(centralSpan.attr(\"id\"));\n\t\tbackgroundify( cid, colou"
    +"r, \"v\", \"span\" );\n\t\tbackgroundify( cid, colour, \"t\""
    +", \"td\" );\n\t}\n\t// vscroll function\n\t$(\"#text\").scro"
    +"ll(function () \n\t{ \n\t\tcentralDiff = 4294967296;\n\t\tol"
    +"dCentralSpan = centralSpan;\n\t\tcentralSpan = null;\n\t\tva"
    +"r maxVScroll = $(\"#content\").height()-$(this).height();\n\t"
    +"\tvar maxHScroll = $(\"#innertable\").width()-$(\"#table\")"
    +".width();\n\t\tfindSpanAtOffset( $(\"#text\"), calcMidVPoint"
    +"($(this),maxVScroll) );\n\t\tif ( $(this).scrollTop()==0 )\n"
    +"\t\t{\n\t\t\t$(\"#table\").scrollLeft(0);\n\t\t}\n\t\telse i"
    +"f ( $(this).scrollTop()>=maxVScroll )\n\t\t{\n\t\t\t$(\"#tab"
    +"le\").scrollLeft(maxHScroll);\n\t\t}\n\t\telse\n\t\t{\n\t\t\t"
    +"var tid = \"t\"+baseid(centralSpan.attr(\"id\") );\n\t\t\tv"
    +"ar left = -1;\n\t\t\tvar right = -1;\n\t\t\t$(\"td[id^='\"+t"
    +"id+\"']\").each(function(i)\n\t\t\t{\n\t\t\t\tvar actualTid "
    +"= $(this).attr(\"id\");\n\t\t\t\tif ( actualTid.length==tid."
    +"length\n\t\t\t\t\t||isText(actualTid.substr(tid.length)) ) \n"
    +"\t\t\t\t{\n\t\t\t\t\tvar lpos = $(this).offset().left-$(\"#"
    +"innertable\").offset().left;\n\t\t\t\t\tright = lpos+$(this)"
    +".width();\n\t\t\t\t\tif ( left == -1 )\n\t\t\t\t\t\tleft = l"
    +"pos;\n\t\t\t\t}\n\t\t\t\t// else it's an invalid prefix\n\t\t"
    +"\t});\n\t\t\tif ( left != -1 && right != -1 )\n\t\t\t{\n\t\t"
    +"\t\tvar pos = ((right+left)/2)-($(\"#table\").width()/2);\n"
    +"\t\t\t\t$(\"#table\").scrollLeft(pos);\n\t\t\t}\n\t\t}\n\t\t"
    +"if ( oldCentralSpan != null )\n\t\t{\n\t\t\tvar oid = baseid"
    +"(oldCentralSpan.attr(\"id\"));\n\t\t\tbackgroundify( oid, \""
    +"\", \"v\", \"span\" );\n\t\t\tbackgroundify( oid, \"\", \"t\""
    +", \"td\" );\n\t\t}\n\t\tif ( centralSpan != null )\n\t\t{\n"
    +"\t\t\tvar colour = $(\"select#colour\").val();\n\t\t\tvar ci"
    +"d = baseid(centralSpan.attr(\"id\"));\n\t\t\tbackgroundify( "
    +"cid, colour, \"v\", \"span\" );\n\t\t\tbackgroundify( cid, c"
    +"olour, \"t\", \"td\" );\n\t\t}\n\t});\n\tvar end = new Date("
    +").getTime();\n\tvar time = end - startTime;\n});";
    static String TABLE_CSS="table/default";
    public TestEdition()
    {
        description = "Single version view with table apparatus";
    }
    /**
     * Display the test GUI
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request,
        HttpServletResponse response, String urn ) throws AeseException
    {
        setDocID( request );
        doc = new HTML();
        doc.getHeader().addCSS( EDITION_CSS );
        doc.getHeader().addCSS( getCorForm(TABLE_CSS) );
        doc.getHeader().addScriptSrc( JQUERY_URL );
        doc.getHeader().addScript( VERSION_POPUP_SCRIPT );
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
        String some = request.getParameter(Params.SOME_VERSIONS);
        someVersions = ( some != null && some.equals(HTMLNames.ON) );
        String selected = request.getParameter(Params.SELECTED_VERSIONS);
        if ( selected != null )
            selectedVersions = selected;
        else
            selectedVersions = "all";
        colour = request.getParameter(Params.COLOUR);
        if ( colour == null )
            colour = "pink";
        super.handle( request, response, urn );
    }
    /**
     * Get the content of the version
     * @return an Element (div) containing the content
     */
    private Element getVersionContent()
    {
        try
        {
            String url = "http://localhost:8080/html";
            String urn = Utils.escape( docID );
            url = URLEncoder.append( url, urn );
            url = URLEncoder.addGetParam(url,Params.VERSION1,
                Utils.escape(version1));
            url = URLEncoder.addGetParam(url,Params.SELECTED_VERSIONS,
                selectedVersions);
            String body = URLEncoder.getResponseForUrl(url).trim();
            body = extractCSSFromBody( body );
            length = getLengthFromBody( body, body.length() );
            // it's always safe to return the body as is
            return new HTMLLiteral( body );
        }
        catch ( Exception e )
        {
            return new Text( "Failed CorCode+CorTex to HTML test: "
                +e.getMessage());
        }
    }
    private String getVersions() throws Exception
    {
        String rawURL = "http://localhost:8080/list";
        String urn = Utils.escape( docID );
        rawURL = URLEncoder.append( rawURL, urn );
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
     * Get the version dropdown menu in its own div
     * @return an Element containing the dropdown menu
     */
    private Element getVersionSelector()
    {
        try
        {
            TestGetURL rawUrl = new TestGetURL(
                "http://localhost:8080/html/list");
            String urn = Utils.escape( docID );
            rawUrl.append( urn );
            rawUrl.addParam( Params.NAME, Params.VERSION1 );
            rawUrl.addParam( Params.VERSION1, version1 );
            rawUrl.addParam( Params.FUNCTION, "do_popup1()" );
            String html = URLEncoder.getResponseForUrl( rawUrl.toString() );
            return new HTMLLiteral( html );
        }
        catch ( Exception e )
        {
            return new Text( "Failed List (HTML) test: "
                +e.getMessage());
        }
    }
    /**
     * Add a checkbox to the prefs dialog. Restore saved settings
     * @param form the form element to contain the checkbox
     * @param flag the saved boolean value of the checkbox
     * @param prompt the prompt describing it
     * @param paramName the name of the submitted parameter
     * @return the checkbox element
     */
    private Element addCheckBox( Element form, boolean flag, String prompt, 
        String paramName )
    {
        Element check = new Element(HTMLNames.INPUT);
        check.addAttribute(HTMLNames.TYPE,HTMLNames.CHECKBOX);
        check.addAttribute( HTMLNames.NAME, paramName );
        if ( flag )
            check.addAttribute( HTMLNames.CHECKED, null );
        Element span = new Element(HTMLNames.SPAN);
        span.addText( prompt );
        form.addChild( span );
        form.addChild( check );
        return check;
    }
    /**
     * Add the specified option to the selector
     * @param select the selector element
     * @param value the new value
     * @param displayValue the value to display
     * @param selectit mark the option as "selected"
     */
    private void addOption( Element select, String value, 
        String displayValue, boolean selectit )
    {
        Element option = new Element( HTMLNames.OPTION );
        option.addText( displayValue );
        option.addAttribute( HTMLNames.VALUE, value );
        if ( selectit )
            option.addAttribute( HTMLNames.SELECTED, HTMLNames.SELECTED );
        select.addChild( option );
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
        hidden.addAttribute(HTMLNames.ID, Params.SELECTED_VERSIONS );
        hidden.addAttribute(HTMLNames.NAME, Params.SELECTED_VERSIONS );
        hidden.addAttribute(HTMLNames.VALUE,selectedVersions);
        parent.addChild( hidden );
    }
    /**
     * Add a dropdown menu
     * @param parent 
     */
    void addHighlightDropdown( Element parent )
    {
        Element dropdown = new Element( HTMLNames.SELECT );
        dropdown.addAttribute( HTMLNames.ID, "colour" );
        dropdown.addAttribute( HTMLNames.NAME, "colour" );
        addOption( dropdown, "pink", "pink", colour.equals("pink") );
        addOption( dropdown, "powderblue", "powder blue", 
            colour.equals("powderblue") );
        addOption( dropdown, "palegreen", "pale green", 
            colour.equals("palegreen") );
        addOption( dropdown, "yellow", "yellow", colour.equals("yellow") );
        Element prompt = new Element(HTMLNames.SPAN);
        prompt.addText("highlight:&nbsp;");
        parent.addChild( prompt );
        parent.addChild( dropdown );
        Element target = new Element(HTMLNames.SPAN);
        target.addAttribute(HTMLNames.ID,"target");
        target.addAttribute(HTMLNames.STYLE,"background-color:"+colour);
        target.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
            +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        parent.addChild( target );
    }
    /**
     * Fetch the table based on current options set as instance vars
     * @return a String being a complete HTML table
     */
    private String fetchTable()
    {
        String rawURL = "http://localhost:8080/html/table";
        String urn = Utils.escape( docID );
        rawURL = URLEncoder.append( rawURL, urn );
        // add required params to get table
        rawURL = addGetParam( rawURL, Params.HIDE_MERGED, 
            (hideMerged)?"1":"0" );
        rawURL = addGetParam( rawURL, Params.COMPACT,(compact)?"1":"0" );
        rawURL = addGetParam( rawURL, Params.WHOLE_WORDS, 
            (wholeWords)?"1":"0" );
        rawURL = addGetParam( rawURL, Params.LENGTH, 
            Integer.toString(length) );
        rawURL = addGetParam( rawURL, Params.OFFSET, "0" );
        rawURL = addGetParam( rawURL, Params.SOME_VERSIONS, 
            (someVersions)?"1":"0" );
        rawURL = addGetParam( rawURL, Params.VERSION1, version1 );
        rawURL = addGetParam( rawURL, Params.FIRSTID, "1" );
        if ( selectedVersions != null )
            rawURL = addGetParam( rawURL, Params.SELECTED_VERSIONS, 
                selectedVersions );
        // fetch table
        try
        {
            return URLEncoder.getResponseForUrl( rawURL );
        }
        catch ( Exception e )
        {
            System.out.println("Failed to fetch table");
            return "";
        }
    }
    /**
     * Add the table div to the page
     * @param outer the direct parent div of the table
     */
    private void addTableDiv( Element outer )
    {
        Element tableDiv = new Element(HTMLNames.DIV);
        tableDiv.addAttribute(HTMLNames.ID,"table");
        outer.addChild( tableDiv );
        Element innerTableDiv = new Element(HTMLNames.DIV);
        innerTableDiv.addAttribute(HTMLNames.ID,"innertable");
        tableDiv.addChild( innerTableDiv );
        String tableText = fetchTable();
        innerTableDiv.addChild(new HTMLLiteral(tableText) );
    }
    /**
     * Add the main text div
     * @param outer the parent div
     */
    private void addTextDiv( Element outer )
    {
        Element content = getVersionContent();
        Element textDiv = new Element(HTMLNames.DIV);
        textDiv.addAttribute(HTMLNames.ID,"text");
        outer.addChild( textDiv );
        Element contentDiv = new Element(HTMLNames.DIV);
        contentDiv.addAttribute(HTMLNames.ID,"content");
        textDiv.addChild( contentDiv );
        contentDiv.addChild( content );
    }
    /**
     * Add a versions selector and prompt to the main div
     * @param outer the parent div
     */
    private void addVersionsDiv( Element outer )
    {
        Element selector = getVersionSelector();
        Element versionsDiv = new Element(HTMLNames.DIV);
        versionsDiv.addAttribute(HTMLNames.ID,"versions");
        versionsDiv.addChild(selector);
        outer.addChild( versionsDiv );
    }
    /**
     * Add the prefs "dialog"
     * @param outer the direct parent div
     */
    private void addPrefsDiv( Element outer ) throws Exception
    {
        Element prefsDiv = new Element(HTMLNames.DIV);
        outer.addChild( prefsDiv );
        prefsDiv.addAttribute(HTMLNames.ID,"prefs");
        Element p1 = new Element(HTMLNames.P);
        prefsDiv.addChild( p1 );
        addCheckBox( p1, hideMerged, "hide merged", Params.HIDE_MERGED );
        addCheckBox( p1, wholeWords, "&nbsp;&nbsp;whole words", 
            Params.WHOLE_WORDS );
        Element check = addCheckBox( p1, compact, "&nbsp;&nbsp;compact", 
            Params.COMPACT);
        check.addAttribute("disabled","disabled");
        // submit button
        Element submit = new Element(HTMLNames.INPUT);
        submit.addAttribute(HTMLNames.TYPE,HTMLNames.SUBMIT);
        submit.addAttribute(HTMLNames.VALUE,"Update");
        submit.addAttribute(HTMLNames.ONCLICK,"presubmit()");
        p1.addChild( submit );
        // next row of buttons
        Element p2 = new Element(HTMLNames.P);
        prefsDiv.addChild( p2 );
        // all versions
        addCheckBox( p2, someVersions, "some versions", Params.SOME_VERSIONS );
        addVersionDropdown( p2 );
        addHighlightDropdown( p2 );
    }
    /**
     * Add the button panel (controls the table and prefs divs
     * @param outer the direct parent
     */
    private void addButtonDiv( Element outer )
    {
        Element buttonDiv = new Element(HTMLNames.DIV);
        buttonDiv.addAttribute(HTMLNames.ID,"buttons");
        Element buttonPrefs = new Element(HTMLNames.BUTTON);
        buttonPrefs.addAttribute(HTMLNames.NAME, "prefs");
        buttonPrefs.addText("prefs");
        Element buttonTable = new Element(HTMLNames.BUTTON);
        buttonTable.addText("table");
        buttonTable.addAttribute(HTMLNames.NAME, "table");
        Element buttonNone = new Element(HTMLNames.BUTTON);
        buttonNone.addText("none");
        buttonNone.addAttribute(HTMLNames.NAME, "none");
        buttonDiv.addChild( buttonPrefs );
        buttonDiv.addChild( buttonTable );
        buttonDiv.addChild( buttonNone );
        outer.addChild( buttonDiv );
    }
    /**
     * Get the content of this test
     * @return a div element with appropriate children
     */
    @Override
    public Element getContent()
    {
        Element form = formElement("/tests/edition" );
        Element outer = new Element( "div" );
        outer.addAttribute( HTMLNames.ID, "centre" );
        try
        {
            addVersionsDiv( outer );
            addTextDiv( outer );
            // these next two may be hidden
            addTableDiv( outer );
            addPrefsDiv( outer );
            // this controls the hiding/showing of the above two
            addButtonDiv( outer );
        }
        catch ( Exception e )
        {
            return new Text( "Failed Edition test: "+e.getMessage());
        }
        /*if ( version1 != null )
            rememberParam( form, Params.VERSION1, version1 );*/
        form.addChild( outer );
        return form;
    }
}
