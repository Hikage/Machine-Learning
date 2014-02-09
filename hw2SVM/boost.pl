#!/usr/bin/perl

use strict;
use warnings;

use List::MoreUtils qw( each_array );
use Math::Complex;

# "Boost" Adaboost algorithm script
# © 2014 Brianna Shade
# CS545 - Machine Learning WI 2014

my @w;                                              #global weight vector
my @wbndry;                                         #weight boundaries for roulette

# Update weight vector boundaries for roulette
# takes in an array of new weights
sub updateWeightBound{
    foreach(0..@w-1){
        my $prvbndry = ($_ == 0) ? 0 : $wbndry[$_-1];
        $wbndry[$_] = $prvbndry + $w[$_];
    }
#    foreach(@wbndry){ print "$_\t"; }
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
    my $K = 50;                                     #boosting iterations
    my $kernel = "-t 1 -d 5";                       #kernel parameters

    # Read file into array of arrays
    open(FILE, "<", $S) or die "Can't open file: $!\n";
        my @instances;
        while (my $instance = <FILE>){
            push(@instances, [split(' ', $instance)]);
        }
    close(FILE);

    # Initialize weight vector
    my $M = @instances;
    @w = (1/$M) x $M;
    @wbndry = (0) x $M;
#    print join(" ", @w) . "\n";
    updateWeightBound();
#    print join(" ", @w) . "\n";

    my @Hx = (0) x $M;                              #ensemble classifier array (for all instances)

    foreach my $T (0..$K-1){                                    #boosting iterations
        my $train = "boost/S$T";
        my $hypoth = "boost/h$T";
        my $predict = "boost/$T.predictions";
        my $testpred = "boost/Test$T.predictions";

        my $trainex;
        foreach(0..$M-1){
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

        # Calculate error
        open(FILE, "<", $predict) or die "Can't open file: $!\n";
            my @predictions = <FILE>;
        close(FILE);

        my @actclasses;
        my @pclasses;

        my $error = 0.0;
        my $cmpr = each_array(@instances, @predictions, @w);
        while(my ($inst, $score, $wt) = $cmpr->() ) {
            my $actclss = @$inst[0];
            push(@actclasses, $actclss);

            my $pclss = ($score < 0) ? -1 : 1;
            push(@pclasses, $pclss);

            if($actclss != $pclss){ $error += $wt; }
        }
#        print "error: $error\n";

        # Calculate alpha
        my $alpha = .5 * log((1 - $error) / $error);
#        print "alpha: $alpha\n";

        # Calculate new weight vector
        my $Z = 0;
        foreach(0..@w-1){
            $w[$_] *= exp($alpha * -1 * $actclasses[$_] * $pclasses[$_]);
            $Z += $w[$_];
#            print "a: $actclss, p: $pclss; what: $wt\n";
        }
#        foreach(@w){ print "$_\n"; }
#        print "Z: $Z\n";

        foreach(@w){ $_ /= $Z; }
#        foreach(@w){ print "$_\n"; }
        updateWeightBound();
    
        system("svm_light_osx.8.4_i7/svm_classify $test $hypoth $testpred");
        #extract sign of first row in $testpred (+ or -)
        open(FILE, "<", $testpred) or die "Can't open file: $!\n";
            my @testpredictions = <FILE>;
        close(FILE);

        #compute H(x)
        foreach(0..@Hx-1){
            my $tpclss = ($testpredictions[$_] < 0) ? -1 : 1;
            $Hx[$_] += ($alpha * $tpclss);
#            print "a: $alpha, cl: $tpclss; Hx: $Hx[$_]\n";
        }
    }

    open(FILE, "<", $test) or die "Can't open file: $!\n";
        my @tinsts;
        while (my $tinst = <FILE>){
            push(@tinsts, [split(' ', $tinst)]);
        }
    close(FILE);

    my $acc = 0;
    
    foreach(0..@Hx-1){
        $Hx[$_] = ($Hx[$_] < 0) ? -1 : 1;
        open(FILE, ">>boost/results.out");
            print FILE "tinst: $tinsts[$_][0]; Hx: $Hx[$_]\n";
        close(FILE);
    
        $acc++ if($Hx[$_] == $tinsts[$_][0]);
    }
    
    $acc /= $M;
    print "Ensemble accuracy: $acc\n";
}

run();
