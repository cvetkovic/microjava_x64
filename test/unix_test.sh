#!/usr/bin/env bash

OUTPUT="/mnt/c/Users/jugos000/IdeaProjects/microjava_x64/test/debug/program"
ASSEMBLY_INPUT="/mnt/c/Users/jugos000/IdeaProjects/microjava_x64/test/debug/program.s"

gcc -o $OUTPUT -static $ASSEMBLY_INPUT -fPIC
eval "$OUTPUT"
