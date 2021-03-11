#!/bin/bash

echo "Compiling all MikroJava programs in:"
echo $1
echo

CNT=0

for FILE in $1/*.mj
do
	OUTPUT_1=$(pwd)/tmp1.s
	OUTPUT_2=$(pwd)/tmp2.s
	touch $OUTPUT_1
	touch $OUTPUT_2

	echo $FILE

	EX1=$(eval java -jar pp2.jar -input $FILE -output $OUTPUT_1 -dump_ir)
	EX2=$(eval java -jar pp2.jar -input $FILE -output $OUTPUT_2 -dump_ir -optimize_ir)

	ELF_1=$(pwd)/elf1
	ELF_2=$(pwd)/elf2

	gcc -o $ELF_1 -static $OUTPUT_1 -fPIC
	gcc -o $ELF_2 -static $OUTPUT_2 -fPIC

	if [[ $FILE = *test301.mj ]]
	then
		read IN_1
		
		CMD_1="echo $IN_1 | ./elf1"
		CMD_2="echo $IN_1 | ./elf2"
		
		RESULT_1=$(eval "$CMD_1")
		RESULT_2=$(eval "$CMD_2")
	elif [[ $FILE = *test302.mj ]]
	then
		read IN_1
		read IN_2
		read IN_3
		
		CMD_1="echo $IN_1 $IN_2 $IN_3 | ./elf1"
		CMD_2="echo $IN_1 $IN_2 $IN_3 | ./elf2"
	
		RESULT_1=$(eval "$CMD_1")
		RESULT_2=$(eval "$CMD_2")
	elif [[ $FILE = *test303.mj ]]
	then
		read IN_1
		
		CMD_1="echo $IN_1 | ./elf1"
		CMD_2="echo $IN_1 | ./elf2"
	
		RESULT_1=$(eval "$CMD_1")
		RESULT_2=$(eval "$CMD_2")
	elif [[ $FILE = *test304.mj ]]
	then
		ARGS=""
		
		while : ; do
			read TYPE
			
			[[ "$TYPE" == "c" || "$TYPE" == "s" ]] || break
			
			read IN1
			read IN2
			read IN3
			
			ARGS="$ARGS $TYPE $IN1 $IN2 $IN3"
		done
	
		CMD_1="echo $ARGS | ./elf1"
		CMD_2="echo $ARGS | ./elf2"
		
		RESULT_1=$(eval "$CMD_1")
		RESULT_2=$(eval "$CMD_2")
	else
		RESULT_1=$(eval $ELF_1)
		RESULT_2=$(eval $ELF_2)
	fi

	red=`tput setaf 1`
	green=`tput setaf 2`
	reset=`tput sgr0`

	if [ "$RESULT_1" != "$RESULT_2" ]
	then
		echo "${red}Test failed! Semantics not preserved in $FILE!${reset}"
	else
		echo "${green}Test passed!${reset}"
		echo -e "\t$RESULT_1"
		echo ""
		echo -e "\t$RESULT_2"
	fi

	rm $OUTPUT_1
	rm $OUTPUT_2
	rm $ELF_1
	rm $ELF_2
done
