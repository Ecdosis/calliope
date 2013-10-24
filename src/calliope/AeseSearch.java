/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.it.ItalianAnalyzer;

/**
 * Manage search requests. Update Lucene index etc.
 * @author desmond
 */
public class AeseSearch 
{
    static RAMDirectory index;
    static Analyzer createAnalyzer( String langCode )
    {
        if ( langCode.equals("it") )
            return new ItalianAnalyzer(Version.LUCENE_45);
        else 
            return new StandardAnalyzer(Version.LUCENE_45);
    }
    /**
     * Build the entire Lucene index from scratch
     * @param langCode the language code
     */
    static void buildIndex( String langCode ) throws AeseException
    {
        try
        {
            String[] docIDs = Connector.getConnection().listCollection( 
                Database.CORTEX );
            Analyzer analyzer = createAnalyzer(langCode);
            AeseSearch.index = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(
                Version.LUCENE_45, analyzer);
            IndexWriter w = new IndexWriter( index, config );
            for ( int i=0;i<docIDs.length;i++ )
            {
                addCorTextoIndex( docIDs[i], w );
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Open and index an MVD file
     * @param docID the docID of the cortex
     * @param w the Lucene index-writer
     */
    static void addCorTextoIndex( String docID, IndexWriter w ) 
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
    static HashMap<String,String> getCorTex( String docID ) throws AeseException
    {
        HashMap<String,String> texts = new HashMap<String,String>();
        JSONDocument doc = null;
        byte[] data = null;
        String res = null;
        //System.out.println("fetching version "+vPath );
        try
        {
            res = Connector.getConnection().getFromDb(Database.CORTEX,docID);
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
        if ( res != null )
            doc = JSONDocument.internalise( res );
        if ( doc != null && doc.containsKey(JSONKeys.BODY) )
        {
            String format = (String)doc.get(JSONKeys.FORMAT);
            if ( format == null )
                throw new AeseException("doc missing format");
            if ( format.equals(Formats.MVD) )
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
                // use the default key as a 'version'
                texts.put( Formats.DEFAULT, (String)doc.get(JSONKeys.BODY) );
            }
        }
        return texts;
    }
}
