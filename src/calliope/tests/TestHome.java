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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import calliope.tests.html.HTML;
import calliope.tests.html.Element;
import calliope.tests.html.HTMLCatalog;
import calliope.tests.html.Text;
import calliope.exception.*;
import calliope.AeseServer;
/*
 * This class implements a twist-down hierarchical list
 */
public class TestHome extends Test
{
    static String SCHEDULER_SCRIPT = 
    "var OnloadScheduler =\n\tnew function(){\n\t\tvar executed = "
    +"false;\n\t\tvar negativePriority = [];\n\tvar positivePriori"
    +"ty = [];\n\n\tfunction execute(tasks){\n\t\t\tif (tasks inst"
    +"anceof Array){\n\t\t\t\tfor (var index = 0; index < tasks.le"
    +"ngth; index ++){\n\t\t\ttry{\n\t\t\ttasks[index]();\n\t\t\t}"
    +"catch (error){\n\t\t\t\t\t\t}\n\t\t}\n\t\t}else if (!execute"
    +"d){\n\t\t\t\texecuted = true;\n\t\t\t\tfor (var index = nega"
    +"tivePriority.length - 1; index > 0; index --){\n\t\t\texecut"
    +"e(negativePriority[index]);\n\t\t}\n\t\tfor (var index = 0; "
    +"index < positivePriority.length; index ++){\n\t\t\texecute(p"
    +"ositivePriority[index]);\n\t\t}\n\t\t}\n\t}\n\n\tthis.schedu"
    +"le = function(task, priority){\n\t\t\tif (!priority) priorit"
    +"y = 0;\n\t\t\tif (task instanceof Function){\n\t\t\t\tif (pr"
    +"iority < 0){\n\t\t\tif (!negativePriority[-priority]) negati"
    +"vePriority[-priority] = [];\n\t\t\tnegativePriority[-priorit"
    +"y].push(task);\n\t\t}else{\n\t\t\tif (!positivePriority[prio"
    +"rity]) positivePriority[priority] = [];\n\t\t\tpositivePrior"
    +"ity[priority].push(task);\n\t\t}\n\t\t}else{\n\t\t\t\tthis.s"
    +"chedule(function(){eval(task)}, priority);\n\t\t}\n\t}\n\t\t"
    +"if ('addEventListener' in document){\n\t\t\tdocument.addEven"
    +"tListener('DOMContentLoaded', execute, false);\n\t\t\twindow"
    +".addEventListener('load', execute, false);\n\t}else{\n\t\t\t"
    +"if ('doScroll' in document.documentElement && window == wind"
    +"ow.top){\n\t\t\t\t(function(){\n\t\t\ttry{\n\t\t\tdocument.d"
    +"ocumentElement.doScroll('left');\n\t\t\texecute();\n\t\t\t}c"
    +"atch (error){\n\t\t\twindow.setTimeout(arguments.callee, 0);"
    +"\n\t\t\t}\n\t\t})();\n\t\t}\n\t\t\tdocument.attachEvent(\n\t"
    +"\t\t'onreadystatechange',\n\t\t\tfunction(){\n\t\t\tif (docu"
    +"ment.readyState == 'complete') execute();\n\t\t\t});\n\t\t\t"
    +"window.attachEvent('onload', execute);\n\t}\n\t}();";
    static String LIST_SCRIPT = 
    "var CollapsibleLists =\nnew function(){\nthis.reopen = functi"
    +"on()\n{\n\tvar elem = document.getElementById(\"DOC_ID\");\n"
    +"\tvar components = elem.value.split(\"/\");\n\tvar lis = doc"
    +"ument.getElementsByTagName('li');\n\tfor ( var i=0;i<compone"
    +"nts.length;i++ )\n\t{\n\t\tfor ( var j=0;j<lis.length;j++ )\n"
    +"\t\t{\n\t\t\tif ( lis[j].firstChild.nodeValue == components"
    +"[i] )\n\t\t\t\ttoggle( lis[j] );\n\t\t}\n\t}\n};\nthis.apply"
    +" = function(doNotRecurse){\n\tvar uls = document.getElements"
    +"ByTagName('ul');\n\tfor (var index = 0; index < uls.length; "
    +"index ++){\n\tif (uls[index].className.match(/(^| )collapsib"
    +"leList( |$)/)){\n\t\tthis.applyTo(uls[index], true);\n\t\tif"
    +" (!doNotRecurse){\n\t\tvar subUls = uls[index].getElementsBy"
    +"TagName('ul');\n\t\tfor (var subIndex = 0; subIndex < subUls"
    +".length; subIndex ++){\n\t\t\tsubUls[subIndex].className += "
    +"' collapsibleList';\n\t\t}\n\t\t}\n\t}\n\t}\n\tthis.reopen()"
    +";\n};\nthis.applyTo = function(node, doNotRecurse){\n\tvar l"
    +"is = node.getElementsByTagName('li');\n\tfor (var index = 0;"
    +" index < lis.length; index ++){\n\tif (!doNotRecurse || node"
    +" == lis[index].parentNode){\n\t\tif (lis[index].addEventList"
    +"ener){\n\t\tlis[index].addEventListener(\n\t\t\t'mousedown',"
    +" function (e){ e.preventDefault(); }, false);\n\t\t}else{\n\t"
    +"\tlis[index].attachEvent(\n\t\t\t'onselectstart', function("
    +"){ event.returnValue = false; });\n\t\t}\n\t\tif (lis[index]"
    +".addEventListener){\n\t\tlis[index].addEventListener(\n\t\t\t"
    +"'click', createClickListener(lis[index]), false);\n\t\t}els"
    +"e{\n\t\tlis[index].attachEvent(\n\t\t\t'onclick', createClic"
    +"kListener(lis[index]));\n\t\t}\n\t\ttoggle(lis[index]);\n\t}"
    +"\n\t}\n};\nfunction createClickListener(node){\n\treturn fun"
    +"ction(e){\n\tif (!e) e = window.event;\n\tif (node == (e.tar"
    +"get ? e.target : e.srcElement)) toggle(node);\n\t};\n}\nfunc"
    +"tion toggle(node){\n\tvar open = node.className.match(/(^| )"
    +"collapsibleListClosed( |$)/);\n\tvar uls = node.getElementsB"
    +"yTagName('ul');\n\tfor (var index = 0; index < uls.length; i"
    +"ndex ++){\n\tvar li = uls[index];\n\twhile (li.nodeName != '"
    +"LI') li = li.parentNode;\n\tif (li == node) uls[index].style"
    +".display = (open ? 'block' : 'none');\n\t}\n\tnode.className"
    +" =\n\t\tnode.className.replace(\n\t\t\t/(^| )collapsibleList"
    +"(Open|Closed)( |$)/, '');\n\tif (uls.length > 0){\n\tnode.cl"
    +"assName += ' collapsibleList' + (open ? 'Open' : 'Closed');\n"
    +"\t}\n}\n}();\nOnloadScheduler.schedule(function(){ Collapsi"
    +"bleLists.apply(); });";
    static String LIST_STYLE = 
    "ul ul { margin-right:0; margin-bottom:0;}\nbody { padding:0;m"
    +"argin:0;font-family:Georgia,Utopia,Charter,'Droid Serif','Ti"
    +"mes New Roman',Times,serif;line-height:1.5; }\n.treeView { -"
    +"moz-user-select:none;position:relative; }\n.treeView ul{ mar"
    +"gin:0 0 0 -1.5em;padding:0 0 0 1.5em;}\n.treeView ul ul{ bac"
    +"kground:url('/corpix/list/list-item-contents.png') repeat-y "
    +"left;}\n.treeView li.lastChild > ul{background-image:none;}"
    +"\n.treeView li{ margin:0; padding:0; background:url('/corpix/"
    +"list/list-item-root.png') no-repeat top left;list-style-posi"
    +"tion:inside; list-style-image:url('/corpix/list/button.png')"
    +";cursor:auto; }\n.treeView li.collapsibleListOpen{list-style"
    +"-image:url('/corpix/list/button-open.png'); cursor:pointer; "
    +"}\n.treeView li.collapsibleListClosed{ list-style-image:url("
    +"'/corpix/list/button-closed.png'); cursor:pointer; }\n.treeV"
    +"iew li li{background-image:url('/corpix/list/list-item.png')"
    +"; padding-left:1.5em;}\n.treeView li.lastChild{ background-i"
    +"mage:url('/corpix/list/list-item-last.png');}\n.treeView li."
    +"collapsibleListOpen{ background-image:url('/corpix/list/list"
    +"-item-open.png'); }\n.treeView li.collapsibleListOpen.lastCh"
    +"ild{background-image:url('/corpix/list/list-item-last-open.p"
    +"ng'); }";
    static String CATALOG_SCRIPT = 
    "function setPath( link )\n{\n\tvar elem = document.getElement"
    +"ById(\"DOC_ID\");\n\telem.value=link;\n\tdocument.forms.defa"
    +"ult.setAttribute('action','/tests/html');\n\tdocument.forms."
    +"default.submit();\n}\nfunction toggle(node)\n{\n\tvar open ="
    +" node.className.match(/(^| )collapsibleListClosed( |$)/);\n\t"
    +"var uls = node.getElementsByTagName('ul');\n\tfor (var inde"
    +"x = 0; index < uls.length; index ++)\n\t{\n\t\tvar li = uls["
    +"index];\n\t\twhile (li.nodeName != 'LI') li = li.parentNode;"
    +"\n\t\tif (li == node) \n\t\t\tuls[index].style.display = (op"
    +"en ? 'block' : 'none');\n\t}\n\tnode.className =\n\tnode.cla"
    +"ssName.replace(\n\t\t/(^| )collapsibleList(Open|Closed)( |$)"
    +"/, '');\n\tif (uls.length > 0)\n\t{\n\t\tnode.className += '"
    +" collapsibleList' + (open ? 'Open' : 'Closed');\n\t}\n}\nfun"
    +"ction reopenList(path)\n{\n\tCollapsibleLists.apply();\n\tif"
    +" ( path.length>0 )\n\t{\n\t\tvar seek = \"javascript:setPath"
    +"('\"+path+\"')\";\n\t\tvar stack = new Array();\n\t\tfor ( v"
    +"ar i=0;i<document.links.length;i++ )\n\t\t{\n\t\t\tif ( docu"
    +"ment.links[i].href==seek )\n\t\t\t{\n\t\t\t\tvar parent = do"
    +"cument.links[i].parentNode;\n\t\t\t\twhile ( parent.parentNo"
    +"de != null &&parent.parentNode.className!=\"treeView\" )\n\t"
    +"\t\t\t{\n\t\t\t\t\tif ( parent!=document.links[i].parentNode"
    +"\n\t\t\t\t\t\t&&parent.nodeType==1\n\t\t\t\t\t\t&&parent.nod"
    +"eName==\"LI\"\n\t\t\t\t\t\t&&parent.className != null )\n\t\t"
    +"\t\t\t{\n\t\t\t\t\t\tstack.push(parent);\n\t\t\t\t\t}\n\t\t"
    +"\t\t\tparent = parent.parentNode;\n\t\t\t\t}\n\t\t\t}\n\t\t}"
    +"\n\t\twhile ( stack.length > 0 )\n\t\t{\n\t\t\tvar li = stac"
    +"k.pop();\n\t\t\ttoggle( li );\n\t\t}\n\t\tsetPath( path );\n"
    +"\t}\n}";
    public TestHome()
    {
        description = "Index to the document database";
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
        // create the doc and install the scripts etc
        doc = new HTML();
        doc.getHeader().addScript( SCHEDULER_SCRIPT );
        doc.getHeader().addScript( LIST_SCRIPT );
        doc.getHeader().addScript( CATALOG_SCRIPT );
        doc.getHeader().addCSS( LIST_STYLE );
        super.handle( request, response, urn );
    }
    /**
     * Ensure that we get a response from the server
     * @return a non-null server response or raise an exception
     * @throws AeseException 
     */
    String pollServer() throws AeseException
    {
        String json = null;
        int count = 0;
        while ( json == null && count < 5 )
        {
            json = AeseServer.getConnection().getFromDb("/cortex/_all_docs/");
            count++;
        }
        if ( json == null )
            throw new AeseException("No response from database");
        return json;
    }
    /**
     * Get the content of this test: a simple dropdown menu
     * @return a select element object with appropriate attributes and children
     */
    @Override
    public Element getContent()
    {
        try
        {
            String data = pollServer();
            Element form = formElement( "/tests/home" );
            form.addChild( docIDHidden(docID) );
            HTMLCatalog cata = new HTMLCatalog();
            int count = 5;
            boolean result = false;
            do
            {
                result = cata.load( data );
                if ( !result )
                {
                    data = pollServer();
                    count++;
                }
            } while (!result&&count<5);
            // NB this method itself is called by build
            cata.build();
            form.addChild( cata );
            return form;
        }
        catch ( Exception e )
        {
            e.printStackTrace(System.out);
            return new Text( "Failed HTML Catalog test: "
                +e.getMessage() );
        }
    }
}