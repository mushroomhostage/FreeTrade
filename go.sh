#!/bin/sh -x
CLASSPATH=../craftbukkit-1.1-R1.jar:../plugins-disabled/OddItem-0.8.1.jar javac FreeTrade.java -Xlint:deprecation -Xlint:unchecked
rm -rf me
mkdir -p me/exphc/FreeTrade
mv *.class me/exphc/FreeTrade/
jar cf FreeTrade.jar me/ *.yml
cp FreeTrade.jar ../plugins/
