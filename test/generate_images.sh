#!/bin/bash

mkdir -p debug

pushd debug

if [[ $1 -eq "rcfg" ]] ; then    
	for FILE in *.dot
	do
		if [[ "$FILE" == *"rcfg"* ]];then
        	        rm $FILE
 	       fi
	done
fi

for FILE in *.dot
do
	OUTPUT="${FILE}.png"
	dot -Tpng $FILE > $OUTPUT
done

rm -rf *.dot

popd
