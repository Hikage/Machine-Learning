/**
 * "Bayesian Network Learning"
 * Copyright © 2014 Brianna Shade
 * bshade@pdx.edu
 *
 * Bayesian.java
 * This classifies numerical digits through a naive Bayes probability algorithm
 */
package hw3Bayesian;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class Bayesian {

	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static ArrayList<int[]> testData = new ArrayList<int[]>();
	private static final boolean TRAIN = true;
	private static final boolean TEST = false;
	
	//Training data
	private static int[] trainCt = {376, 389, 380, 389, 387, 376, 377, 387, 380, 382};
	private static final double trainTtl = 3823.0;
	private static double[] trainProb = new double[trainCt.length];
	private static double[][][] CPtrain = new double[64][17][10];
	
	//Test data
	private static int[] testCt = {178, 182, 177, 183, 181, 182, 181, 179, 174, 180};
	private static final double testTtl = 1797.0;

	private static final int TP = 0, FP = 1, FN = 2, TN = 3;
	private static final DecimalFormat pf = new DecimalFormat("#.##%");
	
	/**
	 * Reads input file and generates data set in the form of an array
	 * @param inputFile: file to be read in
	 * @param train: whether training data is being processed (as opposed to test data)
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
				String[] feats = line.split(",");					//parse data into an array
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
	
	/**
	 * Calculates the base probabilities of each digit within the training data
	 */
	public static void calcProbs(){
		for(int i = 0; i < trainProb.length; i++){
			trainProb[i] = trainCt[i] / trainTtl;
		}
	}
	
	/**
	 * Calculates the probabilities for each feature for each digit
	 */
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
					CPtrain[i][j][k] /= trainCt[k];							//convert each feature's count to a probability
				}
			}
		}
	}
	
	/**
	 * Classifies a given instance
	 * @param instance: instance array to be classified
	 * @return: returns the determined classification of the instance
	 */
	public static int classifyInst(int[] instance){
		if(instance == null){
			System.err.println("Instance to be classified is null");
			System.exit(0);
		}
		
		double[] classProbs = new double[trainProb.length];
		
		//iterate through each possible classification (0-9) and through each feature
		for(int i = 0; i < classProbs.length; i++){
			classProbs[i] = Math.log(trainProb[i]);
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
	
	/**
	 * Performs full naive Bayes classification across all test instances
	 * @param testFile: file to be read in and classified
	 * @return: returns the confusion matrix obtained from classification
	 */
	public static int[][] naiveBayesClass(String testFile){		
		extractData(testFile, TEST);
		
		int[][] conMtrx = new int[testCt.length][4];
		
		//iterate through each instance of the data and classify
		for(int i = 0; i < testData.size(); i++){
			int clsif = classifyInst(testData.get(i));
			int trueclsif = testData.get(i)[testData.get(i).length-1];
			
			if(clsif < 0){
				System.err.println("Error finding maximum probability for classification");
				System.exit(0);
			}
			
			if(clsif == trueclsif){										//correct classification
				conMtrx[clsif][TP]++;
				for(int j = 0; j < conMtrx.length; j++){
					if(j != clsif) conMtrx[j][TN]++;					//increment true negatives for all other classifications
				}
			}
			else{														//incorrect classification
				conMtrx[clsif][FP]++;
				conMtrx[trueclsif][FN]++;
				for(int j = 0; j < conMtrx.length; j++){
					if(j != clsif && j != trueclsif) conMtrx[j][TN]++;	//increment true negatives for all other classifications
				}
			}
		}
		
		return conMtrx;
	}
	
	/**
	 * Converts the confusion matrix into a string representation
	 * @param digitConMtrx: confusion matrix for a specific digit, to be converted
	 * @return: returns the string representation of the indicated confusion matrix
	 */
	public static String digitConMtrxToString(int[] digitConMtrx){
		return digitConMtrx[TP] + "(TP), " + digitConMtrx[TN] + "(TN), " +
				digitConMtrx[FP] + "(FP), " + digitConMtrx[FN] + "(FN)";
	}
	
	/**
	 * Calculates overall accuracy of test data
	 * @param conMtrx: confusion matrix used to calculate accuracy
	 * @return: returns overall accuracy of test data
	 */
	public static double calcAcc(int[][] conMtrx){		
		int correct = 0;
		for(int i = 0; i < conMtrx.length; i++){
			correct += conMtrx[i][TP] + conMtrx[i][TN];
		}
		
		return correct/testTtl/testCt.length;
	}
	
	/**
	 * Prints the confusion matrix for all digits and overall accuracy
	 * @param conMtrx: confusion matrix to be printed
	 */
	public static void printConMtrx(int[][] conMtrx){
		System.out.println((int)testTtl + " total instances");
		System.out.println("Accuracy: " + pf.format(calcAcc(conMtrx)));
		for(int i = 0; i < conMtrx.length; i++){
			System.out.println(i + ": " + digitConMtrxToString(conMtrx[i]));
		}
	}
	
	/**
	 * Main method
	 * @param args: takes in the names of the training data and test data files
	 */
	public static void main(String[] args) {
		if(!runTests(true, args[0], args[1])) System.exit(0);
		
		//extractData(args[0], TRAIN);
		//calcProbs();
		//calcCondProbs();
		//int[][] conMtrx = naiveBayesClass(args[1]);
		//printConMtrx(conMtrx);
		// TODO: 4 bins
	}
	
	
	/**** Unit Tests ****/
	
	/**** Unit Testing ****/
	
	/**
	 * Resets data structures
	 */
	public static void clearData(){
		trainData.clear();
		testData.clear();
	}
	
	
	/**
	 * Tests the calcProbs() method
	 * @return: returns true if all tests pass
	 */
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
		//test random example
		if(trainProb[5] != (double)376/3823){
			System.err.println("Probability miscalculation; should be " + (double)376/3823 + " (is " + trainProb[5] + ")");
			return false;
		}
		
		System.out.println("Probability calculation tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests the calcCondProbs() method
	 * @return: returns true if all tests pass
	 */
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
	
	
	
	/**
	 * Tests the classifyInst() method
	 * @return: returns true if all tests pass
	 */
	public static boolean testClassifyInst(){
		System.out.println("Testing instance classification...");
		
		//random instances pulled from training data
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
	
	
	/**
	 * Tests the naiveBayesClass() method
	 * @param testFile: file to be extracted
	 * @param verbose: if test should print additional information
	 * @return: returns true if all tests pass
	 */
	public static boolean testNaiveBayesClass(String testFile, boolean verbose){
		System.out.println("Testing naive Bayes classification...");
		
		int[][] conMtrx = naiveBayesClass(testFile);
			
		for(int i = 0; i < conMtrx.length; i++){
			if(conMtrx[i][TP] == 0 || conMtrx[i][TP] == testCt[i]){
				System.err.println("Perfect accuracy or failure very unlikely for " + i + ": " + conMtrx[i][TP]);
				return false;
			}
			if(conMtrx[i][TP] + conMtrx[i][TN] + conMtrx[i][FP] + conMtrx[i][FN] != testTtl){
				System.err.println("Confusion matrix for digit " + i + " should sum to total instance count (" + testTtl + "): " +
						digitConMtrxToString(conMtrx[i]));
				return false;
			}
		}
		
		if(verbose) printConMtrx(conMtrx);
		
		System.out.println("Naive Bayes classification tests pass! :)\n");
		return true;
	}
	
	/**
	 * Driver for unit tests
	 * @param verbose: if tests should print additional information
	 * @param trainFile: training file to be extracted
	 * @param testFile: test file to be extracted
	 * @return: returns true if all tests pass
	 */
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
