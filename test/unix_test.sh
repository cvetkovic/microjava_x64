#!/usr/bin/env bash

OUTPUT="/mnt/c/Users/cl160127d/IdeaProjects/microjava_x64/test/program"
ASSEMBLY_INPUT="/mnt/c/Users/cl160127d/IdeaProjects/microjava_x64/test/program.s"

gcc -o $OUTPUT -static $ASSEMBLY_INPUT -fPIC
eval "$OUTPUT"