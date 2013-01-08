#!/bin/bash
#install homebrew
if [ $USER = "root" ]; then
# define pgrep function for BSD
pgrep(){ ps -ax -o pid,command | grep "$@" | grep -v 'grep' | awk '{print $1;}'; }
BREW_LOC=`which brew`
if [ -z $BREW_LOC ]; then
    echo "installing homebrew ..."
    ruby -e "$(curl -fsSkL raw.github.com/mxcl/homebrew/go)"
else
    echo "homebrew was already installed"
fi
# install gcc
GCC_LOC=`which gcc`
if [ -z $GCC_LOC ]; then
    echo "installing gcc..."
    brew tap homebrew/dupes
    brew install apple-gcc42
else
    echo "gcc already installed"
fi
# install couchdb
COUCHDB_LOC=`which couchdb`
if [ -z $COUCHDB_LOC ]; then
    brew install couchdb
else
    echo "couchdb already installed"
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
else
    echo "admin user found"
fi
# upload sample data
RUNFOLDER=`ls -d hritserver-*`
echo "uploading sample data to database..."
cd $RUNFOLDER/backup
# avoid prompt for password
export PASSWORD="jabberw0cky"
./upload-all.sh
echo "installing formatter and stripper libraries"
cd ..
./install-libs.sh
./hritserver-start.sh
cd ../..
else
  echo "Need to be root. Did you use sudo?"
fi
