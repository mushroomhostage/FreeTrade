#!/bin/sh -x
CLASSPATH=../craftbukkit-1.0.1-R1.jar javac FreeTrade.java
rm -rf com
mkdir -p com/exphc/FreeTrade
mv *.class com/exphc/FreeTrade/
jar cf FreeTrade.jar com/ *.yml
cp FreeTrade.jar ../plugins/
