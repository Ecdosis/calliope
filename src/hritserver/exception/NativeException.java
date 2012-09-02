/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.exception;

/**
 * Handle an exception arising from a native code fault
 * @author desmond
 */
public class NativeException extends HritException
{
    public NativeException( String message )
    {
        super( message );
    }
}
