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
package hritserver.json;
import hritserver.exception.JSONException;
import hritserver.json.JSONDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
/**
 * Represent a JSON document as a HashMap. Not order-preserving, 
 * except within arrays.
 * @author desmond
 */
public class JSONDocument extends HashMap<String,Object>
{
    static char[] hex = {'0','1','2','3','4','5','6','7','8','9','a',
        'b','c','d','e','f'};
    /**
     * Parse a string and return it
     * @param data the document
     * @param offse the ofset to start paring from
     * @return a string
     */
    private static JSONValue parseString( String data, int offset )
    {
        StringBuilder sb = new StringBuilder();
        int start = offset;
        while ( offset < data.length() )
        {
            if ( data.charAt(offset) == '\\' )
            {
                offset++;
                if ( offset < data.length() )
                {
                    if ( data.charAt(offset) == '"' )
                        sb.append('"');
                    else if ( data.charAt(offset)=='\\' )
                        sb.append('\\');
                    else if ( data.charAt(offset)=='/' )
                        sb.append('/');
                    else if ( data.charAt(offset)=='b' )
                        sb.append('\10');
                    else if ( data.charAt(offset)=='f' )
                        sb.append('\14');
                    else if ( data.charAt(offset)=='n' )
                        sb.append('\12');
                    else if ( data.charAt(offset)=='r' )
                        sb.append('\15');
                    else if ( data.charAt(offset)=='t' )
                        sb.append('\t');
                    else if ( data.charAt(offset)=='u' )
                    {
                        int value = 0;
                        offset++;
                        if ( offset >= data.length() )
                            break;
                        for ( int i=0;i<4;i++ )
                        {
                            char token = data.charAt(offset);
                            value *= 16;
                            if ( Character.isDigit(token) )
                                value += token-'0';
                            else if ( token >= 'a' && token <= 'f' )
                                value += token + 10 - 'a';
                            else if ( token >= 'A' && token <= 'F' )
                                value += token + 10 - 'A';
                            offset++;
                            if ( offset >= data.length() )
                                break;
                        }
                        if ( value / 65536 > 0 )
                            sb.append( (char)value / 65535);
                        sb.append( (char)(value % 65536) );
                    }
                    else if ( data.charAt(offset)=='[' )
                        sb.append('[');
                    else if ( data.charAt(offset)==']' )
                        sb.append(']');
                }
            }
            else if ( data.charAt(offset)=='"' )
            {
                offset++;
                break;
            }
            else
                sb.append( data.charAt(offset) );
            offset++;
        }
        JSONValue val = new JSONValue(sb.toString());
        val.consumed = offset - start;
        return val;
    }
    /**
     * Parse a number. We just use the Java parsing of Double and Integer
     * @param data the data to read from
     * @param offset the offset within data to start from
     * @return a JSONValue object
     */
    private static JSONValue parseNumber( String data, int offset )
    {
        int start = offset;
        boolean hasDot = false;
        JSONValue obj;
        while ( offset < data.length() 
            && !Character.isWhitespace(data.charAt(offset))
            && data.charAt(offset)!=','
            && data.charAt(offset)!='}'
            && data.charAt(offset)!=']' )
        {
            if ( data.charAt(offset)=='.' )
                hasDot = true;
            offset++;
        }
        String number = data.substring( start, offset );
        if ( hasDot )
            obj = new JSONValue(Double.parseDouble(number));
        else
            obj = new JSONValue(Integer.parseInt(number));
        obj.consumed = offset-start;
        return obj;
    }
    /**
     * Parse a pure boolean value
     * @param data the data string
     * @param offset where we start reading
     * @return a JSON value object
     */
    private static JSONValue parseBoolean( String data, int offset ) 
        throws JSONException
    {
        int start = offset;
        JSONValue obj;
        while ( offset < data.length() 
                && Character.isLetter(data.charAt(offset)) )
            offset++;
        String boolValue = data.substring(start,offset);
        if ( boolValue.toLowerCase().equals("true") )
            obj = new JSONValue(true);
        else if ( boolValue.toLowerCase().equals("false") )
            obj = new JSONValue(false);
        else 
            throw new JSONException("invalid boolean "+data);
        obj.consumed = offset-start;
        return obj;
    }
    /**
     * Parse an array: a comma-separated list of things in square brackets
     * @param data the string to read from
     * @param offset where to start reading in data
     * @return a JSONValue containing the array
     * @throws JSONException on syntax error
     */
    private static JSONValue parseArray( String data, int offset ) 
        throws JSONException
    {
        int start = offset;
        ArrayList<Object> array = new ArrayList<Object>();
        JSONValue obj = new JSONValue( array );
        while ( offset<data.length() && data.charAt(offset) != ']' )
        {
            JSONValue value = parseValue( data, offset );
            if ( value != null )
            {
                offset += value.consumed;
                array.add( value.value );
            }
            while ( offset < data.length()
                && Character.isWhitespace(data.charAt(offset)) )
                offset++;
            if ( data.charAt(offset) ==',' )
                offset++;
        }
        // point past closing bracket
        offset++;
        obj.consumed = offset-start;
        return obj;
    }
    /**
     * Parse a null value
     * @param data the data string to get it from
     * @param offset the offset in data to start from
     * @return a JSON value 
     * @thrwos JSONException on error
     */
    private static JSONValue parseNull( String data, int offset )
        throws JSONException
    {
        int start = offset;
        JSONValue obj;
        while ( offset < data.length() 
            && Character.isLetter(data.charAt(offset)) )
        offset++;
        String nullValue = data.substring(start,offset);
        if ( nullValue.toLowerCase().equals("null") )
        {
            obj = new JSONValue( null );
            obj.consumed = offset-start;
        }
        else
            throw new JSONException("expected null but found "+data );
        return obj;
    }
    /**
     * Parse a single JSON value
     * @param data
     * @param offset
     * @return a JSON value
     * @throws a JSONException on syntax error
     */
    private static JSONValue parseValue( String data, int offset ) 
        throws JSONException
    {
        JSONValue obj = null;
        int start = offset;
        while ( offset < data.length() 
            && Character.isWhitespace(data.charAt(offset)) )
            offset++;
        if ( offset < data.length() )
        {
            char token = data.charAt(offset++);
            if ( token == '"' )
            {
                obj = parseString( data, offset );
            }
            else if ( Character.isDigit(token)|| token == '-' )
            {
                offset--;
                obj = parseNumber( data, offset );
            }
            else if ( token == '{' )
            {
                obj = parseObject( data, offset );
            }
            else if ( token == '[' )
            {
                obj = parseArray( data, offset );
            }
            else if ( token == 't' || token == 'f' )
            {
                offset--;
                obj = parseBoolean( data, offset );
            }
            else if ( token == 'n' )
            {
                offset--;
                obj = parseNull( data, offset );
            }
            if ( obj != null )
                offset += obj.consumed;
        }
        obj.consumed = offset-start;
        return obj;
    }
    /**
     * Parse a JSON object recursively
     * @param data the data to parse
     * @param offset the offset we are currently reading from in data
     * @return a JSON value
     * @throws JSONException on syntax error
     */
    private static JSONValue parseObject( String data, int offset )
        throws JSONException
    {
        int start = offset;
        JSONValue key = null;
        JSONDocument doc = new JSONDocument();
        while ( offset < data.length() )
        {
            char token = data.charAt(offset++);
            if ( token == '"' )
            {
                key = parseString(data,offset);  
                offset += key.consumed;
            }
            else if ( token == ':' )
            {
                JSONValue val = parseValue(data,offset);
                doc.put( (String)key.value, val.value );
                offset += val.consumed;
            }
            else if ( token == ',' )
                key = null;
            else if ( token == '}' )
                break;
        }
        JSONValue value = new JSONValue( doc );
        value.consumed = offset-start;
        return value;
    }
    /**
     * Read in an external JSON document
     * @param src the external file
     * @param encoding its encoding, e.g. "UTF-8"
     * @return a JSON document or null
     */
    public static JSONDocument internalise( File src, String encoding )
    {
        try
        {
            FileInputStream fis = new FileInputStream( src );
            byte[] data = new byte[(int)src.length()];
            fis.read( data );
            fis.close();
            return JSONDocument.internalise( new String(data,encoding) );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
            return null;
        }
    }
    /**
     * Parse a JSON response or document
     * @param data a string
     * @return a JSON document object
     */
    public static JSONDocument internalise( String data )
    {
        try
        {
            JSONValue value = parseObject( data, 0 );
            return (JSONDocument) value.value;
        }
        catch ( Exception e )
        {
            return null;
        }
    }
    /**
     * Convert just a value to a String. Recursive.
     * @param value the JSON value
     * @return a String
     */
    private String jsonValueToString( Object value )
    {
        StringBuilder sb = new StringBuilder();
        if ( value instanceof JSONDocument 
            || value instanceof Boolean 
            || value instanceof Integer
            || value instanceof Double )
            sb.append( value.toString() );
        else if ( value instanceof String )
        {
            sb.append( "\"" );
            sb.append( value );
            sb.append( "\"" );
        }
        else if ( value == null )
            sb.append( "null" );
        else if ( value instanceof ArrayList )
        {
            ArrayList list = (ArrayList) value;
            sb.append("[ ");
            for ( int i=0;i<list.size();i++ )
            {
                sb.append( jsonValueToString(list.get(i)) );
                if ( i < list.size()-1 )
                    sb.append(",");
            }
            sb.append( " ]" );
        }
        return sb.toString();
    }
    /**
     * Convert an entire JSON document to a string. Recursive.
     * @return a String
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = keySet();
        Iterator<String> iter = keys.iterator();
        sb.append("{ ");
        while ( iter.hasNext() )
        {
            String key = iter.next();
            Object value = get( key );
            sb.append( "\"" );
            sb.append( key );
            sb.append( "\": ");
            sb.append( jsonValueToString(value) );
            if ( iter.hasNext() )
                sb.append(", ");
        }
        sb.append(" }");
        return sb.toString();
    }
    /**
     * Convert a control code to its JSON equivalent
     * @param token the offending control code
     * @return a String using \\uHHHH
     */
    private static String toJSONControlCode( char token )
    {
        if ( token == '\b' )
            return "\\b";
        else if ( token == '\f' )
            return "\\f";
        else if ( token == '\n' )
            return "\\n";
        else if ( token == '\r' )
            return "\\r";
        else if ( token == '\t' )
            return "\\t";
        else
        {
            char[] string = new char[6];
            string[0] = '\\';
            string[1] = 'u';
            int res = token;
            for ( int i=5;i>1;i-- )
            {
                string[i] = hex[res%16];
                res /= 16;
            }
            return new String( string );
        }
    }
    /**
     * Escape a string, replacing '"", "\" etc and control chars with their 
     * JSON equivalents
     * @param input the input string to convert
     * @return the escaped and JSON compatible string value
     */
    private static String escape( String input )
    {
        int reqd = input.length();
        StringBuilder sb = new StringBuilder( reqd+reqd*10/9 );
        for ( int i=0;i<input.length();i++ )
        {
            char token = input.charAt(i);
            if ( Character.isISOControl(token) )
                sb.append( toJSONControlCode(token) );
            else if ( token == '"' )
                sb.append("\\\"");
            else if ( token == '\\' )
                sb.append("\\\\");
            else if ( token == '/' )
                sb.append("\\/");
            else if ( token == ']' )
                sb.append("\\]");
            else if ( token == '[' )
                sb.append("\\[");
            else 
                sb.append( token );
        }
        return sb.toString();
    }
    /**
     * Add a new element to the document
     * @param key its key
     * @param body the body of the element
     * @param parseJSON true if the body needs parsing into a JSON document
     * @throws JSONException on syntax error
     */
    public void add( String key, String body, boolean parseJSON ) 
        throws JSONException
    {
        if ( parseJSON )
        {
            JSONValue value = JSONDocument.parseObject(body, 0);
            put( key, value.value );
        }
        else    // it's a string, perhaps XML
        {
            put( key, JSONDocument.escape(body) );
        }
    }
    /**
     * Test program
     * @param args first: name of the directory for example JSON files
     */
    public static void main( String[] args )
    {
        try
        {
            if ( args.length == 1 )
            {
                File dir = new File( args[0] );
                String[] list = dir.list();
                for ( int i=0;i<list.length;i++ )
                {
                    File f = new File(dir,list[i]);
                    FileInputStream fis = new FileInputStream( f );
                    int len = (int)f.length();
                    byte[] data = new byte[len];
                    fis.read( data );
                    JSONDocument doc = JSONDocument.internalise( 
                        new String(data,"UTF-8") );
                    System.out.println( doc.toString() );
                    fis.close();
                }
            }
            else
                System.out.println("need name of directory for JSON examples");
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }
    }
}
