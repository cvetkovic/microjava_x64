#!/usr/bin/env bash

INPUT=""
OUTPUT=""

set_input=0
set_output=0
dump_ast=0
dump_symbols=0
dump_ir=0
dump_asm=0
return_name=0

if [ "$#" == 0 ]; then
  echo "Invalid invocation parameter. Compilation terminated!"
  exit 1
fi

for var in "$@"
do
  #echo $var

  if [ "$var" == "-input" ]; then
    set_input=1
  elif [ "$set_input" == "1" ]; then
    INPUT="$var"
    set_input=0
  elif [ "$var" == "-output" ]; then
    set_output=1
  elif [ "$set_output" == "1" ]; then
    OUTPUT="$var"
    set_output=0
  elif [ "$var" == "-dump_ast" ]; then
    dump_ast=1
  elif [ "$var" == "-dump_symbols" ]; then
    dump_symbols=1
  elif [ "$var" == "-dump_ir" ]; then
    dump_ir=1
  elif [ "$var" == "-dump_asm" ]; then
    dump_asm=1
  elif [ "$var" == "-invoke_after" ]; then
    return_name=1
  else
    echo "Invalid invocation parameter. Compilation terminated!"
    exit 1
  fi
done

if [ "$INPUT" == "" ] | [ "$OUTPUT" == "" ]; then
  echo "Invalid invocation parameter. Compilation terminated!"
  exit 1
fi

concatenated="-input ${INPUT} -output ${OUTPUT}"

#echo $concatenated

if [ "$dump_ast" == "1" ]; then
    concatenated="${concatenated} -dump_ast"
fi
if [ "$dump_symbols" == "1" ]; then
    concatenated="${concatenated} -dump_symbols"
fi
if [ "$dump_ir" == "1" ]; then
    concatenated="${concatenated} -dump_ir"
fi
if [ "$dump_asm" == "1" ]; then
    concatenated="${concatenated} -dump_asm"
fi

#echo $concatenated

echo "MicroJava Compiler v1.0"
java -jar microjava_x64.jar $concatenated

EXECUTABLE=$(echo $OUTPUT | sed -r "s/.+\/(.+)\..+/\1/")
EXECUTABLE="${EXECUTABLE%.*}"

#echo $EXECUTABLE
GCC_ARGS="-o ${EXECUTABLE} -static ${OUTPUT} -fPIC"
gcc $GCC_ARGS

if [ "$return_name" == "1" ]; then
    eval "./${EXECUTABLE}"
fi