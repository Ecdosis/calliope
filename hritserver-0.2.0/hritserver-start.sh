#!/bin/bash
getjarpaths()
{
  JARPATH=""
  for f in $1/*.jar
  do
    JARPATH="$JARPATH:$f"
  done
  echo $JARPATH
  return
}
JARPATHS=`getjarpaths lib`
LIBPATH=`java LibPath`
if [[ $LIBPATH != */usr/local/lib* ]]
then
  LIBPATH=$LIBPATH:/usr/local/lib
fi
if [ `uname` = "Darwin" ]; then
  pgrep(){ ps -ax -o pid,command | grep "$@" | grep -v 'grep' | awk '{print $1;}'; }
  HPID=`pgrep hritserver.jar`
  if [ -n "$HPID" ]; then
    kill $HPID
  fi
else
  pkill -c -f hritserver.jar
fi
nohup java -Djava.library.path=$LIBPATH -cp .$JARPATHS -jar hritserver.jar >/dev/null &

