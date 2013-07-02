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
www.github.com/AESE-Infrastructure. Click on the calliope repository, 
then the Downloads tab. Select "calliope-0.2.2.tar.gz" and download it 
(Don't click on "Download as .tar.gz" or .zip). When it is on you hard 
disk it will unpack automatically or by double-clicking on it or on the 
commandline (OSX removes the .gz automatically):

tar xvf calliope-0.2.2.tar
Now move the folder to a convenient location:

cd ~/Downloads
mv calliope-0.2.2 ~
cd ~/calliope-0.2.2

First set up the database for admin access. In the calliope-0.2.2
folder are two scripts add-user.sh and couchdb.sh Run this command first:

mongo
db = db.getSiblingDB('admin')
db.addUser( { user: "admin", pwd: "jabberw0cky" } )
exit

After running this you have to press return to get back the prompt. Check 
that mongodb is running:

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

Access the service on http://localhost:8080/tests/ (trailing slash is 
significant). To make it visible on port 80 add the following lines to 
the end of /etc/apache2/httpd.conf,and restart apache

On OSX:

ProxyPass /tests/ http://localhost:8080/tests/ retry=0
ProxyPass /corpix/ http://localhost:8080/corpix/ retry=0
ProxyPass /import/ http://localhost:8080/import/ retry=0
ProxyPass /upload/ http://localhost:8080/upload/ retry=0
ProxyPass /calliope/ http://localhost:8080/calliope/ retry=0
ProxyPass /test/ http://localhost:8080/test/ retry=0

Now restart apache: sudo apachectl restart

On Ubuntu:

Add the above lines instead to /etc/apache2/mods-available/proxy.conf.

Restart apache2: sudo service apache2 restart

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

