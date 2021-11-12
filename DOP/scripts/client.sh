#!/bin/bash

cd /home/vadim/Labs/prog/UDP_Server/DOP

rep="src"
jvrep="bin"
compile="javac -d ./$jvrep $rep/*.java"
launch="java -cp $jvrep $rep.Client $1 $2"

echo ""
echo $compile
$compile
echo ""
echo $launch
echo ""
$launch