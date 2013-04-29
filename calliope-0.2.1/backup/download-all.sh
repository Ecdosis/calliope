#!/bin/bash
curl -s -X GET http://admin:jabberw0cky@localhost:5984/config/splitter%2Fdefault -o config/splitter/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/config/splitter%2Ftei -o config/splitter/tei.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/config/stripper%2Fplay%2Fitalian%2Fcapuana -o config/stripper/play/italian/capuana.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/config/stripper%2Fplay%2Fitalian%2Fdefault -o config/stripper/play/italian/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corcode/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1%2Fdefault -o corcode/english/shakespeare/kinglear/act1/scene1/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FFrammento%201%2Fdefault -o corcode/italian/capuana/aristofanunculos/Frammento\ 1/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FFrammento%202%2Fdefault -o corcode/italian/capuana/aristofanunculos/Frammento\ 2/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corcode/italian%2Fcapuana%2Faristofanunculos%2FIntroduction%2Fdefault -o corcode/italian/capuana/aristofanunculos/Introduction/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/TEI%2Fdefault -o corform/TEI/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/TEI%2Fdrama%2Fdefault -o corform/TEI/drama/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/default -o corform/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/list%2Fdefault -o corform/list/default.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/play%2Fitalian%2Fcapuana -o corform/play/italian/capuana.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/play%2Fitalian%2Fcapuana%2Faristfanunculos -o corform/play/italian/capuana/aristfanunculos.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/corform/play%2Fitalian%2Fcapuana%2Faristofanunculos -o corform/play/italian/capuana/aristofanunculos.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/cortex/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1 -o cortex/english/shakespeare/kinglear/act1/scene1.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FFrammento%201 -o cortex/italian/capuana/aristofanunculos/Frammento\ 1.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FFrammento%202 -o cortex/italian/capuana/aristofanunculos/Frammento\ 2.json
curl -s -X GET http://admin:jabberw0cky@localhost:5984/cortex/italian%2Fcapuana%2Faristofanunculos%2FIntroduction -o cortex/italian/capuana/aristofanunculos/Introduction.json
