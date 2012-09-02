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
package hritserver.constants;

/**
 * Parameters passed in HTTP requests
 * @author desmond
 */
public class Params 
{
    /** passed-in form param base name for corcodes */
    public static String CORCODE = "CORCODE";
    /** passed-in form param base name for styles */
    public static String STYLE = "STYLE";
    /** passed-in form param base name for formats */
    public static String FORMAT = "FORMAT";
    /** the contents of an XML recipe file */
    public static String RECIPE = "RECIPE";
    /** an XML file uploaded for stripping/importing */
    public static String XML = "XML";
    /** an extra short name (for compare) */
    public static String SHORTNAME = "SHORTNAME";
    /** an extra groups path for compare */
    public static String GROUPS = "GROUPS";
    /** name of list dropdowns etc */
    public static String NAME = "NAME";
    /** calling name of javascript function */
    public static String FUNCTION = "FUNCTION";
    /** ID for currently chosen document */
    public static String DOC_ID = "DOC_ID";
    /** language for uploads */
    public static String LANGUAGE = "LANGUAGE";
    /** author for uploads */
    public static String AUTHOR = "AUTHOR";
    /** work for uploads */
    public static String WORK = "WORK";
    /** section for uploads */
    public static String SECTION = "SECTION";
     /** subsection for uploads */
    public static String SUBSECTION = "SUBSECTION";
    /** mvd version+groups for version 1 */
    public static String VERSION1 = "version1";
    /** chosen plain text filter  */
    public static String FILTER = "FILTER";
    /** chosen splitter config  */
    public static String SPLITTER = "splitter";
    /** chosen stripper config  */
    public static String STRIPPER = "stripper";
    /** chosen plain text filter config  */
    public static String TEXT = "text";
    /** offset into a version */
    public static String OFFSET = "OFFSET";
    /** length of a range in the given version */
    public static String LENGTH = "LENGTH";
    /** hide merged versions in a table */
    public static String HIDE_MERGED = "HIDE_MERGED";
    /** compact versions where possible in a table */
    public static String COMPACT = "COMPACT";
    /** expand differences to whole words in table */
    public static String WHOLE_WORDS = "WHOLE_WORDS";
    /** choose only some versions for comparison */
    public static String SOME_VERSIONS = "SOME_VERSIONS";
    /** set of selected versions if not ALL */
    public static String SELECTED_VERSIONS = "SELECTED_VERSIONS";
    /** prefix for short version (value=long version name)*/
    public static String SHORT_VERSION = "VERSION_";
}
