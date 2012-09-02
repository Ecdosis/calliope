#!/bin/bash
contains()
{
	echo `echo $1 | grep -c $2`
}
docid()
{
	echo `echo $1 | awk '{gsub(/\//,"%2F",$1);print $1;}'`
}
filename()
{
	echo `echo $1 | awk '{l=split($1,a,"/");print a[l];}'`
}
upload()
{
	local REVEXPR=""
	if [ $3 ]; then
		REVEXPR="?rev=$3"
	fi
	local FILENAME=`filename $f`
	local RESPONSE=`curl -s -X PUT http://admin:jabberw0cky@localhost:5984/corpix/$1/$FILENAME$REVEXPR --data-binary @$2 -H "Content-Type: image/png"`
	echo `echo $RESPONSE | awk '{split($1,a,"\"");print a[10]}'`
}
descend()
{
	local DIR=$1
    local REVID=""
	local DOCID=`docid $DIR`
	for f in $DIR/*
	do
		if [ -d $f ]; then
			descend $f
		elif [ -f $f ]; then
			local CONTAINS=`contains $f ".png"`
			if [ $CONTAINS = 1 ]; then
				REVID=`upload $DOCID $f $REVID`
			else
			        CONTAINS=`contains $f ".gif"`
			        if [ $CONTAINS = 1 ]; then
				    REVID=`upload $DOCID $f $REVID`
			        fi
			fi
		fi
	done
}
descend "list"
