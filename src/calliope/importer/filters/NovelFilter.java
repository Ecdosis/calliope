/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.importer.filters;

import calliope.exception.ImportException;
import calliope.importer.Archive;
import calliope.json.JSONDocument;
import calliope.json.corcode.Range;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
 * A filter to convert novel text
 * @author desmond
 */
public class NovelFilter extends Filter
{
    MarkupSet markup;
    boolean lastWasHyphen=false;
    byte[] CR = {'\n'};
    byte[] HYPHEN = {'-'};
    byte[] SPACE = {' '};
    int written  = 0;
    @Override
    public void configure( JSONDocument config )
    {
        // implement this later
        System.out.println("Novel filter: config");
    }
    /**
     * Return something for a dropdown menu
     * @return a String
     */
    @Override
    public String getDescription()
    {
        return "Novel chapter with heading, paragraphs qnd quotations";
    }
    private void writeCurrent( ByteArrayOutputStream txt, byte[] current )
        throws IOException
    {
        if ( lastWasHyphen )
        {
            if ( current.length>0 && Character.isLetter(current[0]) )
            {
                txt.write( current );
                written += current.length;
            }
            else
            {
                txt.write( HYPHEN );
                written += HYPHEN.length;
            }
        }
        else
        {
            if ( current[current.length-1]=='-' )
            {
                txt.write( current, 0, current.length-1 );
                written += current.length-1;
            }
            else
            {
                txt.write( current );
                written += current.length;
            }
        }
        if ( current.length > 0 )
            lastWasHyphen = ( current[current.length-1]=='-' );    
    }
    /**
     * Work out if this line, which starts with a space is part of a quote
     * @param lines an array of lines
     * @param i the index of the current line
     * @return true if it's a quote else false
     */
    private boolean isQuote( String[] lines, int i )
    {
        int nIndentedLines = 0;
        for ( int j=i;j<lines.length;j++ )
        {
            if ( lines[j].length()==0 )
                return true;
            else if ( lines[j].charAt(0)!= ' ' )
                return nIndentedLines>1
                    &&Character.isUpperCase(lines[j].charAt(0));
            else 
                nIndentedLines++;
        }
        return false;
    }
    boolean isNumber( String line )
    {
        boolean res = false;
        for ( int i=0;i<line.length();i++ )
        {
            if ( !Character.isDigit(line.charAt(i)) )
            {
                res = false;
                break;
            }
            else
                res = true;
        }
        return res;
    }
    /**
     * Convert to standoff properties
     * @param input the raw text input string
     * @param name the name of the new version
     * @param cortext a cortext mvd archive
     * @param corcode a corcode mvd archive
     * @return log output
     */
    @Override
    public String convert( String input, String name, Archive cortex, 
        Archive corcode ) throws ImportException
    {
        try
        {
            ByteArrayOutputStream txt = new ByteArrayOutputStream();
            String[] lines = input.split("\n");
            int paraStart = 0;
            int quoteStart = 0;
            written = 0;
            lastWasHyphen = false;
            markup = new MarkupSet();
            int state = 0;
            for ( int i=0;i<lines.length;i++ )
            {
                boolean startsWithSpace = lines[i].startsWith(" ");
                String str = lines[i].trim();
                boolean isPageNumber = str.length()<5&&isNumber(str);
                byte[] current = str.getBytes("UTF-8");
                switch ( state )
                {
                    case 0: // looking for chapter title
                        if ( current.length>0 )
                        {
                            writeCurrent( txt, current );
                            markup.add("head",0,written);
                            writeCurrent( txt, CR );
                            paraStart = written;
                            state = 1;
                        }
                        break;
                    case 1: // paragraph
                        if ( current.length==0 )
                        {
                            if ( written>paraStart )
                            {
                                markup.add("p",paraStart, written-paraStart);
                                writeCurrent( txt, CR );
                            }
                            paraStart = written;
                        }
                        else if ( isPageNumber )
                        {
                            Range r = new Range( "pb", written, 0 );
                            r.addAnnotation( "n", lines[i] );
                            markup.add( r );
                            writeCurrent( txt, SPACE );
                        }
                        else if ( startsWithSpace && isQuote(lines,i) )
                        {
                            if ( written>paraStart )
                            {
                                markup.add("p",paraStart, written-paraStart);
                                writeCurrent( txt, CR );
                            }
                            quoteStart = written;
                            writeCurrent( txt, current );
                            state = 2;
                        }
                        else if ( startsWithSpace )
                        {
                            if ( written>paraStart )
                            {
                                markup.add("p",paraStart, written-paraStart);
                                writeCurrent( txt, CR );
                            }
                            paraStart = written;
                            writeCurrent( txt, current );
                        }
                        else // middle of paragraph
                        {
                            if ( lastWasHyphen )
                            {
                                // no dictionary lookup just yet
                                Range r = new Range( "lb", written, 0 );
                                r.addAnnotation( "break", "no" );
                                markup.add( r );
                            }
                            else if ( written > paraStart )
                            {
                                writeCurrent( txt, SPACE );
                            }
                            writeCurrent( txt, current );
                        }
                        break;
                    case 2: // actual quote
                        if ( current.length== 0 || !startsWithSpace )
                        {
                            markup.add("quote", quoteStart, 
                                written-quoteStart );
                            writeCurrent( txt, CR );
                            paraStart = written;
                            if ( current.length>0 )
                                writeCurrent( txt, current );
                            state = 1;
                        }
                        else
                        {
                            if ( !lastWasHyphen )
                                writeCurrent( txt, SPACE );
                            markup.add( "lb", written, 0 );
                            writeCurrent( txt, current );
                        }
                        break;
                }
            }
            if ( state == 1 && paraStart < written )
                markup.add( "p", paraStart, written-paraStart );
            else if ( state == 3 && quoteStart < written )
                markup.add( "quote", quoteStart, written-quoteStart );
            markup.sort();
            byte[] bytes = txt.toByteArray();
            String text = new String( bytes, "UTF-8");
            cortex.put( name, bytes );
            String json = markup.toSTILDocument().toString();
            corcode.put( name, json.getBytes() );
            return "";
        }
        catch ( Exception e )
        {
            throw new ImportException( e );
        }
    }
}
