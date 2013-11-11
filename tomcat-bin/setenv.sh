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
export CATALINA_OPTS="-Djava.library.path=$LIBPATH -cp .$JARPATHS"
