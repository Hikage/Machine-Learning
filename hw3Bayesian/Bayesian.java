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
import java.util.Arrays;

public class Bayesian {

	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static ArrayList<int[]> testData = new ArrayList<int[]>();
	private static final boolean TRAIN = true;
	private static final boolean TEST = false;
	
	private static int[] trainCt = {376, 389, 380, 389, 387, 376, 377, 387, 380, 382};
	private static final double trainTtl = 3823.0;
	private static double[] trainProb = new double[trainCt.length];
	private static double[][][] CPtrain = new double[64][17][10];
	
	private static int[] testCt = {178, 182, 177, 183, 181, 182, 181, 179, 174, 180};
	private static final double testTtl = 1797.0;
	private static double[] testProb = new double[testCt.length];
	
	/**
	 * Reads input file and generates training data set
	 */
	public static void extractData(String inputFile, boolean train){		
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
				
				//add to training or test
				if(train) trainData.add(instance);
				else testData.add(instance);
			}
		}
		catch(IOException ex){
			System.err.println("Oh no! An error occurred!\n Error: " + ex);
			System.exit(0);
		}
	}
	
	public static void calcProbs(){
		for(int i = 0; i < trainProb.length; i++){
			trainProb[i] = trainCt[i] / trainTtl;
			//System.out.println(trainProb[i]);
		}
	}
	
	public static void calcCondProbs(){
		
		//Laplace smoothing
		for(double[][] digitvals : CPtrain){
			for(double[] digits : digitvals){
				Arrays.fill(digits, 1.0);									//initialize entire array to 1 so all counts are 1 greater
			}
		}
		for(int i = 0; i < trainCt.length; i++) trainCt[i] += 17;			//17 different values each attribute could take
		
		for(int[] instance : trainData){
			for(int i = 0; i < instance.length-1; i++){
				CPtrain[i][instance[i]][instance[instance.length-1]]++;		//increment count of [feature number][feature value][class]
			}
		}
		for(int i = 0; i < CPtrain.length; i++){
			for(int j = 0; j < CPtrain[0].length; j++){
				for(int k = 0; k < CPtrain[0][0].length; k++){
					CPtrain[i][j][k] /= trainCt[k];
				}
			}
		}
	}
	
	public static int classifyInst(int[] instance){
		if(instance == null){
			System.err.println("Instance to be classified is null");
			System.exit(0);
		}
		
		for(int i = 0; i < testProb.length; i++) testProb[i] = testCt[i] / testTtl;
		
		if(testProb[0] == 0){
			System.err.println("testProb calculations failed");
			System.exit(0);
		}
		
		double[] classProbs = new double[testProb.length];
		
		//iterate through each possible classification (0-9) and through each feature
		for(int i = 0; i < classProbs.length; i++){
			classProbs[i] = Math.log(testProb[i]);
			for(int j = 0; j < instance.length-1; j++){
				classProbs[i] += Math.log(CPtrain[j][instance[j]][i]);	//add in log of probability j has value of instance[j] for class i
			}
		}
		
		//find classification with highest probability
		double max = Double.NEGATIVE_INFINITY;
		int clsif = -1;
		for(int i = 0; i < classProbs.length; i++){
			if(classProbs[i] > max){
				max = classProbs[i];
				clsif = i;
			}
		}
		
		return clsif;
	}
	
	public static int[] naiveBayesClass(String testFile){		
		extractData(testFile, TEST);
		
		int[] correct = new int[testProb.length];
		for(int i = 0; i < testData.size(); i++){
			int clsif = classifyInst(testData.get(i));
			if(clsif < 0){
				System.err.println("Error finding maximum probability for classification");
				System.exit(0);
			}
			
			if(clsif == testData.get(i)[testData.get(i).length-1]) correct[clsif]++;
		}
		
		return correct;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(!runTests(true, args[0], args[1])) System.exit(0);
		
		//extractData(args[0], true);
		//calcProbs();
		//calcCondProbs();
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
			if(CPtrain[0][0][i] < .95){
				System.err.println("Conditional probability miscalculation with " + i + "s; the first feature " +
						"for all data is 0, so this value should be about 95% (with smoothing): " + CPtrain[0][0][i]);
				return false;
			}
		}
		
		//randomly selected, manually calculated examples
		if(CPtrain[4][16][7] == 135.0/trainCt[7]){
			System.err.println("The fourth feature should have 134 counted 16 values for the digit 7:" +
					"Calculated probability: " + CPtrain[4][16][7] + "; Est. count: " + CPtrain[4][16][7]*trainCt[7]);
			return false;
		}
		if(CPtrain[47][0][4] == 194.0/trainCt[4]){
			System.err.println("The 47th feature should have 193 counted 0s for the digit 4:" +
					"Calculated probability: " + CPtrain[47][0][4] + "; Est. count: " + CPtrain[47][0][4]*trainCt[4]);
			return false;
		}
		if(CPtrain[59][8][9] == 28.0/trainCt[9]){
			System.err.println("The 47th feature should have 193 counted 0s for the digit 4:" +
					"Calculated probability: " + CPtrain[59][8][9] + "; Est. count: " + CPtrain[59][8][9]*trainCt[9]);
			return false;
		}
		
		System.out.println("Conditional probability calculation tests pass! :)\n");
		return true;
	}
	
	
	public static boolean testClassifyInst(){
		System.out.println("Testing instance classification...");
		
		int[] inst1 = {0,0,1,13,14,3,0,0,0,0,8,16,13,2,0,0,0,2,16,16,3,0,0,0,0,3,16,12,1,0,0,0,0,5,16,14,5,0,0,0,0,3,16,16,16,16,6,0,0,1,14,16,16,16,12,0,0,0,3,12,15,14,7,0,6};
		int clsif = classifyInst(inst1);		
		if(clsif < 0 || clsif > 9){
			System.err.println("Classification outside of acceptable range: " + clsif);
			return false;
		}
		if(clsif != 6){
			System.err.println("Known training instance misclassified (should be 6): " + clsif);
			return false;
		}
		
		int[] inst2 = {0,0,1,12,16,14,2,0,0,0,13,11,3,16,5,0,0,4,14,0,0,15,6,0,0,6,12,8,13,16,5,0,0,0,9,12,4,10,8,0,0,0,3,0,0,11,5,0,0,0,16,14,5,15,4,0,0,0,3,12,16,11,1,0,9};
		clsif = classifyInst(inst2);		
		if(clsif < 0 || clsif > 9){
			System.err.println("Classification outside of acceptable range: " + clsif);
			return false;
		}
		if(clsif != 9){
			System.err.println("Known training instance misclassified (should be 9): " + clsif);
			return false;
		}
		
		int[] inst3 = {0,0,3,9,14,9,0,0,0,5,16,14,5,0,0,0,0,12,11,3,0,0,0,0,0,13,16,12,1,0,0,0,0,4,11,13,8,0,0,0,0,0,0,7,11,0,0,0,0,0,1,12,12,0,0,0,0,0,2,15,7,0,0,0,5};
		clsif = classifyInst(inst3);		
		if(clsif < 0 || clsif > 9){
			System.err.println("Classification outside of acceptable range: " + clsif);
			return false;
		}
		if(clsif != 5){
			System.err.println("Known training instance misclassified (should be 5): " + clsif);
			return false;
		}
		
		System.out.println("Instance classification tests pass! :)\n");
		return true;
	}
	
	public static boolean testNaiveBayesClass(String testFile, boolean verbose){
		System.out.println("Testing naive Bayes classification...");
		
		int[] correct = naiveBayesClass(testFile);
		
		for(int i = 0; i < correct.length; i++){
			if(correct[i] == 0 || correct[i] == testCt[i]){
				System.err.println("Perfect accuracy or failure very unlikely for " + i + ": " + correct[i]);
				return false;
			}
			if(verbose) System.out.println(i + ": " + correct[i] + "/" + testCt[i]);
		}
		
		System.out.println("Naive Bayes classification tests pass! :)\n");
		return true;
	}
	
	public static boolean runTests(boolean verbose, String trainFile, String testFile){
		clearData();
		extractData(trainFile, TRAIN);
		if(!testCalcProbs()) return false;
		if(!testCalcCondProbs()) return false;
		if(!testClassifyInst()) return false;
		if(!testNaiveBayesClass(testFile, verbose)) return false;
		return true;
	}

}
