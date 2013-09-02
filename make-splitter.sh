#!/bin/bash
if [ -d "splitter-temp" ]; then
  rm -rf splitter-temp
fi
mkdir splitter-temp
mkdir splitter-temp/calliope/
mkdir splitter-temp/calliope/json/
mkdir splitter-temp/calliope/exception/
mkdir splitter-temp/calliope/handler/
mkdir splitter-temp/calliope/handler/post/
mkdir splitter-temp/calliope/handler/post/importer/
cp build/classes/calliope/json/*.class splitter-temp/calliope/json/
cp build/classes/calliope/exception/*.class splitter-temp/calliope/exception/
cp build/classes/calliope/handler/post/importer/*.class splitter-temp/calliope/handler/post/importer/
jar cfm splitter.jar MANIFEST.MF -C splitter-temp calliope 
rm -rf splitter-temp
