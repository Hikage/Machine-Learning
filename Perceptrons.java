/**
 * "Perceptrons"
 * CS545: Machine Learning - HW1
 * Copyright © 2014 Brianna Shade
 * bshade@pdx.edu
 *
 * Perceptrons.java
 * TODO details on this class
 */
package hw1Perceptrons;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class Perceptrons {

	private static final String inputFile = "src/hw1Perceptrons/letter-recognition.data";
	private static final double lrate = 0.2;
	private static final int bias = 1;
	private static final int TRAIN = 0, TEST = 1;
	private static final int TP = 0, FP = 1, FN = 2, TN = 3;
	private static ArrayList<String[]> trainData = new ArrayList<String[]>();
	private static ArrayList<String[]> testData = new ArrayList<String[]>();
	private static double[] weights = new double[17];
	private static int numEpochs = 0;
	
	
	/**
	 * Reads input file and generates training and test data sets
	 * @param testCase: char testing against
	 * @param target: char seeking
	 */
	public static void extractData(char testCase, char target){
		testCase = Character.toUpperCase(testCase);
		target = Character.toUpperCase(target);
		
		int dataSet = TRAIN;
		
		FileReader fr;
		BufferedReader buff;
		
		try{
			fr = new FileReader(inputFile);
			buff = new BufferedReader(fr);
			String line;
			while((line = buff.readLine()) != null && line != ""){
				if(Character.toUpperCase(line.charAt(0)) == testCase ||
						Character.toUpperCase(line.charAt(0)) == target){
					
					String[] instance = line.split(",");
					
					//add to training or test
					if(dataSet == TRAIN) trainData.add(instance);
					else testData.add(instance);
					dataSet = (dataSet + 1) % 2;
				}
			}
		}
		catch(IOException ex){
			System.err.println("Oh no! An error occurred!\n Error: " + ex);
			System.exit(0);
		}
	}
	
	/**
	 * Scales data values to be within 0 and 1
	 * @param testInstance: Used only for unit tests
	 */
	public static void scaleFeatures(String[] testInstance){
		//divide each feature value by 15
		for(String[] instance : trainData){
			for(int i = 1; i < instance.length; i++){
				if(Character.isDigit(instance[i].charAt(0)))
					instance[i] = Double.toString(Double.parseDouble(instance[i])/15);
				else{
					System.err.println("Invalid feature value: " + instance[i] + ".  Should be an int");
					System.exit(0);
				}
			}
		}
		for(String[] instance : testData){
			for(int i = 1; i < instance.length; i++){
				if(Character.isDigit(instance[i].charAt(0)))
					instance[i] = Double.toString(Double.parseDouble(instance[i])/15);
				else{
					System.err.println("Invalid feature value: " + instance[i] + ".  Should be an int");
					System.exit(0);
				}
			}
		}
		
		if(testInstance != null)
			for(int i = 1; i < testInstance.length; i++)
				testInstance[i] = Double.toString(Double.parseDouble(testInstance[i])/15);
	}
	
	/**
	 * Initialize weights randomly, setting the first value to the bias
	 */
	public static void initializeWeights(){
		weights[0] = bias;

		//for each of the 16 weights, generate a random double
		for(int i = 1; i < weights.length; i++){
			boolean rand = new Random().nextBoolean();
			int negOffset = 1;
			if(rand) negOffset *= -1;
			weights[i] = new Random().nextDouble() * negOffset;
		}
	}
	
	/**
	 * Train on a specific instance example
	 * @param type: training data, testing data, or unit test
	 * @param index: which instance we're processing
	 * @param testInstance: unit test sample data
	 * @param testWeights: unit test sample weights
	 * @return: the sgn value of the perceptron on the given instance - 1 if deemed a match, -1 otherwise
	 */
	public static double processInstance(int type, int index, String[] testInstance, double[] testWeights){		
		//calculate sgn value
		double sgn = weights[0];
		for(int i = 1; i < weights.length; i++){
			if(type == TRAIN)
				sgn += (weights[i] * Double.parseDouble(trainData.get(index)[i]));
			else if(type == TEST)
				sgn += (weights[i] * Double.parseDouble(testData.get(index)[i]));
			else
				sgn += (testWeights[i] * Double.parseDouble(testInstance[i]));
		}
		
		return sgn;
	}
	
	/**
	 * Allows for a variable threshold with which to classify
	 * instance results
	 * @param sgn: sgn value of instance
	 * @param thresh: theshold against which to classify
	 * @return: returns 1 if deemed a positive match, -1 otherwise
	 */
	public static int classifyInstance(double sgn, double thresh){
		if(sgn > thresh) return 1;
		else return -1;
	}
	
	/**
	 * Cycles through each training example to calculate new weights
	 * @param target: character sought
	 * @param test: used for unit test printing
	 * @return: accuracy value of epoch
	 */
	public static double trainEpoch(char target, boolean test){
		if(test) printInstance(weights);
		int tlAcc = 0;
		for(int i = 0; i < trainData.size(); i++){
			double sgn = processInstance(TRAIN, i, null, null);
			
			int result, tar;
			if(trainData.get(i)[0].charAt(0) == target) tar = 1;
			else tar = -1;

			int classif = classifyInstance(sgn, 0);
			tlAcc += (1 - Math.abs(tar - classif)/2);
			
			if(test){
				System.out.print("Result: " + classif + "; Target: " + tar + "; ");
				printInstance(trainData.get(i));
			}
			
			//update weights after each instance
			//instances classified correctly will result in a no-op
			weights[0] += lrate * (tar - classif);
			for(int j = 1; j < weights.length; j++){
				weights[j] += lrate * (tar - classif) * Double.parseDouble(trainData.get(i)[j]);
			}
			
			if(test) printInstance(weights);
		}
		
		return (tlAcc * 1.0)/trainData.size();
	}
	
	/**
	 * Process epoch and calculate accuracy
	 * Stop once weights converge
	 * @param target: character sought
	 * @param convThresh: theshold past which to stop processing epochs
	 * @param test: used only for unit testing
	 * @return: returns the final accuracy
	 */
	public static double cycleEpochs(char target, double convThresh, boolean test){
		double avgDiff = 0;
		double acc = 0.0;
		numEpochs = 0;
		do{
			double[] prevWeights = weights.clone();
			acc = trainEpoch(target, false);
			//calculate weight difference
			double[] wDiff = new double[weights.length];
			for(int i = 0; i < wDiff.length; i++){
				wDiff[i] = weights[i] - prevWeights[i];
				avgDiff += wDiff[i];
			}
			avgDiff /= wDiff.length;
			
			numEpochs++;
			
			if(test){
				DecimalFormat df = new DecimalFormat("#.##");
				System.out.println(df.format(avgDiff) + "; " + df.format(acc));
			}
		}while (Math.abs(avgDiff) >= convThresh);
		
		return acc;
	}
	
	/**
	 * Test current perceptron against test data set
	 * Returns current accuracy
	 * @param target: character sought
	 * @return: returns an array representing the the scores and true classes
	 */
	public static ArrayList<double[]> testData(char target){
		ArrayList<double[]> testScores = new ArrayList<double[]>();
		
		//process each testing instance
		for(int i = 0; i < testData.size(); i++){
			double[] iResults = new double[2];
			double sgn = processInstance(TEST, i, null, null);
			iResults[0] = sgn;
			
			//calculate running confusion matrix
			if(testData.get(i)[0].charAt(0) == target) iResults[1] = 1;
			else iResults[1] = -1;
			
			testScores.add(iResults);
		}
		
		return testScores;
	}
	
	/**
	 * Calculates the current confusion matrix
	 * based on scores, actuals, and a provided threshold
	 * @param testScores: test data scores and true classes
	 * @param cthresh: threshold against which to classify
	 * @return: returns the confusion matrix
	 */
	public static int[] calcConfusionMatrix(ArrayList<double[]> testScores, double cthresh){
		int[] conMtrx = new int[4];
		for(int i = 0; i < testScores.size(); i++){			
			int classif = classifyInstance(testScores.get(i)[0], cthresh);
			
			//calculate confusion matrix
			if(testScores.get(i)[1] == 1){
				if(classif > 0) conMtrx[TP]++;
				else conMtrx[FN]++;
			}
			else{
				if(classif > 0) conMtrx[FP]++;
				else conMtrx[TN]++;
			}
		}
		return conMtrx;
	}
	
	public void printResults(int[] conMtrx){
		//print epochs and accuracy
		double teacc = (conMtrx[TP] + conMtrx[TN]) / (testData.size()  * 1.0);
		DecimalFormat df = new DecimalFormat("###.##%");
		System.out.println("Epochs: " + numEpochs + "; Accuracy: " + df.format(teacc));
		
		//print confusion matrix
		System.out.println("TP: " + conMtrx[TP] + "; FP: " + conMtrx[FP] + "; FN: "
				+ conMtrx[FN] + "; TN: " + conMtrx[TN]);
		
		//print precision and recall
		double precision = (conMtrx[TP] * 1.0) / (conMtrx[TP] + conMtrx[FP]);
		double recall = (conMtrx[TP] * 1.0) / (conMtrx[TP] + conMtrx[FN]);
		System.out.println("Precision: " + precision + "; Recall: " + recall);
		
		//print data for ROC curve
		double TPR = (conMtrx[TP] * 1.0) / (conMtrx[TP] + conMtrx[FN]);
		double FPR = (conMtrx[FP] * 1.0) / (conMtrx[TN] + conMtrx[FP]);
	}
	
	public static void main(String args[]){
		if(!runUnitTests()) System.exit(0);
		
		//initializeWeights()
		//extract all As and Bs from data
		//for first half of extracted data, train
		//test on second half
	}
	
	
	/**** Unit Tests ****/
	public static boolean runUnitTests(){
		if(!testExtractData(false)) return false;
		if(!testScaleFeatures(false)) return false;
		if(!testInitializeWeights(false)) return false;
		if(!testProcessInstance()) return false;
		if(!testClassifyInstance()) return false;
		if(!testTrainEpoch(false)) return false;
		if(!testCycleEpochs(false)) return false;
		if(!testTestData(false)) return false;
		if(!testCalcConfusionMatrix(false)) return false;
		return true;
	}
	
	/**
	 * Print given instance
	 * @param instance: instance to print
	 */
	public static void printInstance(String[] instance){
		System.out.print(instance[0]);
		DecimalFormat df = new DecimalFormat("#.##");
		for(int i = 1; i < instance.length; i++) System.out.print("," + df.format(Double.parseDouble(instance[i])));
		System.out.println();
	}
	
	/**
	 * Print given instance
	 * @param instance: instance to print
	 */
	public static void printInstance(double[] instance){
		System.out.print(instance[0]);
		DecimalFormat df = new DecimalFormat("#.##");
		for(int i = 1; i < instance.length; i++) System.out.print("," + df.format(instance[i]));
		System.out.println();
	}
	
	/**
	 * Resets global data arrays
	 */
	public static void clearData(){
		trainData.clear();
		testData.clear();
	}
	
	public static boolean testExtractData(boolean printExamples){
		System.out.println("\nTesting data extraction...");
		
		extractData('B', 'A');
		
		if(printExamples){
			System.out.println("\nSample Training Data:");
			for(int i = 0; i < 5; i++) printInstance(trainData.get(i));
			
			System.out.println("\nSample Test Data:");
			for(int i = 0; i < 5; i++) printInstance(testData.get(i));
			
			System.out.println();
		}
		
		for(int i = 0; i < 5; i++){
			int rand = new Random().nextInt(trainData.size());
			String[] instance = trainData.get(rand);
			char type = instance[0].charAt(0);
			if(type != 'A' && type != 'B'){
				System.err.println("Invalid training instance extracted: " + type);
				return false;
			}
			if(instance.length != 17){
				System.err.println("Invalid number of training instance features: " + instance.length);
				printInstance(instance);
				return false;
			}
			
			rand = new Random().nextInt(testData.size());
			instance = testData.get(rand);
			type = instance[0].charAt(0);
			if(type != 'A' && type != 'B'){
				System.err.println("Invalid test instance extracted: " + type);
				return false;
			}
			if(instance.length != 17){
				System.err.println("Invalid number of training instance features: " + instance.length);
				printInstance(instance);
				return false;
			}
		}
		
		System.out.println("Data extraction tests pass! :)");
		return true;
	}

	public static boolean testScaleFeatures(boolean printExamples){
		System.out.println("\nTesting feature scaling...");
		
		String[] testInstance = {"A","0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		scaleFeatures(testInstance);
		
		if(printExamples){
			System.out.println("\nSample Training Data:");
			for(int i = 0; i < 5; i++) printInstance(trainData.get(i));
			
			System.out.println("\nSample Test Data:");
			for(int i = 0; i < 5; i++) printInstance(testData.get(i));
			
			System.out.println();
		}
		
		DecimalFormat df = new DecimalFormat("#.##");
		if(!(df.format(Double.parseDouble(testInstance[2])).equals("0.07"))){
			System.err.println("Feature not scaled correctly. Expected: 0.07; Got: " + df.format(Double.parseDouble(testInstance[2])));
			return false;
		}
		if(!(df.format(Double.parseDouble(testInstance[6])).equals("0.33"))){
			System.err.println("Feature not scaled correctly. Expected: 0.33; Got: " + df.format(Double.parseDouble(testInstance[6])));
			return false;
		}
		if(!(df.format(Double.parseDouble(testInstance[16])).equals("1"))){
			System.err.println("Feature not scaled correctly. Expected: 1.00; Got: " + df.format(Double.parseDouble(testInstance[16])));
			return false;
		}
		
		for(int i = 0; i < 5; i++){
			int rand1 = new Random().nextInt(trainData.size());
			int rand2 = new Random().nextInt(trainData.get(0).length-1) + 1;
			
			double feature = Double.parseDouble(trainData.get(rand1)[rand2]);
			if(feature < 0 || feature > 1){
				System.err.println("Training feature incorrectly scaled: " + feature);
				return false;
			}
			
			rand1 = new Random().nextInt(testData.size());
			rand2 = new Random().nextInt(testData.get(0).length-1) + 1;
			feature = Double.parseDouble(testData.get(rand1)[rand2]);
			if(feature < 0 || feature > 1){
				System.err.println("Test feature incorrectly scaled: " + feature);
				return false;
			}
		}
		
		System.out.println("Feature scaling tests pass! :)");
		return true;
	}

	public static boolean testInitializeWeights(boolean printWeights){
		System.out.println("\nTesting weight initialization...");

		initializeWeights();
		
		//Test for properly-set bias
		if(weights[0] != bias){
			System.err.println("First index of weights should be the bias (" + bias + "): " + weights[0]);
			return false;
		}
		if(printWeights) printInstance(weights);
		
		boolean neg = false;
		boolean pos = false;
		
		//Test for correct range
		for(int i = 1; i < weights.length; i++){
			if(weights[i] < -1 || weights[i] > 1){
				System.err.println("Invalid weight at index " + i + ": " + weights[i]);
				return false;
			}
			if(weights[i] > 0) neg = true;
			if(weights[i] < 0) pos = true;
		}
		
		//Test that weights are both negative and positive
		if(!neg){
			System.err.println("All weights are positive");
			return false;
		}
		if(!pos){
			System.err.println("All weights are negative");
			return false;
		}
		
		//Test for equivalent weights (random values shouldn't be the same)
		int rand1 = new Random().nextInt(weights.length);
		int rand2 = new Random().nextInt(weights.length);
		int rand3 = new Random().nextInt(weights.length);
		if(weights[rand1] == weights[rand2] && weights[rand2] == weights[rand3]){
			System.err.println("Weights at indexes " + rand1 + ", " + rand2 + ", and " + rand3 + "are equal: " + weights[rand1]);
			return false;
		}
		
		System.out.println("Weight initialization tests pass! :)");
		return true;
	}
	
	public static boolean testProcessInstance(){
		System.out.println("\nTesting instance processing...");
		
		String[] testInstance = {"A","0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		scaleFeatures(testInstance);
		double[] negWeights = {1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
		double[] posWeights = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		double[] eqWeights = {1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1};
		if(processInstance(3, 0, testInstance, negWeights) > 0){
			System.err.println("Result with negative weights should be negative");
			return false;
		}
		if(processInstance(3, 0, testInstance, posWeights) < 0){
			System.err.println("Result with positive weights should be positive");
			return false;
		}
		if(processInstance(3, 0, testInstance, eqWeights) < 0){
			System.err.println("Result with equal weights should be positive");
			return false;
		}
		
		System.out.println("Instance processing tests pass! :)");
		return true;
	}
	
	public static boolean testClassifyInstance(){
		System.out.println("\nTesting instance classification...");
		
		String[] testInstance = {"A","0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		scaleFeatures(testInstance);
		double[] negWeights = {1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
		double[] posWeights = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		double[] eqWeights = {1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1, -1, 1};
		if(classifyInstance(processInstance(3, 0, testInstance, negWeights), 0) != -1){
			System.err.println("Result with negative weights should be negative");
			return false;
		}
		if(classifyInstance(processInstance(3, 0, testInstance, posWeights), 0) != 1){
			System.err.println("Result with positive weights should be positive");
			return false;
		}
		if(classifyInstance(processInstance(3, 0, testInstance, eqWeights), 0) != 1){
			System.err.println("Result with equal weights should be positive");
			return false;
		}
		
		System.out.println("Instance classification tests pass! :)");
		return true;
	}

	public static boolean testTrainEpoch(boolean printInstances){
		System.out.println("\nTesting epoch training...");

		clearData();
		extractData('A', 'A');
		scaleFeatures(null);
		initializeWeights();
		trainEpoch('A', printInstances);
		
		System.out.println("Epoch training tests pass! :)");
		return true;
	}
	
	public static boolean testCycleEpochs(boolean printAvgWts){
		System.out.println("\nTesting epoch cycling...");

		clearData();
		extractData('A', 'B');
		scaleFeatures(null);
		initializeWeights();
		double acc = cycleEpochs('A', 0.02, printAvgWts);
		if(acc < .5){
			System.err.println("Accuracy shouldn't be this low after full epoch cycling: " + acc);
			return false;
		}
		if(acc > 1){
			System.err.println("Accuracy shouldn't be greater than 100%: " + acc);
			return false;
		}
		
		System.out.println("Epoch cycling tests pass! :)");
		return true;
	}

	public static boolean testTestData(boolean printResults){
		System.out.println("\nTesting test data...");

		clearData();
		extractData('A', 'B');
		scaleFeatures(null);
		initializeWeights();
		cycleEpochs('A', 0.02, false);
		
		ArrayList<double[]> testScores = testData('A');
		
		if(printResults){
			for(int i = 0; i < 10; i++){
				System.out.println("Score: " + testScores.get(i)[0] + "; Classification: " + testScores.get(i)[1]);
			}
		}
		
		System.out.println("Test data tests pass! :)");
		return true;
	}
	
	public static boolean testCalcConfusionMatrix(boolean printResults){
		System.out.println("\nTesting confusion matrix calculation...");
		
		clearData();
		extractData('A', 'B');
		scaleFeatures(null);
		initializeWeights();
		double tracc = cycleEpochs('A', 0.02, false);
		
		ArrayList<double[]> testScores = testData('A');
		
		//Test 0 threshold
		int[] conMtrx = calcConfusionMatrix(testScores, 0);		
		double teacc = (conMtrx[TP] + conMtrx[TN]) / (testData.size()  * 1.0);
		DecimalFormat df = new DecimalFormat("###.##%");
		System.out.println("TP: " + conMtrx[TP] + "; FP: " + conMtrx[FP] + "; FN: "
				+ conMtrx[FN] + "; TN: " + conMtrx[TN] + "; Accuracy: " + df.format(teacc));		
		
		if(printResults){
			System.out.println("Training accuracy: " + df.format(tracc) + "; Testing accuracy: " + df.format(teacc));			
		}
		
		if(Math.abs(tracc - teacc) > .05){
			System.err.println("Accuracy differences shouldn't be greater than 5% between training and testing: "
					+ df.format(tracc) + "; " + df.format(teacc));
			return false;
		}
		
		
		//Test large negative threshold
		conMtrx = calcConfusionMatrix(testScores, -20);		
		teacc = (conMtrx[TP] + conMtrx[TN]) / (testData.size()  * 1.0);
		if(printResults) System.out.println("TP: " + conMtrx[TP] + "; FP: " + conMtrx[FP] + "; FN: "
				+ conMtrx[FN] + "; TN: " + conMtrx[TN] + "; Accuracy: " + df.format(teacc));
		
		if(conMtrx[TN] + conMtrx[FN] > 0){
			System.err.println("Nothing should be classified as negative with such a low threshold" +
					"TP: " + conMtrx[TP] + "; FP: " + conMtrx[FP] + "; FN: "
					+ conMtrx[FN] + "; TN: " + conMtrx[TN] + "; Accuracy: " + df.format(teacc));
			return false;
		}
		
		//Test large negative threshold
		conMtrx = calcConfusionMatrix(testScores, 20);		
		teacc = (conMtrx[TP] + conMtrx[TN]) / (testData.size()  * 1.0);
		if(printResults) System.out.println("TP: " + conMtrx[TP] + "; FP: " + conMtrx[FP] + "; FN: "
				+ conMtrx[FN] + "; TN: " + conMtrx[TN] + "; Accuracy: " + df.format(teacc));
		
		if(conMtrx[TP] + conMtrx[FP] > 0){
			System.err.println("Nothing should be classified as positive with such a high threshold:" +
					"TP: " + conMtrx[TP] + "; FP: " + conMtrx[FP] + "; FN: "
					+ conMtrx[FN] + "; TN: " + conMtrx[TN] + "; Accuracy: " + df.format(teacc));
			return false;
		}
		
		
		System.out.println("Confusion matrix calculation tests pass! :)");
		return true;
	}
}
