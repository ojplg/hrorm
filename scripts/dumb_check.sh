#!/bin/bash

find ../src/main -name *.java | xargs grep FIXME
find ../src/main -name *.java | xargs grep TODO
find ../src/main -name *.java | xargs grep System.out
