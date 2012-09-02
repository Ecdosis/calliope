/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver.tests;

/**
 *
 * @author desmond
 */
/**
 * simple uri encoder, made from the spec at:
 * http://www.ietf.org/rfc/rfc2396.txt
 * Feel free to copy this. I take no responsibility for anything, ever.
 * @author Daniel Murphy
 */
public class URLEncoder 
{
	private static String mark = "-_.!~*'()\"";
    /**
     * Add a get param to a get URL
     * @param url the url as a string
     * @param key the key of the argument
     * @param value its value
     * @return the encoded url with the param
     */
 	public static String addGetParam( String url, String key, String value )
	{
		String sep = (url.lastIndexOf('&')==-1)?"?":"&";
		return url+sep+encodeParam(key)+"="+encodeParam(value);
	}
    private static String encodeParam( String arg ) 
	{
        StringBuilder url = new StringBuilder(); // Encoded URL
        // thanks Marco!
 
        char[] chars = arg.toCharArray();
        for(int i = 0; i<chars.length; i++) {
            char c = chars[i];
            if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
                url.append(c);
            }
            else {
                url.append("%");
                url.append(Integer.toHexString((int)c));
            }
        }
        return url.toString();
    }
}
