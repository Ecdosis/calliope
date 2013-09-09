/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;
import static calliope.handler.post.importer.Splitter.VERSIONS;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represent a cluster of siblings (add/del/rdg/sic/corr etc) in a TEI document.
 * @author desmond
 */
public class Cluster 
{
    int depth;
    HashMap<String,MutableInt> counter;
    HashSet<Element> elements;
    HashSet<String> versions;
    /** first default version equated with BSE */
    Element firstDefault;
    /** last version in a sibling set at the top with SOME uncancelled text */
    Element lastUncancelled;
    // parent of first entry
    Element root;
    Discriminator discrim;
    Cluster( Discriminator discrim )
    {
        counter = new HashMap<String,MutableInt>();
        elements = new HashSet<Element>();
        versions = new HashSet<String>();
        this.discrim = discrim;
    }
    /**
     * Does this element have a sibling with the same parent?
     * @param elem the element to test
     */
    boolean hasSameParent( Element elem )
    {
        String eName = elem.getNodeName();
        Node eParent = elem.getParentNode();
        Iterator<Element> iter = elements.iterator();
        while ( iter.hasNext() )
        {
            Element s = iter.next();
            if ( s.getNodeName().equals(eName) && s.getParentNode()==eParent 
                && !discrim.areAdjacent(s,elem) && !discrim.areAdjacent(elem,s) )
                return true;
        }
        return false;
    }
    /**
     * Add a new name or increment the index of an existing one
     * @param name the name of the new element
     * @param elem the element itself
     */
    void inc( String name, Element elem )
    {
        String pName = "";
        if ( !counter.containsKey(name) )
        {
            MutableInt pos = new MutableInt(0);
            counter.put( name, pos );
            pName = name+"0";
            if ( firstDefault==null && !discrim.isAdd(pName) )
                firstDefault = elem;
            elements.add(elem);
        }
        else
        {
            MutableInt mi = counter.get(name);
            if ( !hasSameParent(elem) )
                mi.inc();
            pName = name+mi.intValue();
            if ( firstDefault==null && !discrim.isAdd(pName) )
                firstDefault = elem;
            elements.add( elem );
        }
        elem.setAttribute( Splitter.ORIGINAL, pName );
        versions.add( pName );
        // set local instance vars
        if ( !discrim.isDeleted(elem) && !discrim.isCorrected(elem) 
            && pName.length()>0 )
            lastUncancelled = elem;
        Node parent = elem.getParentNode();
        if ( root == null && parent != null 
            && parent.getNodeType()==Node.ELEMENT_NODE )
            root = (Element)parent;
    }
    /**
     * Keep track of how deep we are from where we started
     */
    void descend()
    {
        depth++;
    }
    void ascend()
    {
        if ( depth > 0 )
            depth--;
    }
    int depth()
    {
        return depth;
    }
    /**
     * Get the canonical name for the specified version
     * @param key the key such as "add" or "del" or "rdg"
     * @return rdg0, del1 etc
     */
    String getName( String key )
    {
        if ( counter.containsKey(key) )
            return key+counter.get(key).intValue();
        else
            return key+0;
    }
    String getAll()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = versions.iterator();
        while ( iter.hasNext() )
        {
            String key = iter.next();
            if ( sb.length()>0 )
                sb.append(" ");
            sb.append( key );
        }
        return sb.toString();
    }
    /**
     * Is this Registry ready to be percolated up?
     * @return true if it's time
     */
    boolean ripe()
    {
        return counter.size()>0 && depth==0;
    }
    int size()
    {
        return counter.size();
    }
    /**
     * Copy the given child versions to all parent nodes, recursively
     * @param elem the parent node
     * @param vers the versions of the child to add to elem
     */
    void percolateParent( Element elem, String vers )
    {
        Discriminator.addVersion( elem, vers );
        Node parent = elem.getParentNode();
        if ( parent != null && parent instanceof Element )
            percolateParent( (Element)parent, vers );
    }
    /**
     * Apply the given version to all sub-elements 
     * @param elem the element to start from
     * @param vers the version to add
     * @param s the splitter object
     */
    void applyVersion( Element elem, String vers, Splitter s )
    {
        Discriminator.addVersion( elem, vers );
        Node n = elem.getFirstChild();
        while ( n != null )
        {
            boolean first = false;
            if ( n.getNodeType()==Node.ELEMENT_NODE )
            {
                Element child = (Element)n;
                if ( discrim.isSibling(child) )
                {
                    if ( !first )
                    {
                        applyVersion( (Element)n, vers, s );
                        first = true;
                    }
                }
                else
                {
                    applyVersion( (Element)n, vers, s );
                    first = false;
                }
            }
            else if ( n.getNodeType()==Node.TEXT_NODE 
                && !Discriminator.isWhitespace(n.getTextContent()) )
            {
                first = false;
            }
            n = n.getNextSibling();
        }
    }
    /**
     * Get the versions of all sibling to the given element
     * @param elem the element to get the sibling versions of
     * @return a space-delimited set of sibling versions
     */
    String siblingVersions( Element elem )
    {
        StringBuilder sb = new StringBuilder();
        Element start = Discriminator.firstSibling( elem );
        if ( start != null )
        {
            Node sibling = start;
            while ( sibling != null )
            {
                if ( sibling.getNodeType()==Node.ELEMENT_NODE )
                {
                    String vers = ((Element)sibling).getAttribute(Splitter.ORIGINAL);
                    if ( sb.length()>0 )
                        sb.append(" ");
                    sb.append( vers );
                }
                sibling = sibling.getNextSibling();
            }
        }
        return sb.toString();
    }
    /**
     * Subtract some versions from an existing version set
     * @param all all the versions
     * @param minus those we don't want in all
     * @return all with the minus versions removed
     */
    private String subtractVersions( String all, String minus )
    {
        StringBuilder sb = new StringBuilder();
        String[] aParts = all.split(" ");
        String[] mParts = minus.split(" ");
        for ( int i=0;i<aParts.length;i++ )
        {
            int j=0;
            for ( j=0;j<mParts.length;j++ )
                if ( aParts[i].equals(mParts[j]) )
                    break;
            if ( j == mParts.length )
            {
                if ( sb.length()>0 )
                    sb.append(" ");
                sb.append( aParts[i] );
            }
        }
        return sb.toString();
    }
    /**
     * Inherit from the parent IF it belongs to the cluster
     * @param elem the child element
     * @param pElem the parent element above it
     */
    void inheritFromParent( Element elem, Splitter s )
    {
        // Rule 4
        if ( discrim.isFirstAdjacentSibling(elem,s) )
        {
            Node parent = elem.getParentNode();
            if ( parent != null && parent instanceof Element && parent != root )
            {
                Element pElem = (Element)parent;
                inheritFromParent( pElem, s );
                if ( elements.contains(pElem) )
                {
                    String pVers = pElem.getAttribute(VERSIONS);
                    // exclude siblng versions
                    String sVers = siblingVersions( elem );
                    pVers = subtractVersions(pVers,sVers);
                    Discriminator.addVersion( elem, pVers );
                }
            }
        }
    }
    /**
     * Propagate all versions of each element to their ancestors, then reset.
     */
    void percolateUp( Splitter s )
    {
        Iterator<Element> iter = elements.iterator();
        while ( iter.hasNext() )
        {
            Element elem = iter.next();
            String vers = elem.getAttribute(VERSIONS);
            Node parent = elem.getParentNode();
            if ( parent != null && parent instanceof Element )
            {
                Element pElem = (Element)parent;
                percolateParent( pElem, vers );
            }
        }
        iter = elements.iterator();
        while ( iter.hasNext() )
        {
            Element elem = iter.next();
            inheritFromParent( elem, s );
        }
        // set default version
        if ( firstDefault != null )
        {
            applyVersion( firstDefault, Splitter.BASE, s );
        }
        if ( lastUncancelled != null )
        {
            // LATER add all versions NOT in this set, and not deleted
            // try to migrate it up to the top level
            Node p = lastUncancelled;
            while ( p != null && p.getParentNode() != root )
            {
                p = p.getParentNode();
                if ( p != null 
                    && p.getNodeType()==Node.ELEMENT_NODE 
                    && elements.contains(p) )
                    lastUncancelled = (Element)p;
            }
            lastUncancelled.setAttribute( Splitter.FINAL, this.getAll() );
        }
        else
            System.out.println("Warning: no uncancelled version!");
        counter.clear();
        elements.clear();
        versions.clear();
        root = lastUncancelled = firstDefault = null;
    }
}
