#!/usr/bin/env bash

gcc -o program -static program.s -fPIC
./program
