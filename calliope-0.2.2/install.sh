#!/bin/sh
if [ "`uname`" = "Darwin" ]; then
  APACHE2_LOC=`which httpd`
  HASREADLINK=`whereis readlink | awk '{print $1;}'`
  LIBSUFFIX="dylib"
  JAVAC=`whereis javac | awk '{print $1;}'`
  JDKINCLUDEDIRNAME="Headers"
  HAS_BREW=`which brew`
  if [ -z "$HAS_BREW" ]; then
    echo "Please install homebrew"
    exit
  fi
else
  APACHE2_LOC=`which apache2`
  LIBSUFFIX="so"
  HASREADLINK=`whereis -b readlink | awk '{print $2;}'`
  JAVAC=`whereis -b javac | awk '{print $2;}'`
  JDKINCLUDEDIRNAME="include"
fi
HAS_GCC=`which gcc`
if [ -z "$HAS_GCC" ]; then
  echo "Please install gcc"
  exit
fi
if [ -z "$JAVAC" ]; then
  echo "Install a jdk first!"
  exit
fi
if [ -z "$APACHE2_LOC" ]; then
  echo "Please install apache2"
  exit
fi
# passed all basic sanity checks
a2enmod proxy 1>/dev/null
a2enmod proxy_http 1>/dev/null
a2enmod proxy_balancer 1>/dev/null
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
    gcc -DHAVE_EXPAT_CONFIG_H -DJNI -DHAVE_MEMMOVE -Istripper/include -I$JDKINCLUDE -O0 -g3 -Wall -fPIC stripper/src/*.c -shared -o libAeseStripper.$LIBSUFFIX
    gcc -DHAVE_EXPAT_CONFIG_H -DJNI -DHAVE_MEMMOVE -Iformatter/include -Iformatter/include/AESE -Iformatter/include/STIL -I$JDKINCLUDE -O0 -g3 -Wall -fPIC formatter/src/*.c formatter/src/AESE/*.c formatter/src/STIL/*.c -shared -o libAeseFormatter.$LIBSUFFIX
    if [ -d /usr/local/lib/libAeseStripper.$LIBSUFFIX.dSYM ]; then
      rm -rf libAeseStripper.$LIBSUFFIX.dSYM
    fi
    if [ -d /usr/local/lib/libAeseFormatter.$LIBSUFFIX.dSYM ]; then
      rm -rf libAeseFormatter.$LIBSUFFIX.dSYM
    fi
    mv libAeseStripper.$LIBSUFFIX $LIB_LOC
    mv libAeseFormatter.$LIBSUFFIX $LIB_LOC
  else
    echo "couldn't find jdk include directory ($JDKINCLUDE)"
  fi
# install pdef-tool
  cd pdef-tool
  ./rebuild.sh
  cd ..
else
  echo "Need to be root. Did you use sudo?"
fi
# set up proxy
if [ -d /etc/apache2/mods-available/ ]; then
  cp proxy.conf /etc/apache2/mods-available/
elif grep -Fq "ProxyPass /calliope/ http://localhost:8080/calliope/ retry=0" /etc/apache2/httpd.conf ; then 
  echo "proxy already set up"
elif [ -e /etc/apache2/httpd.conf ]; then
   cat httpd.conf >> /etc/apache2/httpd.conf 
else
  echo "unrecognised apache configuration"
fi
apachectl restart
# setup mongo user
mongo mongouser.js
./calliope-start.sh
# upload test data
cd pdef-tool
pdef-tool archive
