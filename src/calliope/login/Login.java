/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calliope.login;
import calliope.exception.LoginException;

/**
 * Abstract login services
 * @author desmond
 */
public interface Login 
{
    /** 
     * Login to a remote service of some kind
     * @param host the host's domain-name - the subclass will insert protocol
     * @param user the user name
     * @param password the user password
     * @return the login response or cookie
     */
    abstract public String login( String host, String user, String password ) 
        throws LoginException;
    abstract public void logout( String host, String cookie ) 
        throws LoginException;
}
