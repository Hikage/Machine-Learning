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
import java.util.StringTokenizer;

public class Perceptrons {

	private static final String inputFile = "src/hw1Perceptrons/letter-recognition.data";
	private static final double lrate = 0.2;
	private static final int bias = 1;
	private static final int TRAIN = 0;
	private static final int TEST = 1;
	private static ArrayList<String[]> trainData = new ArrayList<String[]>();
	private static ArrayList<String[]> testData = new ArrayList<String[]>();
	private static double[] weights = new double[17];
	
	
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
		
		if(testInstance != null){
			for(int i = 1; i < testInstance.length; i++) testInstance[i] = Double.toString(Double.parseDouble(testInstance[i])/15);
		}
	}
	
	//TODO: randomly generate initial +/- weights
	/*
	 * Initialize weights randomly
	 */
	public void initializeWeights(){
		//for each of the 16 weights, generate a random double
	}
	
	//TODO: process training example
	/*
	 * Train on a specific instance example
	 * Returns perceptron response
	 */
	public int processInstance(){
		int response = 0;
		//read line from file
		//populate array with scaled feature values
		//calculate sgn value
		//if sgn > 0, return 1, else -1
		return response;
	}
	
	//TODO: process epoch
	/*
	 * Cycles through each training example to calculate
	 * new weights
	 */
	public void trainEpoch(){
		//processInstance()
		//if incorrect classification,  update weights
	}
	
	//TODO: epoch cycling
	/*
	 * Process epoch and calculate accuracy
	 * Stop once weights converge
	 */
	public void cycleEpochs(){
		//avgWgt = 1
		//trainEpoch()
		//calculate weight difference
		//if weights have converged, stop
	}
	
	//TODO: test against test set
	/*
	 * Test current perceptron against test
	 * data set, returning current accuracy
	 */
	public int[] testData(){
		int[] results = new int[4];
		final int TP = 0, FP = 1, FN = 2, TN = 3;
		//for each line, processInstance()
		//calculate running confusion matrix
		return results;
	}
	
	public void printResults(int[] results){
		//print accuracy
		//print confusion matrix
		//print precision and recall
		//print data for ROC curve
	}
	
	public static void main(String args[]){
		runUnitTests();
		
		//initializeWeights()
		//extract all As and Bs from data
		//for first half of extracted data, train
		//test on second half
	}
	
	
	/**** Unit Tests ****/
	public static boolean runUnitTests(){
		testExtractData(false);
		testScaleFeatures(false);
		return true;
	}
	
	public static boolean testExtractData(boolean printExamples){
		System.out.println("\nTesting data extraction...");
		
		extractData('B', 'A');
		
		if(printExamples){
			System.out.println("\nSample Training Data:");
			for(int i = 0; i < 5; i++){
				String[] instance = trainData.get(i);
				for(String feature : instance) System.out.print(feature + ",");
				System.out.println();
			}
			System.out.println("\nSample Test Data:");
			for(int i = 0; i < 5; i++){
				String[] instance = testData.get(i);
				for(String feature : instance) System.out.print(feature + ",");
				System.out.println();
			}
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
				for(String feature : instance) System.out.print(feature);
				System.out.println();
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
				for(String feature : instance) System.out.print(feature);
				System.out.println();
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
			for(int i = 0; i < 5; i++){
				String[] instance = trainData.get(i);
				for(String feature : instance) System.out.print(feature + ",");
				System.out.println();
			}
			System.out.println("\nSample Test Data:");
			for(int i = 0; i < 5; i++){
				String[] instance = testData.get(i);
				for(String feature : instance) System.out.print(feature + ",");
				System.out.println();
			}
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
}
