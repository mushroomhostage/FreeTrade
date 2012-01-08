#!/usr/bin/perl
while(<>) {
    if (m/:/) {
        print;
        print " "x(4*2)."name: \n";
        print " "x(4*2)."aliases: \n";
    }
    if (m/-/) {
        print " "x4 . $_;
    }
}
