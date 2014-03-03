/**
 * "K-means Clustering"
 * Copyright © 2014 Brianna Shade
 * bshade@pdx.edu
 *
 * Kmeans.java
 * TODO details on this class
 */
package hw4Kmeans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Kmeans {
	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static ArrayList<int[]> testData = new ArrayList<int[]>();
	private static final boolean TRAIN = true;
	private static final boolean TEST = false;
	
	
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

	//TODO
	public static void bestIteration(){
		//for i = 1 : 5
		//	choose k random initial cluster centers
		//	until cluster centers stop moving, update centers
		//	calculate the SSE
		//	keep the iteration with the smallest SSE
		//calculate SSS and mean entropy
	}
	
	//TODO
	public static void classifyData(){
		//calculate accuracy and confusion matrix
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(!runTests(true, args[0], args[1])) System.exit(0);
		
		extractData(args[0], TRAIN);
		bestIteration();
		extractData(args[1], TEST);
		classifyData();
	}
	
	
	/**** Unit Tests ****/

	/**
	 * Resets data structures
	 */
	public static void clearData(){
		trainData.clear();
		testData.clear();
	}
	
	//TODO
	public static boolean testBestIteration(){
		System.out.println("Testing best iteration determination...");
		
		System.out.println("Best iteration determination tests pass! :)\n");
		return true;
	}
	
	//TODO
	public static boolean testClassifyData(){
		System.out.println("Testing data classification...");
		
		System.out.println("Data classification tests pass! :)\n");
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
		if(!testBestIteration()) return false;
		extractData(testFile, TEST);
		if(!testClassifyData()) return false;
		return true;
	}

}
