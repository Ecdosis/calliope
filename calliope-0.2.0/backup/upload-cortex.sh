#!/bin/bash
echo "uploading cortex"
if [ -z $PASSWORD ]; then
    read -p "Password:" PASSWORD
fi
# delete database
curl -X DELETE http://admin:$PASSWORD@localhost:5984/cortex
# create database
curl -X PUT http://admin:$PASSWORD@localhost:5984/cortex
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/cortex/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1 --data-binary @cortex/english/shakespeare/kinglear/act1/scene1.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FFrammento%201 --data-binary @cortex/italian/capuana/aristofanunculos/Frammento\ 1.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FFrammento%202 --data-binary @cortex/italian/capuana/aristofanunculos/Frammento\ 2.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FIntroduction --data-binary @cortex/italian/capuana/aristofanunculos/Introduction.json

