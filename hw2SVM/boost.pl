#!/usr/bin/perl

use strict;
use warnings;

# "Boost" Adaboost algorithm script
# © 2014 Brianna Shade
# CS545 - Machine Learning WI 2014

my @w;                                              #global weight vector

# Update weight vector with ranges for roulette
# takes in an array of new weights
sub updateWeights{
    my (@newwts) = @_;
    for(my $i = 0; $i < @w; $i++){
        my $prvbndry;
        if ($i == 0) { $prvbndry = 0 }
        else { $prvbndry = $w[$i-1] }
        $w[$i] = $prvbndry + $newwts[$i];
    }
}

# Recursive binary search to locate appropriate instance bucket
sub locateInstance{
    my($min, $max, $num) = @_;
    if($min == $max) { return $max }
    if($max - $min < 2){
        if($num > $min) { return $min }
        else { return $max}
    }
    my $mid = int((($max - $min) / 2) + $min);
    if($num <= $w[$mid]) { return locateInstance($min, $mid, $num) }
    if($num > $w[$mid]) { return locateInstance($mid, $max, $num) }
}

# Main method
sub run{
    my $S = "train/DogsVsCats.train";               #training data
    my $test = "DogsVsCats.test";                   #test data
    my $K = 10;                                     #boosting iterations
    my $kernel = "-t 1 -d 5";                       #kernel parameters

    # Read file into array of arrays
    open(FILE, "<", $S)
      or die "Can't open file: $!\n";
    my @instances;
    while (my $instance = <FILE>){
        push(@instances, [split(' ', $instance)]);
    }
    close(FILE);

    # Initialize weight vector
    my $M = @instances;
    @w = (0) x $M;
#    print join(" ", @w) . "\n";
    updateWeights((1/$M) x $M);
#    print join(" ", @w) . "\n";

#    foreach my $T (0..$K-1){                                    #boosting iterations
my $T = 0;
        my $train = "boost/S$T";
        my $hypoth = "boost/h$T";
        my $predict = "boost/$T.predictions";
        my $testpred = "boost/Test$T.predictions";

        my $trainex;
        foreach my $i (0..$M-1){
            #roulette wheel selection
            my $r = rand();
            $trainex = @instances[locateInstance(0, $M-1, $r)];
#            print join(" ", @$trainex) . "\n";
            open(FILE, ">>$train");
            print FILE join(" ", @$trainex);
            print FILE "\n";
            close(FILE);
        }

        # SVM
        system("svm_light_osx.8.4_i7/svm_learn $kernel $train $hypoth");            #train
        system("svm_light_osx.8.4_i7/svm_classify $S $hypoth $predict");            #classify

        #use $predict to determine incorrectly classified instances
        #error$T = sum of incorrect classification weights
        #alpha$T = .5 * ln((1 - error$T) / error$T)
        #calculate new weight vector
    
    #    system("svm_classify $test $hypoth $testpred");
        #extract sign of first row in $testpred (+ or -)
        #compute H(x) for every x in the test set sum(alpha$T * $hypoth)
#    }
}

run();
