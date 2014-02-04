#!/usr/bin/perl

use strict;
use warnings;

# "Boost" Adaboost algorithm script
# © 2014 Brianna Shade
# CS545 - Machine Learning WI 2014

my $S = "train/DogsVsCats.train";              #training data
my $K = 10;                                    #boosting iterations

# Read file into array of arrays
open(FILE, "<", $S)
  or die "Can't open file: $!\n";
my @instances;
while (my $instance = <FILE>){
    push(@instances, [split(/ \d+:/, $instance)]);
}

my $M = @instances;
my @w = (1/$M) x $M;
print scalar join(" ", @w) . "\n";
print $M . "\n";
#$M=`wc -l $S | awk '{print $1}'`            #line count (# of training examples)

# Initialize weight vector
# w[$M]                                       #weight vector
# for (wv in w)
# do
#     wv=1/$M
#     done
#
#     for (T in 1..K)                             #boosting iterations
#     do
#         for (i in 1..M)
#             do
