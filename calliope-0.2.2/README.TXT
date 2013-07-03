Installation on MacOSX 
1. Homebrew

Homebrew can be fetched from http://mxcl.github.com/homebrew/. Follow the 
instructions there.

2. gcc

To build the C libraries used by calliope you will first have to install gcc:

brew tap homebrew/dupes
brew install apple-gcc42

3. Install mongodb

The command is

brew update
brew install mongodb

4. Set up databases

You need to first download the calliope package, available from 
www.github.com/AESE-Infrastructure. Click the "download as .zip or .tar.gz"
If it does not unpack automatically unzip it thus:

unzip calliope-master.zip 
OR 
tar xzvf calliope-master.tar.gz

Now move the folder to a convenient location:

cd ~/Downloads
mv calliope-master ~/calliope
cd ~/calliope/calliope-0.2.2

Database setup

mongo
db = db.getSiblingDB('admin')
db.addUser( { user: "admin", pwd: "jabberw0cky" } )
exit

Check that mongodb is running:

ps aux | grep mongodb

You should get two process numbers, one 1-line long (that's the command 
you just ran) and a longer one about 6 lines long. That's mongodb running.

In the browser check that mongodb is running. Navigate to 
http://localhost:28017/ There should be a web interface to mongodb 
there.

5. Run the installer

sudo ./install.sh
sudo ./calliope-start.sh

Hit return. The service will run even when you log off. To stop the 
service, log in as the same user who launched it initially and type 
(but don't do this now):

sudo ./calliope-stop.sh

If any of these scripts complain that you don't have permission to 
execute them just add executable privileges:

chmod +x install.sh
chmod +x calliope-start.sh
chmod +x calliope-stop.sh

Uploading sample data

calliope needs some sample data to run (explicitly the kinglear example)
To upload this go to the pdef-tool folder and type:

pdef-tool archive

This will upload all the data needed by the service to run.

Now install the test databases:

pdef-tool archive

This should upload all the files in archive to you localhost installation of calliope.
The pdef-tool can also be used to upload or download stuff in batches. See man pdef-tool 
for details.

ADDING OTHER TEXTS

The Import dialog can be used to import XML or plain text files. For 
plain text to work there must be a suitable import filter. XML conforming 
to the TEI-Lite schema should import correctly if the TEI/default style 
is chosen. As a security precaution a password is required. This is set to 
"brillig" (without quotation marks).

