#!/bin/bash
read -p "Password:" PASSWORD
# delete database
curl -X DELETE http://admin:$PASSWORD@localhost:5984/corform
# create database
curl -X PUT http://admin:$PASSWORD@localhost:5984/corform

curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/TEI%2Fdefault --data-binary @corform/TEI/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/TEI%2Fdrama%2Fdefault --data-binary @corform/TEI/drama/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/default --data-binary @corform/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/list%2Fdefault --data-binary @corform/list/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/list%2Ftwin-list --data-binary @corform/list/twin-list.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/play%2Fitalian%2Fcapuana --data-binary @corform/play/italian/capuana.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/play%2Fitalian%2Fcapuana%2Faristofanunculos --data-binary @corform/play/italian/capuana/aristofanunculos.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corform/poetry%2Fenglish%2Fharpur --data-binary @corform/poetry/english/harpur.json

