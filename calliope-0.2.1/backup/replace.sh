#!/bin/bash
read -p "Password:" PASSWORD
erase_if_exists()
{
    RESULT=`curl -s --head $1`
	echo $RESULT
    if [ ${RESULT:9:3} == "200" ]; then
      REVID=$(expr "$RESULT" : '.*\"\(.*\)\"')
      curl -X DELETE $1?rev=$REVID
    fi
}
docid=${1%.*}
docid=${docid//\//\%2F}
docid=${docid/\%2F/\/}
echo docid=$docid
erase_if_exists http://admin:$PASSWORD@localhost:5984/$docid
curl -X PUT http://admin:$PASSWORD@localhost:5984/$docid --data-binary @$1
	

