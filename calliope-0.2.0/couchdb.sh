#!/bin/bash
if [ $USER = "root" ]; then
   nohup couchdb & > /dev/null 2>&1
else
   echo "Need to be root. Did you use sudo?"
fi
