Installation on MacOSX 
1. Homebrew

Homebrew can be fetched from http://mxcl.github.com/homebrew/. Follow the 
instructions there.

2. gcc

To build the C libraries used by calliope you will first have to install gcc:

brew tap homebrew/dupes
brew install apple-gcc42

3. Install couchdb

The command is

brew install couchdb
4. Set up databases

You need to first download the calliope package, available from 
www.github.com/AustESE-Infrastructure. Click on the calliope repository, 
then the Downloads tab. Select "calliope-0.2.0.tar.gz" and download it 
(Don't click on "Download as .tar.gz" or .zip). When it is on you hard 
disk it will unpack automatically or by double-clicking on it or on the 
commandline (OSX removes the .gz automatically):

tar xvf calliope-0.2.0.tar
Now move the folder to a convenient location:

cd ~/Downloads
mv calliope-0.2.0 ~
cd ~/calliope-0.2.0

First set up the database for admin access. In the calliope-0.2.0 
folder are two scripts add-user.sh and couchdb.sh Run this command first:

sudo ./couchdb.sh

After running this you have to press return to get back the prompt. Check 
that couchdb is running:

ps aux | grep couchdb

You should get two process numbers, one 1-line long (that's the command 
you just ran) and a longer one about 6 lines long. That's couch running.

Now run:

./add-user.sh
It should respond with "-hashed-9222..." and a lot of hex numbers.

Now test that couch is running by typing the url in a browser: 
http://localhost:5984/_utils

There should be two entries in red: _replicator and _users.

Now install the test databases:

cd backup
./upload-all.sh

The script asks for a password. Type in jabberw0cky (with a zero). This 
is a master script that calls all the other upload-*.sh files. (So you 
can upload them individually.) Finally run calliope from the command 
line:

5. Run the installer

cd ..
sudo ./install.sh
sudo ./calliope-start.sh

Hit return. The service will run even when you log off. To stop the 
service, log in as the same user who launched it initially and type:

sudo ./calliope-stop.sh

Access the service on http://localhost:8080/tests/ (trailing slash is 
significant). To make it visible on port 80 add the following lines to 
the end of /etc/apache2/httpd.conf,and restart apache

On OSX:

ProxyPass /tests/ http://localhost:8080/tests/ retry=0
ProxyPass /corpix/ http://localhost:8080/corpix/ retry=0
ProxyPass /import/ http://localhost:8080/import/ retry=0
ProxyPass /upload/ http://localhost:8080/upload/ retry=0
Now restart apache: sudo apachectl restart
Ubuntu

Add the above lines to /etc/apache2/mods-available/proxy.conf.

Restart apache2: sudo service apache2 restart

ADDING OTHER TEXTS

The Import dialog can be used to import XML or plain text files. For 
plain text to work there must be a suitable import filter. XML conforming 
to the TEI-Lite schema should import correctly if the TEI/default style 
is chosen.

Alternatively the mmpupload tool can be used to upload files in XML or 
plain text in batches. See www.github.com/AustESE-Infrastructure.
