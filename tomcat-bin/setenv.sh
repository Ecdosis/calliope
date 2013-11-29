#!/bin/sh
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
LIBPATH=`java -cp /usr/local/fedora/tomcat/bin/ LibPath`
if [ `echo $LIBPATH | grep -c "/usr/local/lib" ` -eq 0 ] ; then
  LIBPATH=$LIBPATH:/usr/local/lib
fi
export CATALINA_OPTS="-Xincgc -Djava.library.path=$LIBPATH -cp .$JARPATHS"
