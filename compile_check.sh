#!/bin/bash
./gradlew compileDebugKotlin > compile_out.txt 2>&1
cat compile_out.txt | grep -A 20 "BUILD SUCCESSFUL" || cat compile_out.txt | grep -A 20 "FAILURE"
