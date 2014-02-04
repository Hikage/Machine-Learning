#!/usr/bin/perl

use strict;
use warnings;

# "Boost" Adaboost algorithm script
# © 2014 Brianna Shade
# CS545 - Machine Learning WI 2014

my $S = "train/DogsVsCats.train";               #training data
my $test = "DogsVsCats.test";                   #test data
my $K = 10;                                     #boosting iterations
my $kernel = "-t 1 -d 5"                        #kernel parameters

# Read file into array of arrays
open(FILE, "<", $S)
  or die "Can't open file: $!\n";
my @instances;
while (my $instance = <FILE>){
    push(@instances, [split(' ', $instance)]);
}
close(FILE)

# Initialize weight vector
my $M = @instances;
my @w = (1/$M) x $M;
print join(" ", @w) . "\n";
print $M . "\n";

foreach my $T (0..$K-1){                                    #boosting iterations
    my $train = "S$T";
    my $hypoth = "h$T";
    my $predict = "predict/$T.predictions";
    my $testpred = "predict/Test$T.predictions";

    my @trainex;
    foreach my $i (0..$M-1){
        #select training example based on roulette wheel
        open(FILE, ">S$i");
        print FILE join(" ", @trainex);
        close(FILE);
    }

    # SVM
    system("svm_learn $kernel $train $model");          #train
    system("svm_classify $S $hypoth $predict");         #classify

    #use $predict to determine incorrectly classified instances
    #error$T = sum of incorrect classification weights
    #alpha$T = .5 * ln((1 - error$T) / error$T)
    #calculate new weight vector
    
    system("svm_classify $test $hypoth $testpred");
    #extract sign of first row in $testpred (+ or -)
    #compute H(x) for every x in the test set sum(alpha$T * $hypoth)
}
