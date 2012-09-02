/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests.html;

/**
 * Represent a literal section of HTML enclosed in comments
 * @author desmond
 */
public class HTMLComment extends HTMLLiteral
{
    public String toString()
    {
        return "<!--"+super.toString()+"-->";
    }
}
