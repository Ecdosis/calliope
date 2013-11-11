#!/bin/sh
VERSION=$1
if [ -z $VERSION ]; then
  echo "specify a version as first argument"
else 
  if [ -e "calliope-$VERSION" ]; then
    rm -f calliope-$VERSION/1
  else 
    mkdir calliope-$VERSION
  fi
  if [ ! -d calliope-$VERSION/formatter ]; then
    mkdir calliope-$VERSION/formatter
  fi
  if [ ! -d calliope-$VERSION/stripper ]; then
    mkdir calliope-$VERSION/stripper
  fi
  if [ ! -d calliope-$VERSION/lib ]; then
    mkdir calliope-$VERSION/lib
  fi
  cp -r ../standoff/stripper/include calliope-$VERSION/stripper/
  cp -r ../standoff/stripper/src calliope-$VERSION/stripper/
  cp -r ../standoff/formatter/include calliope-$VERSION/formatter/
  cp -r ../standoff/formatter/src calliope-$VERSION/formatter/
  cp -r lib/*.jar calliope-$VERSION/lib
  cp dist/calliope.jar calliope-$VERSION
  rm -f calliope-$VERSION/libAeseStripper.so
  rm -f calliope-$VERSION/libAeseFormatter.so
  tar -zcvf calliope-$VERSION.tar.gz calliope-$VERSION
fi
