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

public class Perceptrons {

	private final String inputFile = "letter-recognition.data";
	private final double lrate = 0.2;
	private final int bias = 1;
	private int[] inputs = new int[17];
	private double[] weights = new double[17];
	
	//TODO: scale data values
	/*
	 * Scales data values to be within 0 and 1
	 */
	public void scaleValues(){
		//divide each feature value by 15
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
		//initializeWeights()
		//extract all As and Bs from data
		//for first half of extracted data, train
		//test on second half
	}
}
