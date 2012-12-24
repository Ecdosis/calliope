/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hritserver;

/**
 * Things accessible form everywhere
 * @author desmond
 */
public class Utils 
{
    /**
     * Join two paths together with a single slash
     * @param part1 the first path perhaps ending in a slash
     * @param part2 the second path perhaps starting with a slash
     * @return a single-slash joined version of path1 and path2
     */
    public static String canonisePath( String part1, String part2 )
    {
        if ( part1.length()==0 )
            return part2;
        else if ( part1.endsWith("/") )
            if ( part2.startsWith("/") )
                return part1+part2.substring(1);
            else
                return part1+"/"+part2;
        else if ( part2.startsWith("/") )
            return part1+part2;
        else
            return part1+"/"+part2;
    }
    /**
     * Separate the group from the full path
     * @param path the path to split
     */
    public static String getGroupName( String path )
    {
        int index = path.lastIndexOf("/");
        if ( index == -1 )
            return "";
        else
            return path.substring( 0, index );
    }
    /**
     * Separate the short name from the full path
     * @param path the path to split
     */
    public static String getShortName( String path )
    {
        int index = path.lastIndexOf("/");
        if ( index == -1 )
            return path;
        else
            return path.substring( index+1 );
    }
    public static String escape( String value )
    {
        StringBuilder sb = new StringBuilder();
        {
            for ( int i=0;i<value.length();i++ )
            if ( value.charAt(i) == ' ' )
                sb.append("%20");
            else if ( value.charAt(i) == '/' )
                sb.append("%2F");
            else
                sb.append( value.charAt(i) );
        }
        return sb.toString();
    }
    /**
     * Look for and get the html contained in the body element
     * @return if found, the HTML body else the original html 
     */
    public static String getHTMLBody( String html )
    {
        // find start of text after "<body>"
        int pos = html.indexOf("<body");
        if ( pos == -1 )
            pos = html.indexOf("<BODY");
        if ( pos != -1 )
            pos = html.indexOf(">",pos);
        if ( pos != -1 )
        {
            pos++;
            int rpos = html.indexOf("</body>");
            if ( rpos == -1 )
                rpos = html.indexOf("</BODY>");
            if ( rpos != -1 )
                return html.substring(pos,rpos);
        }
        return html;
    }
}
