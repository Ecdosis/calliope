#!/bin/bash
if [ -z $PASSWORD ]; then
    read -p "Password:" PASSWORD
    export PASSWORD=$PASSWORD
fi
./upload-images.sh
./upload-corform.sh
./upload-config.sh
./upload-cortex.sh
./upload-corcode.sh
