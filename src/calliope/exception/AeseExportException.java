/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.exception;

/**
 *
 * @author desmond
 */
public class AeseExportException extends AeseException
{
    public AeseExportException( String message )
    {
        super( message );
    }
    public AeseExportException( Exception e )
    {
        super( e );
    }
}
