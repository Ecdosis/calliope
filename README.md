The folder hritserver-0.1.3 (or whatever version it has now) is a 
self-contained application suitable for installation. The supplied 
scripts work on Mac-OSX and Linux. It is based on Jetty and 
currently uses couchdb.

To set the password the script add-user.sh should be run.

There are also scripts hritserver-start.sh and hritserver-stop.sh for 
starting a stopping the hritserver application in such a way that it 
persists when you log out. These should be run as superuser. 

The install.sh script recompiles the stripper 
and formatter programs and installs them (requires sudo).

The folder "backup" within the hritserver-0.1.3 folder contains a script 
upload-all.sh for uploading the examples texts and config files. This calls 
images.sh, which can be run separately for uploading only the images. 

For these scripts to work a running instance of couchdb and the curl tool 
are required. Within the upload folder the folder config contains JSON 
configuration files arranged hierarchically. The list folder contains 
images for the catalog list view. The .json files contain samples texts 
from King Lear and perhaps in future other examples.

For uploading material in bulk use the mmpupload tool.
