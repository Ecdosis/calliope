/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.exception;

/**
 * Error parsing JSON
 * @author desmond
 */
public class JSONException extends HritException
{
    public JSONException( String message )
    {
        super( message );
    }
    public JSONException( Exception e )
    {
        super( e );
    }
}
