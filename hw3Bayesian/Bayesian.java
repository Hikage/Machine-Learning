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
	private static int[] trainCt = {376, 389, 380, 389, 387, 376, 377, 387, 380, 382};
	private static int trainTtl = 3823;
	private static double[] trainProb = new double[trainCt.length];
	private static double[][][] CPtrain = new double[64][17][10];
	private static double[] testProb = {178.0, 182.0, 177.0, 183.0, 181.0, 182.0, 181.0, 179.0, 174.0, 180.0};
	
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
			trainProb[i] = trainCt[i] / (double)trainTtl;
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
	
	public static int classifyInst(int inst){
		int[] instance = testData.get(inst);
		
		if(testProb[0] > 1){
			System.err.println("testProb hasn't yet been initialized fully with probabilities!");
			System.exit(0);
		}
		
		double[] classProbs = testProb.clone();
		
		//iterate through each possible classification (0-9) and through each feature
		for(int i = 0; i < classProbs.length; i++){
			for(int j = 0; j < instance.length-1; j++){
				classProbs[i] *= CPtrain[j][instance[j]][i];	//multiply in probability att j has value of instance[j] for class i
			}
		}
		
		//find classification with highest probability
		double max = 0;
		int clsif = -1;
		for(int i = 0; i < classProbs.length; i++){
			classProbs[i] = Math.log(classProbs[i]);
			if(classProbs[i] > max){
				max = classProbs[i];
				clsif = i;
			}
		}
		
		return clsif;
	}
	
	public static void naiveBayesClass(String testFile){
		for(int i = 0; i < testProb.length; i++) testProb[i] /= 1797;
		
		extractData(testFile, false);
		
		int[] correct = new int[testProb.length];
		for(int i = 0; i < testData.size(); i++){
			int clsif = classifyInst(i);
			if(clsif < 0){
				System.err.println("Error finding maximum probability for classification");
				System.exit(0);
			}
			
			if(clsif == testData.get(i)[testData.get(i).length-1]) correct[clsif]++;
		}
		
		for(int i = 0; i < correct.length; i++){
			System.out.println(i + ": " + correct[i]);
		}
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
	
	public static boolean runTests(boolean verbose, String trainFile, String testFile){
		clearData();
		extractData(trainFile, true);
		if(!testCalcProbs()) return false;
		if(!testCalcCondProbs()) return false;
		naiveBayesClass(testFile);
		return true;
	}

}
