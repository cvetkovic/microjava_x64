#!/bin/bash


bash ./framework.sh ./code_samples/optimizations/cfg_cleaner
bash ./framework.sh ./code_samples/optimizations/dead_code
bash ./framework.sh ./code_samples/optimizations/inlining
bash ./framework.sh ./code_samples/optimizations/loop_invariant
bash ./framework.sh ./code_samples/optimizations/unreachable_code
bash ./framework.sh ./code_samples/optimizations/value_numbering

echo "--------------------------------------------"
echo "Following tests require input from the user!"
echo "--------------------------------------------"

bash ./framework.sh ./code_samples/public_test
