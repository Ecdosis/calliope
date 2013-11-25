/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.exception;

/**
 *
 * @author desmond
 */
public class AnnotationException extends AeseException
{
    public AnnotationException( Exception e )
    {
        super( e );
    }
    public AnnotationException( String mess )
    {
        super( mess );
    }
}
