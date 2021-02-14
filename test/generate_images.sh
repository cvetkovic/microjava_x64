#!/bin/bash

mkdir -p debug

pushd debug

for FILE in *.dot
do
	OUTPUT="${FILE}.png"
	dot -Tpng $FILE > $OUTPUT
done

rm -rf *.dot

popd
