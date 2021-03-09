#!/bin/sh

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

	EX1=$(eval java -jar pp2.jar -input $FILE -output $OUTPUT_1 -dump_ir)
	EX2=$(eval java -jar pp2.jar -input $FILE -output $OUTPUT_2 -dump_ir -optimize_ir)



	ELF_1=$(pwd)/elf1
	ELF_2=$(pwd)/elf2

	gcc -o $ELF_1 -static $OUTPUT_1 -fPIC
	gcc -o $ELF_2 -static $OUTPUT_2 -fPIC

	RESULT_1=$(eval $ELF_1)
	RESULT_2=$(eval $ELF_2)

	if [ "$RESULT_1" != "$RESULT_2" ]
	then
		echo "Test failed! Semantics not preserved in $FILE!"
	else
		echo "Test passed!"
	fi

	rm $OUTPUT_1
	rm $OUTPUT_2
	rm $ELF_1
	rm $ELF_2
done
