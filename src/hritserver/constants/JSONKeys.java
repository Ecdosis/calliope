/* This file is part of hritserver.
 *
 *  hritserver is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
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
package hritserver.constants;

/**
 *
 * @author desmond
 */
public class JSONKeys 
{
    /** JSON document key for body */
    public static String BODY = "body";
    /** JSON document key for style */
    public static String STYLE = "style";
    /** JSON document key for name */
    public static String NAME = "name";
    /** JSON document key for name */
    public static String RELOFF = "reloff";
    /** JSON document key for format */
    public static String FORMAT = "format";
    /** JSON document key for length */
    public static String LEN = "len";
    /** if the range is removed */
    public static String REMOVED = "removed";
    /** JSON document key for content */
    public static String CONTENT = "content";
    /** JSON document key for annotations */
    public static String ANNOTATIONS = "annotations";
    /** JSON document key for ranges array */
    public static String RANGES = "ranges";
    /** array of formats returned by HritStripper etc */
    public static String FORMATS = "formats";
    /** ID for child of parent */
    public static String CHILDID = "childid";
    /** ID for parent */
    public static String PARENTID = "parentid";
    /** description of an MVD */
    public static String DESCRIPTION = "description";
    /** short name for a version */
    public static String VERSION_SHORT = "version-short";
    /** long name for a version */
    public static String VERSION_LONG = "version-long";
    /** group name */
    public static String GROUP = "group";
    /** top group */
    public static String TOPGROUP = "top-group";
    /** an empty range */
    public static String EMPTY = "empty";
    /** list of element in select etc */
    public static String LIST = "list";
    /** name of function for javascript etc */
    public static String FUNCTION = "function";
    /** generic ID */
    public static String ID = "id";
    /** rows of documents returned by _all_docs */
    public static String ROWS = "rows";
    /** key for document in _all_docs */
    public static String KEY = "key";
    /** specification of version1 key on version dropdown */
    public static String VERSION1 = "version1";
    /** title of cortex */
    public static String TITLE = "title";
    /** author of cortex */
    public static String AUTHOR = "author";
    /** work section */
    public static String SECTION = "section";
    /** work subsection */
    public static String SUBSECTION = "subsection";
}
