/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.exception;

/**
 * Represent an exception during import
 * @author desmond
 */
public class ImportException extends HritException
{
    public ImportException( String message )
    {
        super( message );
    }
    public ImportException( Exception e )
    {
        super( e );
    }
}
