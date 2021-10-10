#!/bin/bash

cd /home/vadim/Labs/prog/UDP_Server

rep="src"
jvrep="bin"
compile="javac -d ./$jvrep $rep/*.java"
launch="java -cp $jvrep $rep.Server $1"

echo ""
echo $compile
$compile
echo ""
echo $launch
echo ""
$launch
