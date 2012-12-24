#!/bin/bash
read -p "Password:" PASSWORD
export PASSWORD=$PASSWORD
./upload-images.sh
./upload-corform.sh
./upload-config.sh
./upload-cortex.sh
./upload-corcode.sh
