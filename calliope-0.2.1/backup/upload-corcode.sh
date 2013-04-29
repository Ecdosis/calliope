#!/bin/bash
echo "uploading corcode"
if [ -z $PASSWORD ]; then
    read -p "Password:" PASSWORD
fi
# delete database
curl -X DELETE http://admin:$PASSWORD@localhost:5984/corcode
# create database
curl -X PUT http://admin:$PASSWORD@localhost:5984/corcode
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corcode/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1%2Fdefault --data-binary @corcode/english/shakespeare/kinglear/act1/scene1/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FFrammento%201%2Fdefault --data-binary @corcode/italian/capuana/aristofanunculos/Frammento\ 1/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FFrammento%202%2Fdefault --data-binary @corcode/italian/capuana/aristofanunculos/Frammento\ 2/default.json
curl -s -X PUT http://admin:$PASSWORD@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FIntroduction%2Fdefault --data-binary @corcode/italian/capuana/aristofanunculos/Introduction/default.json

