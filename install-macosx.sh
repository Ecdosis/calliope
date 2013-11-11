#!/bin/bash
#install homebrew
if [ $USER = "root" ]; then
# define pgrep function for BSD
pgrep(){ ps -ax -o pid,command | grep "$@" | grep -v 'grep' | awk '{print $1;}'; }
BREW_LOC=`which brew`
if [ -z $BREW_LOC ]; then
    echo "installing homebrew. this may take some time ..."
    su - $SUDO_USER -c "curl -o /tmp/012345.rb -fsSkL raw.github.com/mxcl/homebrew/go"
	chmod +x /tmp/012345.rb
    su - $SUDO_USER -c "/tmp/012345.rb"
	rm /tmp/012345.rb
    BREW_LOC=`which brew`
else
    echo "homebrew was already installed"
fi
# check that homebrew was installed
if [ -z $BREW_LOC ]; then
    echo "homebrew install failed. exiting..."
    exit
fi
# install gcc
GCC_LOC=`which gcc`
if [ -z $GCC_LOC ]; then
    echo "installing gcc..."
    brew tap homebrew/dupes
    brew install apple-gcc42
	GCC_LOC=`which gcc`
else
    echo "gcc already installed"
fi
# check that gcc was installed
if [ -z $GCC_LOC ]; then
    echo "gcc install failed. exiting..."
    exit
fi
# install couchdb
COUCHDB_LOC=`which couchdb`
if [ -z $COUCHDB_LOC ]; then
    brew install couchdb
else
    echo "couchdb already installed"
fi
# check that couchdb was installed
if [ -z $COUCHDB_LOC ]; then
    echo "couchdb install failed. exiting..."
    exit
fi
# launch couchdb daemon
COUCHDAEMON=`launchctl list | grep couchdb | awk '{print $1;}'`
if [ -z $COUCHDAEMON ]; then
    # kill existing rogue instances of couchdb
    CPID=`pgrep couchdb`
    if [ -n $CPID ]; then
        echo "killing existing couchdb instance $CPID"
        kill $CPID
    fi
    echo "launching couchdb daemon"
    cp org.apache.couchdb.plist /Library/LaunchDaemons/
    launchctl load /Library/LaunchDaemons/org.apache.couchdb.plist
else
    echo "couchdb daemon already running"
fi
# wait for couchdb to launch
i="0"
while [ $i -lt 4 ]
do
sleep 1
CPID=`pgrep couchdb`
if [ -z $CPID ]; then 
    i="4"
else
    i=$[$i+1]
fi
done
# check that it worked
if [ -z $CPID ]; then
    echo "couchdb daemon failed to launch. exiting..."
    exit
fi
# check that admin user exists
res=`curl -s -X GET http://admin:jabberw0cky@localhost:5984/_users/_all_docs|grep _design/_auth`
if [ -z $res ]; then
    echo "creating admin user..."
    curl -s -X PUT http://localhost:5984/_config/admins/admin -d '"jabberw0cky"'
	res=`curl -s -X GET http://admin:jabberw0cky@localhost:5984/_users/_all_docs|grep _design/_auth`
	if [ -z $res ]; then
		echo "couldn't create admin user. exiting..."
		exit
	fi
else
    echo "admin user found"
fi
# move to runfolder
RUNFOLDER=`ls -d calliope-*`
cd $RUNFOLDER
read -p "Reinitialise sample data? (deletes ALL data):" UPLOAD
if [ $UPLOAD == "Y" ]; then
	# upload sample data
	echo "uploading sample data to database..."
	cd backup
	# avoid prompt for password
	export PASSWORD="jabberw0cky"
	./upload-all.sh
    # move back to runfolder
	cd ..
else
	echo "OK, not uploading any data..."
fi
echo "installing formatter and stripper libraries"
./install-libs.sh
./calliope-start.sh
cd ../..
echo "done!"
else
  echo "Need to be root. Did you use sudo?"
fi
