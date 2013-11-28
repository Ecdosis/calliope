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
package calliope.search;

import calliope.Connector;
import calliope.Utils;
import calliope.constants.Formats;
import calliope.constants.JSONKeys;
import calliope.exception.AeseException;
import calliope.constants.Database;
import calliope.constants.LuceneFields;
import calliope.json.JSONDocument;
import edu.luc.nmerge.mvd.MVD;
import edu.luc.nmerge.mvd.MVDFile;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Locale;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;

/**
 * Manage search requests. Update Lucene index etc.
 * @author desmond
 */
public class AeseSearch 
{
    static
    {
        String langCode = Locale.getDefault().getLanguage();
        try
        {
            AeseSearch.buildIndex( langCode );
        }
        catch ( AeseException ae )
        {
            ae.printStackTrace( System.out );
        }
    }
    static NIOFSDirectory index;
    static int maxHits = 300;
    static File indexLocation;
    /**
     * Update the index for just ONE docID     
     * @param docID the documents to regenerate
     * @param langCode the language code of the analyzer
     * @throws AeseException 
     */
    public static void updateIndex( String docID, String langCode ) 
        throws AeseException
    {
        try
        {
            Analyzer analyzer = createAnalyzer(langCode);
            IndexWriterConfig config = new IndexWriterConfig(
                Version.LUCENE_45, analyzer);
            if ( index == null )
                throw new AeseException(
                    "Index must be initialised before update");
            IndexWriter w = new IndexWriter( index, config );
            Term t = new Term( LuceneFields.DOCID, docID );
            w.deleteDocuments(t);
            addCorTextoIndex( docID, w );
            w.close();
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Build the entire Lucene index from scratch
     * @param langCode the language code
     */
    public static void buildIndex( String langCode ) throws AeseException
    {
        try
        {
            String[] docIDs = Connector.getConnection().listCollection( 
                Database.CORTEX );
            Analyzer analyzer = createAnalyzer(langCode);
            File home = new File( System.getProperty("user.home") );
            indexLocation = new File(home, ".calliope" );
            if ( !indexLocation.exists() )
                indexLocation.mkdir();
            AeseSearch.index = new NIOFSDirectory( indexLocation );
            IndexWriterConfig config = new IndexWriterConfig(
                Version.LUCENE_45, analyzer);
            IndexWriter w = new IndexWriter( index, config );
            for ( int i=0;i<docIDs.length;i++ )
            {
                addCorTextoIndex( docIDs[i], w );
            }
            w.close();
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    private static String formatVersionID( String vId )
    {
        int index = vId.lastIndexOf("/");
        if ( index != -1 )
            return "<span class=\"group\">"
                +vId.substring(0,index)
                +"</span> <span class=\"version\">"
                +vId.substring(index)+"</span>";
        else
            return "<span class=\"version\">"+vId+"</span>";
    }
    private static String formatDocID( String docID )
    {
        int index = docID.lastIndexOf("/");
        if ( index != -1 )
            return "<span class=\"filename\">"+docID.substring(index)+"</span>";
        else
            return "<span class=\"filename\">"+docID+"</span>";
    }
    /**
     * Search the index for the given expression
     * @param expr the expression to be parsed
     * @param langCode the language of the expression and index
     * @param profile the hit profile (where to start from etc)
     * @return the result docs
     */
    public static String searchIndex( String expr, String langCode, 
        HitProfile profile )
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            Analyzer analyzer = AeseSearch.createAnalyzer( langCode );
            DirectoryReader reader = DirectoryReader.open(AeseSearch.index);
            if ( reader != null )
            {
                IndexSearcher searcher = new IndexSearcher( reader );
                QueryParser qp = new QueryParser(Version.LUCENE_45, "text", 
                    analyzer);
                Query q = qp.parse( expr );
                TopDocs hits = searcher.search(q, AeseSearch.maxHits);
                ScoreDoc[] docs = hits.scoreDocs;
                for ( int j=profile.from;j<profile.to&&j<docs.length;j++ )
                {
                    Document doc = searcher.doc(docs[j].doc);
                    String vid = doc.get(LuceneFields.VID);
                    String docID = doc.get(LuceneFields.DOCID);
                    Highlighter h = new Highlighter(new QueryScorer(q));
                    String text = getCorTexVersion( docID, vid );
                    sb.append( formatDocID(docID) );
                    sb.append( " " );
                    sb.append( formatVersionID(vid) );
                    sb.append( " " );
                    String frag = h.getBestFragment(analyzer,"text",text);
                    sb.append("<span class=\"found\">");
                    sb.append( frag );
                    sb.append("</span>\n");
                }
                profile.numHits = docs.length;
            }
            reader.close();
        }
        catch ( Exception e )
        {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }
    /**
     * Get the text of ONE version of a document (maybe only 1 version exists)
     * @param docID the document to fetch (MVD or TEXT)
     * @param vId the version Id (may be "default")
     * @return a string being that doc/version's content
     * @throws AeseException 
     */
    private static String getCorTexVersion( String docID, String vId ) 
        throws AeseException
    {
        JSONDocument doc = null;
        byte[] data = null;
        String res = null;
        //System.out.println("fetching version "+vPath );
        res = Connector.getConnection().getFromDb(Database.CORTEX,docID);
        if ( res != null )
            doc = JSONDocument.internalise( res );
        if ( doc != null && doc.containsKey(JSONKeys.BODY) )
        {
            String format = (String)doc.get(JSONKeys.FORMAT);
            if ( format == null )
                throw new AeseException("doc missing format");
            // first check if it's an MVD 
            if ( format.startsWith(Formats.MVD) )
            {
                MVD mvd = MVDFile.internalise( (String)doc.get(
                    JSONKeys.BODY) );
                String sName = Utils.getShortName(vId);
                String gName = Utils.getGroupName(vId);
                int version = mvd.getVersionByNameAndGroup( sName, gName );
                if ( version != 0 )
                {
                    data = mvd.getVersion( version );
                    if ( data != null )
                    {
                        try
                        {
                            res = new String( data, mvd.getEncoding() );
                        }
                        catch ( UnsupportedEncodingException ue )
                        {
                            throw new AeseException( ue );
                        }
                    }
                    else
                        throw new AeseException("Version "+vId+" not found");
                }
                else
                    throw new AeseException("Version "+vId+" not found");
            }
            else
            {
                res = (String)doc.get( JSONKeys.BODY );
            }
        }
        return res;
    }
    /**
     * Create a language-specific analyzer
     * @param langCode the language code
     * @return an analyzer for that language
     */
    private static Analyzer createAnalyzer( String langCode )
    {
        if ( langCode.equals("it") )
            return new ItalianAnalyzer(Version.LUCENE_45);
        else // add other analyzers here
            return new StandardAnalyzer(Version.LUCENE_45);
    }
    /**
     * Open and index an MVD file
     * @param docID the docID of the cortex
     * @param w the Lucene index-writer
     */
    private static void addCorTextoIndex( String docID, IndexWriter w ) 
        throws AeseException
    {
        try
        {
            HashMap<String,String> map = getCorTex( docID );
            Set<String> keys = map.keySet();
            Iterator<String> iter = keys.iterator();
            while ( iter.hasNext() )
            {
                String key = iter.next();
                Document d = new Document();
                StringField sf1 = new StringField(LuceneFields.VID, key, 
                    Field.Store.YES );
                d.add( sf1 );
                StringField sf2 = new StringField( LuceneFields.DOCID, docID, 
                    Field.Store.YES);
                d.add( sf2 );
                TextField tf = new TextField(LuceneFields.TEXT,map.get(key),
                    Field.Store.NO);
                d.add( tf );
                w.addDocument( d );
            }
        }
        catch ( IOException ioe )
        {
            throw new AeseException( ioe );
        }
    }
    /**
     * Get the cortex at a particular docID. This doesn't have to be an MVD.
     * @param docID the docID of the cortex
     * @return an array of versions found at that docID
     * @throws AeseException 
     * @return a map of versionIDs to strings ("default" key if just TEXT)
     */
    private static HashMap<String,String> getCorTex( String docID ) 
        throws AeseException
    {
        HashMap<String,String> texts = new HashMap<String,String>();
        JSONDocument doc = null;
        String res = null;
        //System.out.println("fetching version "+vPath );
        res = Connector.getConnection().getFromDb(Database.CORTEX,docID);
        if ( res != null )
            doc = JSONDocument.internalise( res );
        if ( doc != null && doc.containsKey(JSONKeys.BODY) )
        {
            String format = (String)doc.get(JSONKeys.FORMAT);
            if ( format == null )
                throw new AeseException("doc missing format");
            if ( format.startsWith(Formats.MVD) )
            {
                MVD mvd = MVDFile.internalise( (String)doc.get(
                    JSONKeys.BODY) );
                if ( mvd != null )
                {
                    int nVers = mvd.numVersions();
                    for ( int i=1;i<=nVers;i++ )
                    {
                       String vId = mvd.getGroupPath((short)i)
                           +"/"+mvd.getVersionShortName(i);
                       byte[] versData = mvd.getVersion( i );
                       try
                       {
                           String versStr = new String( versData, 
                               mvd.getEncoding() );
                           texts.put( vId, versStr );
                       }
                       catch ( UnsupportedEncodingException e )
                       {
                           throw new AeseException( e );
                       }
                    }
                }
                else
                    throw new AeseException("failed to fetch mvd "+docID );
            }
            else if ( format.equals( Formats.TEXT ) )
            {
                // just use the default key as a 'version'
                texts.put( Formats.DEFAULT, (String)doc.get(JSONKeys.BODY) );
            }
        }
        return texts;
    }
}
