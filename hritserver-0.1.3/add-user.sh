#!/bin/sh
read -p "Admin password:" PASSWORD
curl -X PUT http://localhost:5984/_config/admins/admin -d '"$PASSWORD"'
