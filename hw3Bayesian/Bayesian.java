/**
 * "Bayesian Network Learning"
 * Copyright © 2014 Brianna Shade
 * bshade@pdx.edu
 *
 * Bayesian.java
 * TODO details on this class
 */
package hw3Bayesian;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Bayesian {

	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static int[] trainCt = {376, 389, 380, 389, 387, 376, 377, 387, 380, 382};
	private static int trainTtl = 3823;
	private static double[] trainProb = new double[trainCt.length];
	private static double[][][] CPtrain = new double[64][17][10];
	
	private static double[] Ptest = {
		178/1797,		//0
		182/1797,		//1
		177/1797,		//2
		183/1797,		//3
		181/1797,		//4
		182/1797,		//5
		181/1797,		//6
		179/1797,		//7
		174/1797,		//8
		180/1797};		//9
	
	/**
	 * Reads input file and generates training data set
	 */
	public static void extractData(String inputFile){		
		FileReader fr;
		BufferedReader buff;
		
		try{
			fr = new FileReader(inputFile);
			buff = new BufferedReader(fr);
			
			//read in each line of the input file
			String line;
			while((line = buff.readLine()) != null && line != ""){					
				String[] feats = line.split(",");			//parse data into an array
				int[] instance = new int[feats.length];
				for(int i = 0; i < feats.length; i++){
					instance[i] = Integer.parseInt(feats[i]);
				}
				trainData.add(instance);					//add to training or test
			}
		}
		catch(IOException ex){
			System.err.println("Oh no! An error occurred!\n Error: " + ex);
			System.exit(0);
		}
	}
	
	public static void calcProbs(){
		for(int i = 0; i < trainProb.length; i++){
			trainProb[i] = trainCt[i] / (double)trainTtl;
			//System.out.println(trainProb[i]);
		}
	}
	
	public static void calcCondProbs(){
		for(int[]instance : trainData){
			for(int i = 0; i < instance.length-1; i++){
				CPtrain[i][instance[i]][instance[instance.length-1]]++;		//increment count of [feature number][feature value][class]
				//if(i == 0) System.out.println("["+ i + "][" + instance[i] + "][" + instance[instance.length-1] + "]");
			}
		}
		for(int i = 0; i < CPtrain.length; i++){
			for(int j = 0; j < CPtrain[0].length; j++){
				for(int k = 0; k < CPtrain[0][0].length; k++){
					//System.out.println(CPtrain[i][j][k] + "/" + trainProb[k] + " = " + CPtrain[i][j][k] / trainProb[k]);
					//if(i == 0) System.out.println(j + ", " + k + ": " + CPtrain[i][j][k] + "/" + trainCt[k]);
					CPtrain[i][j][k] /= trainCt[k];
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(!runTests(true, args[0])) System.exit(0);
		
		//extractData(args[0]);
		// TODO: Compute prior probability (included in optdigits.info)
		// TODO: Compute conditional probabilities for each digit, for each attribute value
		// TODO: Smooth conditional probabilities
		// TODO: Run naive Bayes on training data
		// TODO: Calculate accuracy of test data and single confusion matrix for all 10 digits
		// TODO: 4 bins
	}
	
	
	/**** Unit Testing ****/
	
	public static void clearData(){
		trainData.clear();
	}
	
	public static boolean testCalcProbs(){
		System.out.println("Testing probability calculations...");
		calcProbs();
		
		if(trainProb.length != trainCt.length){
			System.err.println("Probabilities somehow ended up with a different size than the counts: " + 
					trainProb.length + " (should be " + trainCt.length + ")");
			return false;
		}
		double ttl = 0;
		for(int i = 0; i < trainProb.length; i++){
			if(trainProb[i] == 0){
				System.err.println("Value at " + i + " is 0");
				return false;
			}
			ttl += trainProb[i];
		}
		if(ttl != 1){
			System.err.println("Total of all probabilities should be 1.0: " + ttl);
			return false;
		}
		if(trainProb[5] != (double)376/3823){
			System.err.println("Probability miscalculation; should be " + (double)376/3823 + " (is " + trainProb[5] + ")");
			return false;
		}
		
		System.out.println("Probability calculation tests pass! :)\n");
		return true;
	}
	
	public static boolean testCalcCondProbs(){
		System.out.println("Testing conditional probability calculations...");
		calcCondProbs();
		
		for(int i = 0; i < trainCt.length; i++){
			if(CPtrain[0][0][i] == 0){
				System.err.println("The first feature should have more than 0 counted 0s");
				return false;
			}
			if(CPtrain[0][0][i] != 1){
				System.err.println("Conditional probability miscalculation with " + i + "s; the first feature " +
						"for all data is 0, so this value should be 1.0: " + CPtrain[0][0][i]);
				return false;
			}
		}
		
		//randomly selected, manually calculated examples
		if(CPtrain[4][16][7] == 134.0/trainCt[7]){
			System.err.println("The fourth feature should have 134 counted 16 values for the digit 7:" +
					"Calculated probability: " + CPtrain[4][16][7] + "; Est. count: " + CPtrain[4][16][7]*trainCt[7]);
			return false;
		}
		if(CPtrain[47][0][4] == 193.0/trainCt[4]){
			System.err.println("The 47th feature should have 193 counted 0s for the digit 4:" +
					"Calculated probability: " + CPtrain[47][0][4] + "; Est. count: " + CPtrain[47][0][4]*trainCt[4]);
			return false;
		}
		if(CPtrain[59][8][9] == 27.0/trainCt[9]){
			System.err.println("The 47th feature should have 193 counted 0s for the digit 4:" +
					"Calculated probability: " + CPtrain[59][8][9] + "; Est. count: " + CPtrain[59][8][9]*trainCt[9]);
			return false;
		}
		
		System.out.println("Conditional probability calculation tests pass! :)\n");
		return true;
	}
	
	public static boolean runTests(boolean verbose, String trainFile){
		clearData();
		extractData(trainFile);
		if(!testCalcProbs()) return false;
		if(!testCalcCondProbs()) return false;
		return true;
	}

}
