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

RUNNING IN AN IDE
To run in an IDE either use the supplied nbproject for Netbeans, or if 
you want to use another IDE make sure you run java LibPath to 
determine your machine's library path, and add /usr/local/lib to it.
This is because when the install.sh script is run it places two C libraries
there. These need to be found by the java runtime, and that is ONLY
possible if you tell it when you invoke it with options similar to these:
-Djava.library.path=.:/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:/usr/local/lib  -Xcheck:jni -Xmx1024m
The stuff before "/usr/local/lib" is what is output by java LibPath.

UPLOADING SAMPLE DATA
Once you have got an instance of couchdb running you need to do three things:
1) Set the password. Using curl this can be done thus:
curl -X PUT http://localhost:5984/_config/admins/admin -d '"secret"'
2) Run upload-all.sh in the backup directory to upload all the basic stuff.
3) [optional] Run mmpupload with the directory of your choice to add more stuff
