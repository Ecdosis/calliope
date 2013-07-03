#!/bin/bash
install_manpage()
{
  manpaths=`man -w`
  OIFS="$IFS"
  IFS=':'
  patharray=($manpaths)
  IFS=$OIFS
  i=0
  while [ $i -lt ${#patharray[@]} ]
  do
    lsres=`ls ${patharray[$i]}`
    if [[ $lsres == *man1* ]]; then
        cp $1 ${patharray[$i]}"/man1"
        break
    fi
    (( i=i+1 ))
  done
}
if [ $USER = "root" ]; then
  gcc -Iinclude src/*.c -lm -o pdef-tool
  gzip -c pdef-tool.1 > pdef-tool.1.gz 
  install_manpage pdef-tool.1.gz
  if [ ! -d "/usr/local/bin/" ]; then
      mkdir /usr/local/bin
  fi
  mv pdef-tool /usr/local/bin/
else
  echo "Did you use sudo?"
fi
