/* This file is part of calliope.
 *
 *  calliope is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  calliope is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
 */
package calliope.handler;
import calliope.exception.AeseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Grand-daddy class for all handlers
 * @author ddos
 */
abstract public class AeseHandler 
{
    protected String encoding;
    
    public abstract void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws AeseException;
    /**
     * Guess the correct encoding in cases where the user has mislabelled
     * @param bytes an array of bytes
     * @return the encoding, defaulting to UTF-8
     */
    public String guessEncoding(byte[] bytes) 
    {
        org.mozilla.universalchardet.UniversalDetector detector =
            new org.mozilla.universalchardet.UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String charset = detector.getDetectedCharset();
        if ( charset == null )
            charset = checkForMac(bytes);
        if ( charset == null )
            charset = "UTF-8";
        detector.reset();
        if ( !charset.equals(encoding) ) 
            encoding = charset;
        return encoding;
    }
    private String checkForMac( byte[] data )
    {
        int macchars = 0;
        for ( int i=0;i<data.length;i++ )
        {
            if ( data[i]>=0xD0 && data[i]<=0xD5 )
            {
                macchars++;
                if ( macchars > 5 )
                    break;
            }
        }
        if ( macchars > 5 )
            return "macintosh";
        else
            return null;
    }
    
}
