#!/bin/sh
VERSION=$1
if [ -z $VERSION ]; then
  echo "specify a version as first argument"
else 
  if [ -e "hritserver-$VERSION" ]; then
    rm -f hritserver-$VERSION/1
  else 
    mkdir hritserver-$VERSION
  fi
  if [ ! -d hritserver-$VERSION/formatter ]; then
    mkdir hritserver-$VERSION/formatter
  fi
  if [ ! -d hritserver-$VERSION/stripper ]; then
    mkdir hritserver-$VERSION/stripper
  fi
  if [ ! -d hritserver-$VERSION/lib ]; then
    mkdir hritserver-$VERSION/lib
  fi
  cp -r ../standoff/stripper/include hritserver-$VERSION/stripper
  cp -r ../standoff/stripper/src hritserver-$VERSION/stripper/
  cp -r ../standoff/formatter/include hritserver-$VERSION/formatter/
  cp -r ../standoff/formatter/src hritserver-$VERSION/formatter/
  cp -r lib/*.jar hritserver-$VERSION/lib
  cp dist/hritserver.jar hritserver-$VERSION
  rm -f hritserver-$VERSION/libHritStripper.so
  rm -f hritserver-$VERSION/libHritFormatter.so
  tar -zcvf hritserver-$VERSION.tar.gz hritserver-$VERSION
fi
