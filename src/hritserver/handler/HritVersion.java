/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  hritserver is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
 */
package hritserver.handler;

/**
 * Represent a version returned from an MVD
 * @author desmond
 */
public class HritVersion 
{
    /** format the text is in */
    String format;
    /** actual text of the version */
    byte[] version;
    /** if CorCode the default style */
    String defaultStyle;
    /**
     * Set the format of this version
     * @param format the format to set
     */
    public void setFormat( String format )
    {
        this.format = format;
    }
    /**
     * Set the default style of this version
     * @param style the style to set
     */
    public void setStyle( String style )
    {
        this.defaultStyle = style;
    }
    /**
     * Set the version data of this version
     * @param format the version contents to set
     */
    public void setVersion( byte[] version )
    {
        this.version = version;
    }
     /**
     * Set the format of this version
     * @param format the format to set
     */
    public String getFormat()
    {
        return format;
    }
    /**
     * Set the version data of this version
     * @param format the version contents to set
     */
    public byte[] getVersion()
    {
        return version;
    }
    /**
     * Get the default style of this version
     * @return the default style if any
     */
    public String getStyle()
    {
        return this.defaultStyle;
    }
}
