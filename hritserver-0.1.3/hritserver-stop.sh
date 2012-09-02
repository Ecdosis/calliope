#!/bin/sh
if [ `uname` = "Darwin" ]; then
  pgrep(){ ps -x -o pid,command | grep "$@" | grep -v 'grep' | awk '{print $1;}'; }
  HPID=`pgrep hritserver.jar`
  CPID=`pgrep couchdb`
  if [ -n "$HPID" ]; then
    kill $HPID
  fi
  if [ -n "$CPID" ]; then
    kill $CPID
  fi
else
  pkill -c -f hritserver.jar
  pkill -c -f couchdb
fi
