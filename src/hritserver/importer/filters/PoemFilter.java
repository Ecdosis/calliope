/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.importer.filters;
import hritserver.importer.Archive;
import hritserver.json.JSONDocument;
import hritserver.exception.ImportException;
/**
 * Import a poem with stanzas and a title
 * @author desmond
 */
public class PoemFilter extends Filter
{
    public PoemFilter()
    {
        super();
    }
    @Override
    public void configure( JSONDocument config )
    {
        System.out.println("Poem filter: config");
    }
    /**
     * Return something for a dropdown menu
     * @return a String
     */
    @Override
    public String getDescription()
    {
        return "Poem with stanzas";
    }
    /**
     * Convert to standoff properties
     * @param input the raw text input string
     * @param cortext a cortext mvd archive
     * @param corcode a corcode mvd archive
     * @return log output
     */
    @Override
    public String convert( String input, Archive cortex, Archive corcode ) 
        throws ImportException
    {
        StringBuilder xml = new StringBuilder();
        String[] lines = input.split("\n");
        String localTitle="";
        String tempTitle="";
        int state = 0;
        for ( int i=0;i<lines.length;i++ )
        {
            lines[i] = lines[i].trim();
            switch ( state )
            {
                case 0:
                    if ( lines[i].length()>0 )
                    {
                        tempTitle = lines[i];
                        state = 1;
                    }
                    break;
                case 1:
                    if ( lines[i].length()==0 )
                    {
                        localTitle = tempTitle;
                        //setTitle( localTitle );
                        //xml.append( header );
                        xml.append("<head>");
                        //xml.append(title );
                        xml.append("</head>\n");
                        xml.append( "<lg>" );
                    }
                    else
                    {
                        //header.replace("[header]","");
                        //xml.append( header );
                        xml.append( "<lg><l>$temp_title</l>\n" );
                        xml.append( "<l>$line</l>\n" );
                    }
                    state = 2;
                    break;
                case 2:
                    if ( lines[i].length()>0 )
                        xml.append( "<l>$line</l>\n" );
                    else
                    {
                        chomp( xml );
                        xml.append( "</lg>\n" );
                        state = 3;
                    }
                    break;
                case 3:
                    if ( lines[i].length()>0 )
                    {
                        xml.append( "<lg><l>$line</l>\n" );
                        state = 2;
                    }
                    break;
            }
        }
        if ( state == 2 )
        {
            chomp( xml );
            xml.append( "</lg>\n" );
        }
        xml.append( "</body></text></TEI>\n" );
        return xml.toString();
    }
    /**
     * Do the perl chomp: remove trailing whitespace
     * @param sb the StringBuilder that's for chomping
     */
    private void chomp( StringBuilder sb )
    {
        while ( sb.length()>0 
            && Character.isWhitespace(sb.charAt(sb.length()-1)))
            sb.setLength( sb.length()-1 );
    }
}
