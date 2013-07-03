#!/bin/bash
if [ $USER = "root" ]; then
  gcc -Iinclude src/*.c -lm -o pdef-tool
  gzip -c pdef-tool.1 > pdef-tool.1.gz 
  cp pdef-tool.1.gz /usr/local/share/man/man1/
  if [ ! -d "/usr/local/bin/" ]; then
      mkdir /usr/local/bin
  fi
  cp pdef-tool /usr/local/bin/
else
  echo "Did you use sudo?"
fi
