#!/bin/sh
if [ "`uname`" = "Darwin" ]; then
  HASREADLINK=`whereis readlink | awk '{print $1;}'`
  LIBSUFFIX="dylib"
  JAVAC=`whereis javac | awk '{print $1;}'`
  JDKINCLUDEDIRNAME="Headers"
else
  LIBSUFFIX="so"
  HASREADLINK=`whereis -b readlink | awk '{print $2;}'`
  JAVAC=`whereis -b javac | awk '{print $2;}'`
  JDKINCLUDEDIRNAME="include"
fi
LIB_LOC=/usr/local/lib
getjdkinclude()
{
  if [ -n "$HASREADLINK" ]; then 
    while [ -h $JAVAC ]
    do
      JAVAC=`readlink $JAVAC`
    done
    echo `dirname $(dirname $JAVAC)`/$JDKINCLUDEDIRNAME
  else
    echo "need readlink. please install."
  fi
  return 
}
if [ $USER = "root" ]; then
# build stripper and formatter libs
  JDKINCLUDE=`getjdkinclude`
  if [ -d $JDKINCLUDE ]; then
    gcc -DHAVE_EXPAT_CONFIG_H -DJNI -DHAVE_MEMMOVE -Istripper/include -Iformatter/include/STIL -I$JDKINCLUDE -O0 -g3 -Wall -fPIC stripper/src/*.c formatter/src/STIL/cJSON.c -shared -o libAeseStripper.$LIBSUFFIX
    gcc -DHAVE_EXPAT_CONFIG_H -DJNI -DHAVE_MEMMOVE -Iformatter/include -Iformatter/include/AESE -Iformatter/include/STIL -I$JDKINCLUDE -O0 -g3 -Wall -fPIC formatter/src/*.c formatter/src/AESE/*.c formatter/src/STIL/*.c -shared -o libAeseFormatter.$LIBSUFFIX
    if [ -e /usr/local/lib/libAeseStripper.$LIBSUFFIX ]; then
      rm /usr/local/lib/libAeseStripper.$LIBSUFFIX
    fi
    if [ -e /usr/local/lib/libAeseFormatter.$LIBSUFFIX ]; then
      rm /usr/local/lib/libAeseFormatter.$LIBSUFFIX
    fi
    cp libAeseStripper.$LIBSUFFIX $LIB_LOC
    cp libAeseFormatter.$LIBSUFFIX $LIB_LOC
	if [ ! -d "/usr/local/bin/" ]; then
      mkdir /usr/local/bin
    fi
    if [ ! -d "/usr/local/bin/calliope" ]; then
      mkdir /usr/local/bin/calliope
    fi
    cp -r *.sh lib calliope.jar /usr/local/bin/calliope
  else
    echo "couldn't find jdk include directory ($JDKINCLUDE)"
  fi
else
  echo "Need to be root. Did you use sudo?"
fi

