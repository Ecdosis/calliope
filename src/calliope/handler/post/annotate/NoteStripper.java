/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.annotate;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
/**
 * Strip notes from a TEI-XML file 
 * @author desmond
 */
public class NoteStripper {
    SaxParser sp;
    public String strip( String vid, byte[] data ) throws Exception
    {
        sp = new SaxParser( vid );
        sp.digest( data );
        return sp.getBody();
        //System.out.println( sp.toString() );
    }
    public ArrayList<Annotation> getNotes()
    {
        return sp.getNotes();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if ( args.length==3 )
        {
            try
            {
                NoteStripper ns = new NoteStripper();
                File f = new File( args[1] );
                if ( f.exists() )
                {
                    FileInputStream fis = new FileInputStream(f);
                    byte[] data = new byte[(int)f.length()];
                    fis.read(data);
                    fis.close();
                    ns.strip(args[0], data);
                }
                else
                    System.out.println("File "+args[0]+" not found");
            }
            catch ( Exception e )
            {
                e.printStackTrace(System.out);
            }
        }
        else
            System.out.println("usage: java NoteStripper <vid> <xml-file>\n"
                + "e.g. java NoteStripper /Base/F1 "
                +"/act1/scene1 kinglear.xml");
    }
}
