/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */

package hritserver.json.corcode;
import hritserver.exception.JSONException;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Random;
import hritserver.json.JSONDocument;
import java.io.File;
import java.io.FileInputStream;

/**
 * Parse a CorCode bit by bit, preserving internal state between calls.
 * @author desmond
 */
public class ProgressiveParser 
{
    /** current absolute offset of last fully-defined range */
    int currentOffset;
    /** represents internal state of parser */
    int state;
    /** notify this object when a range has been read */
    RangeComplete rc;
    /** current key being built up */
    StringBuilder currentKey;
    /** current literal value being built up */
    StringBuilder currentValue;
    /** hex representation of char being built up */
    StringBuilder currentHexChar;
    /** Current object being built (JSONDocument or ArrayList<Object>) */
    Object currentObject;
    /** stack of nested objects */
    Stack<Object> stack;
    /**
     * Construct a progressive parser
     * @param rc inform this as we read each range
     */
    public ProgressiveParser( RangeComplete rc )
    {
        this.rc = rc;
        this.stack = new Stack<Object>();
    }
    /**
     * Process a single hex char in a key string
     * @param token the token being processed
     * @param start the state to start in
     * @param end the next state after reading the hex string
     * @return the new state (or the same one)
     * @throws JSONException if the syntax was wrong
     */
    int readHex( char token, int start, int end ) throws JSONException
    {
        int s = start;
        if ( Character.isDigit(token)
            || (token<='F'&&token>='A') 
            || (token>='a'&&token<='f') )
        {
            if ( currentHexChar == null )
                currentHexChar = new StringBuilder();
            if ( currentHexChar.length() == 3 )
            {
                currentHexChar.append( token );
                int value = Integer.parseInt(
                    currentHexChar.toString(),16);
                currentKey.append( (char)value );
                currentHexChar = null;
                s = end;
            }
            else
                currentHexChar.append( token );
        }
        else
            throw new JSONException("invalid hex char "+token );
        return s;
    }
    /** 
     * Process a single escaped character in a key name
     * @param token the character
     * @param hex the state to enter for escaped hex chars
     * @param end the state to enter when we're finished
     * @return the new state
     */
    int readEscaped( char token, int hex, int end )
    {
        int s = end;
        if ( currentKey == null )
            currentKey = new StringBuilder();
        if ( token == '"' )
            currentKey.append( '"' );
        else if ( token == '\\' )
            currentKey.append('\\');
        else if ( token == 'b' )
            currentKey.append( '\b' );
        else if ( token == 'f')
            currentKey.append('\f');
        else if ( token == 'n')
            currentKey.append('\n');
        else if ( token == 'r')
            currentKey.append('\r');
        else if ( token == 't')
            currentKey.append('\t');
        else if ( token == 'u' )
            s = hex;
        return s;          
    }
    /**
     * Look for a colon or comma
     * @param token the token in question
     * @punctuation the punctuation (: or ,) to look for
     * @param start the state to stay in if no punctuation
     * @param end the state to move to if punctuation found
     * @return the new state
     */
    int readPunctuation( char token, char punctuation, int start, int end )
    {
        int s = start;
        if ( token == punctuation )
            s = end;
        else if ( !Character.isWhitespace(token) )
        {
            s = -1;
            System.out.println("expected "+punctuation+" but found "+token);
        }
        return s;
    }
    /*
     * Add the currently defined key, value string to the current object
     */
    @SuppressWarnings("unchecked")
    void addStringToCurrent()
    {
        if ( currentObject instanceof JSONDocument )
        {
            JSONDocument doc = (JSONDocument) currentObject;
            doc.put( currentKey.toString(), currentValue.toString() );
            currentKey = currentValue = null;
        }
        else if ( currentObject instanceof ArrayList )
        {
            ArrayList<Object> array = (ArrayList<Object>) currentObject;
            array.add( currentValue.toString() );
            currentValue = null;
        }
    }
    /** 
     * Read one char in a value string
     * @param token the char in question
     * @param start stay in this state unless string ends or error
     * @param escape go to this state for escaped chars
     * @param end select this state when you've parsed the whole string
     * @return the new state
     */
    int readValueString( char token, int start, int escape, int end )
    {
        int s = start;
        if ( token == '\\' )
            s = escape;
        else if ( token == '"' )
        {
            addStringToCurrent();
            s = end;
        }
        else 
        {
            if ( currentValue == null )
                currentValue = new StringBuilder();
            currentValue.append( token );
        }
        return s;
    }
    /** 
     * Read one char in a key string
     * @param token the char in question
     * @param start start in this state
     * @param escape go this this state for escaped chars
     * @param end select this state when you've parsed the string
     * @return the new state
     */
    int readKeyString( char token, int start, int escape, int end )
    {
        int s = start;
        if ( token == '\\' )
            s = escape;
        else if ( token == '"' )
            s = end;
        else 
        {
            if ( currentKey == null )
                currentKey = new StringBuilder();
            currentKey.append( token );
        }
        return s;
    }
    /**
     * Read the first character of a value that decides 
     * what value type it is
     * @param token the token in question
     * @param start the state to stay in when you see white space
     * @param string the state to go into when you see a string
     * @param nullState the state to go into when you see null
     * @param trueState the state to go into when you see true
     * @param falseState the state to go into when you see false
     * @param array the state to go into when you see an array
     * @param object the state to go into when you see an object
     * @param number the state to go into when you see a number
     * @return the new state
     * @throws a JSONExcpetion if there was a syntax error
     */
    @SuppressWarnings("unchecked")
    int readValueStart( char token, int start, int string, int nullState, 
        int trueState, int falseState, int array, int object, int number )
            throws JSONException
    {
        int s = start;
        if ( Character.isWhitespace(token) )
            return s;
        else if ( token == '"' )
        {
            // create value immediately because it might be empty
            currentValue = new StringBuilder();
            s = string;
        }
        else if ( token == '[' )
        {
            stack.push( currentObject );
            if ( currentObject instanceof ArrayList )
            {
                ArrayList list = (ArrayList)currentObject;
                currentObject = new ArrayList<Object>();
                list.add( currentObject );
            }
            else
            {
                JSONDocument doc = (JSONDocument) currentObject;
                currentObject = new ArrayList<Object>();
                doc.put( currentKey.toString(), currentObject );
                currentKey = null;
            }
            s = array;
        }
        else if ( token == '{' )
        {
            stack.push( currentObject );
            if ( currentObject instanceof ArrayList )
            {
                ArrayList<Object> list = (ArrayList<Object>)currentObject;
                currentObject = new JSONDocument();
                list.add( currentObject );
            }
            else
            {
                JSONDocument doc = (JSONDocument) currentObject;
                currentObject = new JSONDocument();
                doc.put( currentKey.toString(), currentObject );
                currentKey = null;
            }
            s = object;
        }
        else if ( token == 'n' )
        {
            currentValue = new StringBuilder();
            currentValue.append( token );
            s = nullState;
        }
        else if ( token == 't' )
        {
            s = trueState;
            currentValue = new StringBuilder();
            currentValue.append( token );
        }
        else if ( token == 'f' )
        {
            currentValue = new StringBuilder();
            currentValue.append( token );
            s = falseState;
        }
        else if ( Character.isDigit(token)|| token=='-' )
        {
            currentValue = new StringBuilder();
            currentValue.append( token );
            s = number;
        }
        else 
        {
            throw new JSONException("expected value but found "+token );
        }
        return s;
    }
    /**
     * Read a literal value and assign to the currentObject
     * @param token the token to read
     * @param literal the literal string to match against
     * @param obj the object to store if matched
     * @param start the state for reading the literal
     * @param end the state after we've read true correctly
     * @return the next state
     * @throws a JSONException if it failed
     */
    @SuppressWarnings("unchecked")
    int readLiteral( char token, String literal, Object obj, int start, 
        int end ) throws JSONException
    {
        int s = start;
        if ( !Character.isWhitespace(token) )
        {
            if ( currentValue == null )
                currentValue = new StringBuilder();
            currentValue.append( token );
        }
        else
        {
            if ( currentValue.toString().equals(literal) )
            {
                if ( currentObject instanceof ArrayList )
                    ((ArrayList<Object>)currentObject).add( obj );
                else if ( currentObject instanceof JSONDocument )
                {
                    ((JSONDocument)currentObject).put(currentKey.toString(), 
                        obj);
                    currentKey = null;
                }
                currentValue = null;
            }
            else
            {
                throw new JSONException("expected "+literal+" but found "
                    +currentValue.toString());
            }
            s = end;
        }
        return s;
    }
    /**
     * Store a number saved in currentValue
     * @throws JSONException 
     */
    @SuppressWarnings("unchecked")
    void storeNumber() throws JSONException
    {
        try
        {
            Object obj;
            if ( currentValue.indexOf(".") != -1 )
                obj = Float.parseFloat(currentValue.toString() );
            else
                obj = Integer.parseInt(currentValue.toString() );
            if ( currentObject instanceof ArrayList )
                ((ArrayList<Object>)currentObject).add( obj );
            else if ( currentObject instanceof JSONDocument )
            {
                ((JSONDocument)currentObject).put(currentKey.toString(), 
                    obj);
                currentKey = null;
            }
            currentValue = null;
        }
        catch ( Exception e )
        {
            throw new JSONException( e );
        }
    }
    /**
     * Store a literal object
     * @param thing the thing to store
     * @throws JSONException 
     */
    @SuppressWarnings("unchecked")
    void storeLiteral( Object thing ) throws JSONException
    {
        try
        {
            if ( currentObject instanceof ArrayList )
            {
                ((ArrayList)currentObject).add( thing );
            }
            else if ( currentObject instanceof JSONDocument )
            {
                ((JSONDocument)currentObject).put(currentKey.toString(), 
                    thing);
                currentKey = null;
            }
            currentValue = null;
        }
        catch ( Exception e )
        {
            throw new JSONException( e );
        }
    }
    /**
     * Read a number value and assign to the currentObject
     * @param token the token to read
     * @param start the state for reading the number
     * @param end the state after we've read true correctly
     * @return the next state
     */
    int readNumber( char token, int start, int end ) throws JSONException
    {
        int s = start;
        if ( Character.isWhitespace(token) )
        {
            storeNumber();
            s = end;
        }
        else
        {
            if ( currentValue == null )
                currentValue = new StringBuilder();
            currentValue.append( token );
        }
        return s;
    }
    /**
     * Read the next item in a comma-separated list
     * @param token the token being read
     * @param start the state to stay in if token is whitespace
     * @param array go to this state to keep parsing an array
     * @param object go to this state to keep parsing an object
     * @param objComma state to look for commas in an object
     * @param arrayComma state to look for commas in an array
     * @return the next state
     * @throws JSONException 
     */
    int readNextItem( char token, int start, int array, int object, 
        int objComma, int arrayComma ) throws JSONException
    {
        int s = start;
        if ( Character.isWhitespace(token) )
            return s;
        else if ( token == ',' )
        {
            if ( currentObject instanceof ArrayList )
                s = array;
            else
                s = object;
        }
        else if ( (currentObject instanceof JSONDocument && token == '}') 
            || (currentObject instanceof ArrayList && token == ']') )
        {
            Object prevObject = currentObject;
            if ( stack.empty() )
            {
                currentObject = null;
                s = -1; // ordinary end of document, no exception
            }
            else 
            {
                currentObject = stack.pop();
                if ( currentObject instanceof ArrayList )
                {
                    if ( prevObject instanceof JSONDocument )
                    {
                        JSONDocument range = (JSONDocument) prevObject;
                        if ( range.containsKey("reloff") 
                            && range.containsKey("len") )
                        {
                            try
                            {
                                int reloff = ((Integer)range.get("reloff")).intValue();
                                int len = ((Integer)range.get("len")).intValue();
                                currentOffset += reloff;
                                this.rc.rangeComplete( currentOffset, len );
                            }
                            catch ( Exception e )
                            {
                                throw new JSONException( e );
                            }
                        }
                    }
                    s = arrayComma;
                }
                else
                    s = objComma;
            }
        }
        else 
            throw new JSONException(
                "expected } or , but found "+token );
        return s;
    }
    /** 
     * Parse a typically short section of character data
     * @param data the data to parse
     * @return true if the data contained a string value
     * @throws JSONException if there was a syntax error
     */
    public boolean parseData( char[] data ) throws JSONException
    {
        boolean readStringValue = false;
        for ( int i=0;i<data.length;i++ )
        {
            if ( state == -1 )
                break;
            switch ( state )
            {
                case 0: // looking for left brace of outermost object
                    if ( data[i]=='{' )
                    {
                        currentObject = new JSONDocument();
                        state = 1;
                    }
                    else if ( !Character.isWhitespace(data[i]) )
                    {
                        System.out.println(
                            "invalid start to JSON document: "+data[i]);
                        state = -1;
                    }
                    break;
                case 1:// looking for string-start in key name
                    if ( data[i] == '"' )
                        state = 2;
                    else if ( !Character.isWhitespace(data[i]) )
                    {
                        System.out.println("invalid start to key string: "
                            +data[i]);
                        state = -1;
                    }
                    break;
                case 2:// reading string in key name
                    state = readKeyString( data[i], 2, 3, 5 );
                    break;
                case 3: // escaped character in key name
                    state = readEscaped( data[i], 4, 2 );
                    break;
                case 4: // reading hex value in key string
                    state = readHex( data[i], 4, 2 );
                    break;
                case 5: // looking for colon
                    state = readPunctuation( data[i], ':', 5, 6 );
                    break;
                case 6: // reading first char of value in object
                    state = readValueStart( data[i], 6, 7, 10, 11, 12, 15, 
                        1, 13 );
                    break;
                case 7: // reading value string in object
                    state = readValueString( data[i], 7, 8, 14 );
                    readStringValue = true;
                    break;
                case 8: // reading escaped char in value string in object
                    state = readEscaped( data[i], 9, 7 );
                    break;
                case 9: // reading hex in value string in object
                    state = readHex( data[i], 9, 7 );
                    break;
                case 10: // read null in object
                    if ( data[i] == ',' || data[i] == '}' )
                    {
                        storeLiteral( null );
                        state = readNextItem( data[i], 14, 15, 1, 14, 23 );
                    }
                    else
                        state = readLiteral( data[i], "null", null, 10, 14 );
                    break;
                case 11:    // read true in object
                    if ( data[i] == ',' || data[i] == '}' )
                    {
                        storeLiteral( true );
                        state = readNextItem( data[i], 14, 15, 1, 14, 23 );
                    }
                    else
                        state = readLiteral( data[i], "true", true, 11, 14 );
                    break;
                case 12:    //read false in object
                    if ( data[i] == ',' || data[i] == '}' )
                    {
                        storeLiteral( false );
                        state = readNextItem( data[i], 14, 15, 1, 14, 23 );
                    }
                    else
                        state = readLiteral( data[i], "false", false, 12, 14 );
                    break;
                case 13:    // reading number in object
                    if ( data[i] == '}' || data[i] == ',' )
                    {
                        storeNumber();
                        state = readNextItem( data[i], 14, 15, 1, 14, 23 );
                    }
                    else
                        state = readNumber( data[i], 13, 14 );
                    break;
                case 14: // reading comma in object
                    state = readNextItem( data[i], 14, 15, 1, 14, 23 );
                    break;
                case 15: // reading a value in an array
                    state = readValueStart( data[i], 15, 16, 19, 20, 21, 15, 
                        1, 22 );
                    break;
                case 16: // reading value string in array
                    state = readValueString( data[i], 16, 17, 23 );
                    readStringValue = true;
                    break;
                case 17: // reading escaped char in value string in array
                    state = readEscaped( data[i], 18, 16 );
                    break;
                case 18: // reading hex in value string in array
                    state = readHex( data[i], 18, 16 );
                    break;
                case 19: // read null in array
                    if ( data[i] == ']' || data[i] == ',' )
                    {
                        storeLiteral( null );
                        state = readNextItem( data[i], 23, 15, 1, 14, 23 );
                    }
                    else
                        state = readLiteral( data[i], "null", null, 19, 23 );
                    break;
                case 20:    // read true in array
                    if ( data[i] == ']' || data[i] == ',' )
                    {
                        storeLiteral( true );
                        state = readNextItem( data[i], 23, 15, 1, 14, 23 );
                    }
                    else
                        state = readLiteral( data[i], "true", true, 20, 23 );
                    break;
                case 21:    //read false in array
                    if ( data[i] == ']' || data[i] == ',' )
                    {
                        storeLiteral( false );
                        state = readNextItem( data[i], 23, 15, 1, 14, 23 );
                    }
                    else
                        state = readLiteral( data[i], "false", false, 21, 23 );
                    break;
                case 22:    // reading number in array
                    if ( data[i] == ']' || data[i] == ',' )
                    {
                        storeNumber();
                        state = readNextItem( data[i], 23, 15, 1, 14, 23 );
                    }
                    else
                        state = readNumber( data[i], 22, 23 );
                    break;
                case 23:    // looking for comma in array
                    state = readNextItem( data[i], 23, 15, 1, 14, 23 );
                    break;
            }
        }
        return readStringValue;
    }
    /**
     * Did this parser instance encounter an error?
     * @return true if it did, else false
     */
    boolean inError()
    {
        return state == -1;
    }
    /**
     * What is the current absolute offset that the ranges specified?
     * @return an int
     */
    int getAbsoluteOffset()
    {
        return currentOffset;
    }
    /**
     * Test routine
     * @param args first is name of JSON file
     */
    public static void main( String[] args )
    {
        if ( args.length==1 )
        {
            File testFolder = new File( args[0] );
            File[] files = testFolder.listFiles();
            RangeReceiver rr = new RangeReceiver();
            for ( int i=0;i<files.length;i++ )
            {
                try
                {
                    ProgressiveParser pp = new ProgressiveParser(rr);
                    FileInputStream fis = new FileInputStream( files[i] );
                    int len = (int)files[i].length();
                    if ( len > 100 )
                    {
                        byte[] data = new byte[len];
                        fis.read( data );
                        fis.close();
                        Random r = new Random();
                        int pos = 0;
                        int den = (int)Math.round(Math.log(len));
                        String text = new String( data );
                        while ( pos < len )
                        {
                            int end = pos + r.nextInt( len/den );
                            if ( end > len )
                                end = len;
                            if ( end > pos )
                            {
                                char[] dst = new char[end-pos];
                                text.getChars( pos, end, dst, 0 );
                                pp.parseData( dst );
                                pos = end;
                            }
                        }
                        System.out.println("Parsed "+files[i].getName()
                            +" correctly");
                    }
                    else
                        System.out.println("ignoring "
                            +files[i].getAbsolutePath()+" because too short");
                }
                catch ( Exception e )
                {
                    e.printStackTrace( System.out );
                }
            }
        }
        else
            System.out.println("usage: java ProgressiveParser <json-src>");
        
    }
}
