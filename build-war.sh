#!/bin/bash
if [ ! -d calliope ]; then
  mkdir calliope
  if [ $? -ne 0 ] ; then
    echo "couldn't create calliope directory"
    exit
  fi
fi
if [ ! -d calliope/WEB-INF ]; then
  mkdir calliope/WEB-INF
  if [ $? -ne 0 ] ; then
    echo "couldn't create calliope/WEB-INF directory"
    exit
  fi
fi
if [ ! -d calliope/WEB-INF/lib ]; then
  mkdir calliope/WEB-INF/lib
  if [ $? -ne 0 ] ; then
    echo "couldn't create calliope/WEB-INF/lib directory"
    exit
  fi
fi
rm calliope/WEB-INF/lib/*.jar
cp dist/calliope.jar calliope/WEB-INF/lib/
cp lib/*.jar calliope/WEB-INF/lib/
echo `./web.sh` > calliope/WEB-INF/web.xml
#cp web.xml calliope/WEB-INF/
jar cf calliope.war -C calliope WEB-INF 
echo "NB: you MUST copy the contents of tomcat-bin to $tomcat_home/bin"
