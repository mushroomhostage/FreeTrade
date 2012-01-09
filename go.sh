#!/bin/sh -x
CLASSPATH=../craftbukkit-1.0.1-R1.jar:../plugins-disabled/OddItem-0.8.1.jar javac FreeTrade.java -Xlint:deprecation
rm -rf com
mkdir -p com/exphc/FreeTrade
mv *.class com/exphc/FreeTrade/
jar cf FreeTrade.jar com/ *.yml
cp FreeTrade.jar ../plugins/
