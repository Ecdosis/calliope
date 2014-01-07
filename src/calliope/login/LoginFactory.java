/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.login;
import calliope.exception.LoginException;
/**
 * Create a actual login class based on its type
 * @author desmond
 */
public class LoginFactory 
{
    /** 
     * Empty constructor. 
     * @param type the login type - should have a corresponding class
     * @return the contructed Login object
     * @throws LoginException if the type was not found
     */
    public static Login createLogin( LoginType type ) throws LoginException
    {
        switch ( type )
        {
            case DRUPAL:
                return new DrupalLogin();
            default:
                throw new LoginException("Unknown login type "+type);  
        }
    }
}
