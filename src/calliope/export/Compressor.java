/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.export;

import java.io.File;

/**
 * Interface for compression classes like zip and tar
 * @author desmond
 */
public interface Compressor 
{
    abstract public File compress() throws Exception;
}
