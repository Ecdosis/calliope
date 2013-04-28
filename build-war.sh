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
cp lib/commons-fileupload-1.2.2.jar calliope/WEB-INF/lib/
cp lib/commons-io-2.1.jar calliope/WEB-INF/lib/
cp lib/htmlparser.jar calliope/WEB-INF/lib/
cp lib/javax.mail-1.3.3.01.jar calliope/WEB-INF/lib/
cp lib/mongo-java-driver-2.11.1.jar calliope/WEB-INF/lib/
cp lib/nmerge.jar calliope/WEB-INF/lib/
cp lib/servlet-api-3.0.jar calliope/WEB-INF/lib/
cp dist/calliope.jar calliope/WEB-INF/lib/
cp web.xml calliope/WEB-INF/
jar cf calliope.war -C calliope WEB-INF -C calliope list

