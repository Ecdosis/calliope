NAME

       pdef‐tool ‐ uploads or downloads a digital scholarly edition


SYNOPSIS

       Uploading: pdef‐tool <source‐folder>

       Downloading:

       ‐h <host> the url for download (defaults to http://localhost:8080/)

       ‐f <formats> a comma‐separated list of TEXT,XML,MVD,MIXED (defaults to
       MVD)

       ‐d <docid> the  prefix  of  a  docid  as  a  regular  expression,  e.g.
       english/poetry.* (defaults to ".*")

       ‐n <name> the name of the archive to download (defaults to archive)

       ‐z  <zip-type>  specifies the type of zip archive, either tar_gz or zip
       (defaults to tar_gz)

       ‐r download required corforms and all configs on server


DESCRIPTION

       pdef‐tool is used to upload  or  download  digital  scholarly  editions
       using  the  PDEF (portable digital edition format). A PDEF archive is a
       specially formatted collection of nested folders and files.

       Some folders begin with metacharacters, which are used to  specify  the
       docids of the data on the server. Paths may be literal or relative.


CONFIG files

       Config  files,  ending  in  ".conf" are JSON files containing key‐value
       pairs as described below. A config file’s values apply to the directory
       in which it occurs and also to any subordinate folders.


LITERAL PATHS

       A folder name beginning with ’@’ designates a literal path. The folder‐
       name minus the ’@’ designates the database collection to which the con‐
       tained  files  will be uploaded. The remaining folders and files nested
       within a literal path folder form the docids. e.g. a file in  a  folder
       structure:

       capuana/@config/stripper/play/italian/capuana.json

       will upload the file "capuana.json" to the database collection "config"
       with the docid "stripper/play/italian/capuana.json". Literal paths  are
       useful for specifying images, configs and corforms.


RELATIVE PATHS

       These begin with "+" and end with "%", so the directory path:

       archive/+english/shakespeare/kinglear/act1/%scene1/

       will    upload    its    contents    to   the   docid   "english/shake‐
       speare/kinglear/act1/scene1".

       Relative paths contain at least one subordinate  folder  called  "MVD",
       "TEXT", "XML" or "MIXED". Their formats are as follows:


MVD folders

       An MVD folder contains one cortex.mvd file containing all the text ver‐
       sions at that docid. It also must contain a folder "corcode" containing
       all  the corcodes of that cortex, for example the file "default" in the
       corcode sub‐folder would be the default corcode for that cortex.

       Other information about the corcode or cortex may  be  contained  in  a
       .conf  file with the same name as the cortex/corcode, e.g. for the cor‐
       tex.mvd file the file cortex.conf would be a JSON file containing  keys
       about  the  document. The following keys are recognised for cortexs and
       corcodes:

       author: The author’s name

       title: The title of the work

       style: the docid of the desired corform

       format: One of "TEXT" (cortexs) or "STIL" (corcodes)

       section: The section of the document this MVD refers to  e.g.  "Act  1,
       scene 1"

       version1: The short ID of the first version to display by default, e.g.
       "/Base/F1". (Version IDs always start with a slash, docids do not)


TEXT folders

       These contain files whose names will be used to compose the  short‐ver‐
       sion  names. If there are subordinate folders with a TEXT folder, these
       are used to specify group‐names. A versions.conf file may  be  used  to
       specify  the long names of these short‐names. This consists of a single
       array keyed with the tag "versions". The array consists of objects with
       the  keys  "key"  and "value", where "key" refers to the version short‐
       name and "value" to its long name. The short‐names must be the same  as
       the file‐names.


XML folders

       May  be specified as per TEXT folders, but extra config keys are recog‐
       nised to facilitate import. In addition  to  the  versions  key,  other
       recognised keys are:

       corform:  specifies  the docid of a corform file (a CSS file wrapped in
       JSON) as the default format for files in this and child directories.

       stripper: specifies the docid of  a  stripper  config  file  to  direct
       stripping of markup from files in this and in child directories.

       splitter:  specifies  the  docid of the splitter config to use for this
       and all child directories.

       filter: designates the name of a Java filter program  to  be  used  for
       filtering text files.


Other config keys

       At the topmost level a PDEF archive should contain a .conf file with at
       least

       base_url: The url to upload to, e.g. http://localhost:8080/


EXAMPLE

       pdef‐tool archive

       pdef‐tool ‐a shakespeare ‐d "english/shakespeare/*"

       (the quotes are required to get around substitution by bash)


                                   29‐5‐2013                      pdef‐tool(1)
