/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;

import calliope.exception.ConfigException;
import static calliope.handler.post.importer.Splitter.VERSIONS;
import calliope.json.JSONDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handle element manipulation and discrimination between siblings etc.
 * @author desmond
 */
public class Discriminator 
{
    private static String REMOVALS = "removals";
    private static String SIBLINGS = "siblings";
    private static String FIRST = "first";
    private static String SECOND = "second";
    private static String WITS = "wits";
    private static String DROPS = "drops";
    private static String KEYS = "keys";
    private static String ADD = "add";
    private static String DEL = "del";
    private static String REG = "reg";
    private static String CORR = "corr";
    private static String EXPAN = "expan";
    private HashMap<String,Sibling> siblings;
    HashSet<String> removals;
    String[] drops;
    String wits;
    static String defaultConf = 
    "{ \"siblings\": [ {\"first\":\"add\", \"second\": \"del\"}, "
        + "{\"first\": \"abbr\", \"second\": \"expan\"}, {\"first\": \"orig\", "
        + "\"second\": \"reg\"}, {\"first\": \"sic\", \"second\": \"corr\"}, "
        + "{\"first\": \"lem\", \"second\": \"rdg\", \"wits\": \"wit\"}], "
        + "\"removals\":[\"app\",\"rdgGrp\",\"subst\",\"mod\",\"choice\","
        + "\"add\",\"del\",\"rdg\",\"lem\",\"add\",\"del\"], \"drops\":"
        + "[\"del0\",\"rdg0\"],\"wits\":\"wits\", \"keys\":[{\"add\":\"add\"},"
        + "{\"del\":\"del\"},{\"reg\":\"reg\"},{\"corr\":\"corr\"},{\"expan\":"
        + "\"expan\"}]}";
    String add;
    String del;
    String abbrev;
    String expan;
    String reg;
    String corr;
    Discriminator( JSONDocument config ) throws ConfigException
    {
        ArrayList sets = (ArrayList)config.get( REMOVALS );
        removals = new HashSet<String>();
        if ( sets != null )
        {
            for ( int i=0;i<sets.size();i++ )
            {
                String r = (String)sets.get( i );
                removals.add( r );
            }
        }
        ArrayList brothers = (ArrayList)config.get( SIBLINGS );
        if ( brothers == null )
            throw new ConfigException("missing siblings in splitter config");
        siblings = new HashMap<String,Sibling>();
        for ( int i=0;i<brothers.size();i++ )
        {
            JSONDocument doc = (JSONDocument)brothers.get( i );
            String first = (String)doc.get( FIRST );
            String second = (String)doc.get( SECOND );
            String wits = (String)doc.get( WITS );
            if ( first!=null&&second!=null )
            {
                siblings.put( first,new Sibling(first,second,wits) );
                siblings.put( second,new Sibling(second,first,wits) );
            }
        }
        ArrayList droplist = (ArrayList)config.get( DROPS );
        if ( droplist == null )
            throw new ConfigException("missing droplist in splitter config");
        drops = new String[droplist.size()];
        for ( int i=0;i<droplist.size();i++ )
        {
            drops[i] = (String)droplist.get(i);
        }
        wits = (String) config.get( WITS );
        if ( wits == null )
            wits = "wits";
        ArrayList keylist = (ArrayList)config.get( KEYS );
        if ( keylist == null )
            throw new ConfigException("missing keylist in splitter config");
        for ( int i=0;i<keylist.size();i++ )
        {
            JSONDocument doc = (JSONDocument)keylist.get( i );
            Set keys = doc.keySet();
            Iterator iter = keys.iterator();
            while ( iter.hasNext() )
            {
                String key = (String)iter.next();
                if ( key.equals(ADD) )
                    add = (String)doc.get(key);
                else if ( key.equals(DEL) )
                    del = (String)doc.get(key);
                else if ( key.equals(REG) )
                    reg = (String)doc.get(key);
                else if ( key.equals(CORR) )
                    corr = (String)doc.get(key);
                else if ( key.equals(EXPAN) )
                    expan = (String)doc.get(key);
                else
                    throw new ConfigException("Unknown key "+key);
            }
        }
    }
    boolean isSibling( Element elem )
    {
        String name = elem.getNodeName();
        return siblings.containsKey(name);
    }
    String getSibling( String name )
    {
        Sibling sib = siblings.get(name);
        if ( sib != null )
            return sib.brother;
        else
            return null;
    }
    /**
     * Does this element only contain deleted text?
     * @param elem the element in question
     * @return true if it is, else false
     */
    boolean isDeleted( Element elem )
    {
        boolean result = true;
        String eName = elem.getNodeName();
        if ( eName.equals(del) )
            result = true;
        else 
        {
            String text = elem.getTextContent();
            int textLen = (text!=null)?text.length():0;
            int childLen = 0;
            Element child = firstChild( elem );
            while ( child != null && !result )
            {
                String childText = elem.getTextContent();
                if ( isDeleted(child) )
                    childLen += (childText!=null)?childText.length():0;
                child = nextSibling( child, true );
            }
            result = childLen == textLen;
        }
        return result;
    }
    boolean isCorrectedElem( Element elem )
    {
        String eName = elem.getNodeName();
        return eName.equals(reg)
            ||eName.equals(corr)
            ||eName.equals(expan);
    }
    /**
     * Does the element only contain corrected text?
     * @param elem the element to test
     * @return true if it is else false
     */
    boolean isCorrected( Element elem )
    {
        boolean result = false;
        if ( isCorrectedElem(elem) )
            result = true;
        else 
        {
            String text = elem.getTextContent();
            int textLen = (text!=null)?text.length():0;
            int childLen = 0;
            Element child = firstChild( elem );
            while ( child != null && !result )
            {
                String childText = elem.getTextContent();
                if ( isCorrected(child) )
                    childLen += (childText!=null)?childText.length():0;
                child = nextSibling( child, true );
            }
            result = childLen == textLen;
        }
        return result;
    }
    boolean isAdd( String name )
    {
        return name.startsWith(add);
    }
    /**
     * Get the last child element that is not corrected or deleted
     * @param elem the element to test
     * @return its last uncancelled/uncorrected child or null
     */
    Element lastOpenChild( Element elem )
    {
        Node child = elem.getFirstChild();
        Element last = null;
        while ( child != null )
        {
            if ( child.getNodeType()==Node.ELEMENT_NODE )
            {
                Element cElem = (Element)child;
                if ( !isDeleted(cElem) && !isCorrected(cElem) )
                    last = cElem;
            }
            child = child.getNextSibling();
        }
        return last;
    }
    /**
     * Are two elements next to each other?
     * @param s1 the LHS element
     * @param s2 the RHS element
     * @return true if s1 is just before s2
     */
    boolean areAdjacent( Element s1, Element s2 )
    {
        Node n = s1;
        while ( n != null )
        {
            n = n.getNextSibling();
            if ( n != null )
            {
                if ( n.getNodeType()==Node.ELEMENT_NODE )
                {
                    if ( n == s2 )
                        return true;
                }
                else if ( n.getNodeType()==Node.TEXT_NODE )
                {
                    if ( !isWhitespace(n.getTextContent()) )
                        break;
                }
            }
        }
        return false;
    }
    static Element firstSibling( Element elem )
    {
        Node start = elem;
        while ( start.getPreviousSibling() != null )
            start = start.getPreviousSibling();
        while ( start.getNodeType() != Node.ELEMENT_NODE )
            start = start.getNextSibling();
        return (start.getNodeType()==Node.ELEMENT_NODE)?(Element)start:null;
    }
    /**
     * Is the given element the first sibling of its type in sequence?
     * @param elem the sibling to test
     * @param s the SPlitter object
     * @return true if it is NOT preceded by another sibling of its type
     */
    boolean isFirstAdjacentSibling( Element elem, Splitter s )
    {
        Node start = elem;
        String sName = start.getNodeName();
        String sibName = getSibling(sName);
        while ( start != null )
        {
            Node prev = start.getPreviousSibling();
            if ( prev != null )
            {
                if ( prev.getNodeType()==Node.ELEMENT_NODE )
                {
                    String pName = prev.getNodeName();
                    if ( (pName.equals(sName)||pName.equals(sibName)) )
                        return false;
                }
                else if ( prev.getNodeType()!=Node.TEXT_NODE 
                    || !isWhitespace(prev.getTextContent()) )
                    return true;
            }
            start = prev;
        }
        return true;
    }
    /**
     * Merge two sets of space-delimited short version names
     * @param oldV the old version names
     * @param newV the new version names to add
     * @return the merged versions
     */
    static String mergeVersions( String oldV, String newV )
    {
        if ( oldV.length()==0 )
            return newV;
        else if ( newV.length()==0 )
            return oldV;
        else
        {
            String[] oldVersions = oldV.split( " " );
            String[] newVersions = newV.split( " " );
            HashSet<String> all = new HashSet<String>();
            for ( int i=0;i<oldVersions.length;i++ )
                all.add( oldVersions[i] );
            for ( int i=0;i<newVersions.length;i++ )
                all.add( newVersions[i] );
            Iterator<String> iter = all.iterator();
            StringBuilder sb = new StringBuilder();
            while ( iter.hasNext() )
            {
                String version = iter.next();
                sb.append( version );
                if ( iter.hasNext() )
                    sb.append( " " );
            }
            return sb.toString();
        }
    }
    /**
     * Get the next sibling that is an element
     * @param elem the element
     * @param skipText if true skip text nodes to next element node
     * @return its next sibling of elem or null
     */
    static Element nextSibling( Element elem, boolean skipText )
    {
        Node n = elem.getNextSibling();
        while ( n != null )
        {
            if ( !skipText 
                && n.getNodeType() == Node.TEXT_NODE 
                && !isWhitespace(n.getTextContent()) )
                    return null;
            else if ( n.getNodeType()==Node.ELEMENT_NODE )
                return (Element)n;
            n = n.getNextSibling();
        }
        return null;
    }
    /**
     * Is a string nothing by white space?
     * @param str the string in question
     * @return true if that is all it is
     */
    static boolean isWhitespace( String str )
    {
        for ( int i=0;i<str.length();i++ )
            if ( !Character.isWhitespace(str.charAt(i)) )
                return false;
        return true;
    }
    /**
     * Get the next child that is an element
     * @param elem the element
     * @return its first child of elem or null
     */
    static Element firstChild( Element elem )
    {
        Node n = elem.getFirstChild();
        while ( n != null && n.getNodeType()!=Node.ELEMENT_NODE )
            n = n.getNextSibling();
        return (Element) n;
    }
    /**
     * Add a new version to the current element
     * @param elem the element to add the new version to
     * @param wits the new version
     */
    static void addVersion( Element elem, String wits )
    {
        if ( elem.hasAttribute(VERSIONS) )
            elem.setAttribute( VERSIONS, 
                mergeVersions(elem.getAttribute(VERSIONS),wits));
        else
            elem.setAttribute( VERSIONS, wits );
    }
    /**
     * Get the next true sibling of the given element
     * @param elem the element to get the next sibling of
     * @return the next true sibling of elem or null
     */
    Element nextTrueSibling( Element elem )
    {
        Node n = elem;
        while ( elem != null )
        {
            n = n.getNextSibling();
            if ( n == null )
                elem = null;
            else if ( n.getNodeType() == Node.ELEMENT_NODE )
            {
                Sibling s = siblings.get(n.getNodeName());
                if ( s != null )
                {
                    String sName = s.getSibling();
                    String nName = n.getNodeName();
                    String eName = elem.getNodeName();
                    if ( eName.equals(sName) || eName.equals(nName) )
                    {
                        elem = (Element)n;
                        break;
                    }
                    else
                        elem = null;
                }
                else
                    elem = null;
            }
            else if ( n.getNodeType() == Node.TEXT_NODE
                && !isWhitespace(n.getTextContent()) )
                elem = null;
        }
        return elem;
    }
}
