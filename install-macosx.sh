#!/bin/bash
#install homebrew
if [ $USER = "root" ]; then
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
# kill existing instances of couchdb
pgrep(){ ps -ax -o pid,command | grep "$@" | grep -v 'grep' | awk '{print $1;}'; }
CPID=`pgrep couchdb`
if [ -n $CPID ]; then
    echo "killing existing couchdb instance $CPID"
    kill $CPID
fi
# launch couchdb daemon
echo "launching couchdb daemon"
cp org.apache.couchdb.plist /Library/LaunchDaemons/
launchctl load /Library/LaunchDaemons/org.apache.couchdb.plist
res=`curl -X GET http://admin:jabberw0cky@localhost:5984/_users/_all_docs|grep _design/_auth`
if [ -z $res ]; then
    echo "creating admin user..."
    curl -X PUT http://localhost:5984/_config/admins/admin -d '"jabberw0cky"'
fi
# install git
GIT_LOC=`which git`
if [ -z $GIT_LOC ]; then
    brew install git
else
    echo "git already installed"
fi
RUNFOLDER=`ls -d hritserver-*`
echo "uploading sample data to database...password is 'jabberw0cky'"
cd $RUNFOLDER/backup
./upload-all.sh
echo "installing formatter and stripper libraries"
cd ..
./install-libs.sh
.hritserver-start.sh
cd ../..
else
  echo "Need to be root. Did you use sudo?"
fi
