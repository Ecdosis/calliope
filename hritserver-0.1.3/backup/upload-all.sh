#!/bin/bash
# delete databases
curl -X DELETE http://admin:jabberw0cky@localhost:5984/cortex
curl -X DELETE http://admin:jabberw0cky@localhost:5984/corcode
curl -X DELETE http://admin:jabberw0cky@localhost:5984/corform
curl -X DELETE http://admin:jabberw0cky@localhost:5984/corpix
curl -X DELETE http://admin:jabberw0cky@localhost:5984/config
# create databases
curl -X PUT http://admin:jabberw0cky@localhost:5984/cortex
curl -X PUT http://admin:jabberw0cky@localhost:5984/corcode
curl -X PUT http://admin:jabberw0cky@localhost:5984/corform
curl -X PUT http://admin:jabberw0cky@localhost:5984/corpix
curl -X PUT http://admin:jabberw0cky@localhost:5984/config

curl -s -X PUT http://admin:jabberw0cky@localhost:5984/config/splitter%2Fdefault --data-binary @config/splitter/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/config/splitter%2Ftei --data-binary @config/splitter/tei.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/config/stripper%2Fplay%2Fitalian%2Fcapuana --data-binary @config/stripper/play/italian/capuana.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/config/stripper%2Fpoetry%2Fenglish%2Fharpur --data-binary @config/stripper/poetry/english/harpur.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/config/stripper%2Fplay%2Fitalian%2Fdefault --data-binary @config/stripper/play/italian/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/config/stripper%2Fdefault --data-binary @config/stripper/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corcode/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1%2Fdefault --data-binary @corcode/english/shakespeare/kinglear/act1/scene1/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FFrammento%201%2Fdefault --data-binary @corcode/italian/capuana/aristofanunculos/Frammento\ 1/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FFrammento%202%2Fdefault --data-binary @corcode/italian/capuana/aristofanunculos/Frammento\ 2/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FIntroduction%2Fdefault --data-binary @corcode/italian/capuana/aristofanunculos/Introduction/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/TEI%2Fdefault --data-binary @corform/TEI/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/TEI%2Fdrama%2Fdefault --data-binary @corform/TEI/drama/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/default --data-binary @corform/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/list%2Fdefault --data-binary @corform/list/default.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/play%2Fitalian%2Fcapuana --data-binary @corform/play/italian/capuana.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/play%2Fitalian%2Fcapuana%2Faristofanunculos --data-binary @corform/play/italian/capuana/aristofanunculos.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corform/poetry%2Fenglish%2Fharpur --data-binary @corform/poetry/english/harpur.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/cortex/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1 --data-binary @cortex/english/shakespeare/kinglear/act1/scene1.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FFrammento%201 --data-binary @cortex/italian/capuana/aristofanunculos/Frammento\ 1.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FFrammento%202 --data-binary @cortex/italian/capuana/aristofanunculos/Frammento\ 2.json
curl -s -X PUT http://admin:jabberw0cky@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FIntroduction --data-binary @cortex/italian/capuana/aristofanunculos/Introduction.json
./images.sh

