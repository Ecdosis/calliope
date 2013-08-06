/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.importer.filters;

import calliope.exception.ImportException;
import calliope.importer.Archive;
import calliope.json.JSONDocument;
import calliope.constants.CSSStyles;
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
    byte[] CR = {'\n'};
    byte[] HYPHEN = {'-'};
    byte[] SPACE = {' '};
    int written  = 0;
    public NovelFilter()
    {
        super();
    }
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
        return "Novel chapter with heading, paragraphs and quotations";
    }
    private void writeCurrent( ByteArrayOutputStream txt, byte[] current )
        throws IOException
    {
        txt.write( current );
        written += current.length;    
    }
    /**
     * Work out if this line, which starts with a space, is part of a quote
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
    /**
     * Is this line just a series of digits?
     * @param line a line of text
     * @return true if it's a number
     */
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
            markup = new MarkupSet();
            int state = 0;
            String lastWord = "";
            String firstWord = "";
            for ( int i=0;i<lines.length;i++ )
            {
                boolean startsWithSpace = lines[i].startsWith(" ");
                String str = lines[i].trim();
                firstWord = getFirstWord( str );
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
                            if ( !lastEndsInHyphen )
                                writeCurrent( txt, SPACE );
                            continue;
                        }
                        else if ( startsWithSpace )
                        {
                            if ( isQuote(lines,i) )
                            {
                                if ( written>paraStart )
                                {
                                    markup.add("p",paraStart,written-paraStart);
                                    writeCurrent( txt, CR );
                                }
                                quoteStart = written;
                                writeCurrent( txt, current );
                                state = 2;
                            }
                            else
                            {
                                if ( written>paraStart )
                                {
                                    markup.add("p",paraStart, written-paraStart);
                                    writeCurrent( txt, CR );
                                }
                                paraStart = written;
                                writeCurrent( txt, current );
                            }
                        }
                        else // middle of paragraph
                        {
                            if ( lastEndsInHyphen )
                            {
                                if ( isHardHyphen(lastWord,firstWord) )
                                {
                                    Range r = new Range( CSSStyles.STRONG, written-1, 1 );
                                    markup.add( r );
                                }
                                else
                                {
                                    Range r = new Range( CSSStyles.WEAK, written-1, 1 );
                                    markup.add( r );
                                }
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
                            if ( lastEndsInHyphen )
                            {
                                if ( isHardHyphen(lastWord,firstWord) )
                                {
                                    Range r = new Range( "strong", written-1, 1 );
                                    markup.add( r );
                                }
                                else
                                {
                                    Range r = new Range( "weak", written-1, 1 );
                                    markup.add( r );
                                }
                            }
                            writeCurrent( txt, current );
                        }
                        break;
                }
                lastWord = getLastWord( str );
            }
            if ( state == 1 && paraStart < written )
                markup.add( "p", paraStart, written-paraStart );
            else if ( state == 3 && quoteStart < written )
                markup.add( "quote", quoteStart, written-quoteStart );
            markup.sort();
            byte[] bytes = txt.toByteArray();
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
