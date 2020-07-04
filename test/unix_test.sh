#!/usr/bin/env bash

OUTPUT="/mnt/c/Users/jugos/IdeaProjects/microjava_x64/test/program"
ASSEMBLY_INPUT="/mnt/c/Users/jugos/IdeaProjects/microjava_x64/test/program.s"

gcc -o $OUTPUT -static $ASSEMBLY_INPUT -fPIC
eval "$OUTPUT"