#!/bin/sh
curl -X POST --form XML=@act1-scene2-F1.xml --form RECIPE=@recipe.xml --form STYLE=TEI --form FORMAT=STIL http://localhost:8080/strip

