/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.annotation;

import calliope.exception.AnnotationException;

/**
 * Some kind of selector
 * @author desmond
 */
public abstract class Selector 
{
    public String toString()
    {
        return super.toString();
    }
    public abstract String getId();
    abstract int end();
    abstract int start();
    abstract void updateStart( int from );
    abstract void updateLen( int len );
    abstract void update() throws AnnotationException;
}
