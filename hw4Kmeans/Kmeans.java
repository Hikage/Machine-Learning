/**
 * "K-means Clustering"
 * Copyright © 2014 Brianna Shade
 * bshade@pdx.edu
 *
 * Kmeans.java
 * This classifies numerical digits through a K-means clustering algorithm
 */
package hw4Kmeans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Kmeans {
	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static ArrayList<int[]> testData = new ArrayList<int[]>();
	private static int[][] clusters;
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

	/**
	 * Initializes preliminary cluster centers (randomly from training instances)
	 * @param k: number of clusters to initialize
	 */
	public static void initializeClusters(int k){
		clusters = new int[k][trainData.get(0).length];
		
		for(int i = 0; i < k; i++){
			int rand = new Random().nextInt(trainData.size());		//extract random instances to use as cluster centers
			clusters[i] = trainData.get(rand);
		}
	}
	
	/**
	 * Given an input training instance, calculates the closest cluster center
	 * @param inst: instance to be classified
	 * @return: returns the index of the closest cluster center
	 */
	public static int assignCluster(int[] inst){
		int bestcl = 0;
		double besteudist = Double.POSITIVE_INFINITY;
		
		//calculate the euclidean distance for each cluster center to find the closest
		for(int i = 0; i < clusters.length; i++){
			double eudist = 0.0;
			for(int j = 0; j < clusters[0].length; j++){
				eudist += Math.pow(inst[j] - clusters[i][j], 2);
			}
			eudist = Math.sqrt(eudist);
			
			if(eudist < besteudist){
				besteudist = eudist;
				bestcl = i;
			}
		}
		
		return bestcl;
	}
	
	//TODO
	public static double updateClusters(){
		double var = 0.0;
		ArrayList[] clustmembers = new ArrayList[clusters.length];
		
		for(int[] inst : trainData){
			//classify each instance, adding its index to the appropriate cluster
		}
		
		//return amount of variance from previous cluster set
		return var;
	}
	
	//TODO
	public static double calculateSSE(){
		double SSE = 0.0;
		
		return SSE;
	}
	
	//TODO
	public static double calculateSSS(){
		double SSS = 0.0;
		
		return SSS;
	}
	
	//TODO
	public static double calculateMEntropy(){
		double mEntropy = 0.0;
		
		return mEntropy;
	}
	
	//TODO
	public static void bestIteration(int k){
		int itCap = 100;
		double thresh = 1.0;
		
		double SSE = 0.0;
		for(int i = 0; i < 5; i++){
			initializeClusters(k);
			for(int j = 0; j < itCap; j++){
				if(updateClusters() < thresh) break;	//until cluster centers stop moving, update centers
			}
			double newSSE = calculateSSE();
			if(newSSE < SSE){
				SSE = newSSE;
				//maintain cluster set
			}
		}
		double SSS = calculateSSS();
		double mEntropy = calculateMEntropy();
	}
	
	//TODO
	public static void classifyData(){
		//calculate accuracy and confusion matrix
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3){
			System.err.println("Usage: java hw4Kmeans.Kmeans inputdata.train inputdata.test K-value");
			System.exit(0);
		}
		
		String trainFile = args[0], testFile = args[1];
		int k = Integer.parseInt(args[2]);
		
		if(!runTests(true, trainFile, testFile, k)) System.exit(0);
		
		extractData(trainFile, TRAIN);
		bestIteration(k);
		extractData(testFile, TEST);
		classifyData();
	}
	
	/**** Printing Methods ****/
	
	public static String instToString(int[] inst){
		String instance = "";
		for(int feature : inst){
			instance += feature + ",";
		}
		return instance;
	}
	
	/**
	 * Prints cluster set
	 */
	public static void printClusters(){
		System.out.println("Clusters:");
		for(int[] cluster : clusters){
			System.out.println(instToString(cluster));
		}
	}
	
	
	/**** Unit Tests ****/

	/**
	 * Resets data structures
	 */
	public static void clearData(){
		trainData.clear();
		testData.clear();
	}
	
	/**
	 * Tests initializeClusters() method
	 * @param k: number of clusters to be initialized
	 * @return: returns true if all tests pass
	 */
	public static boolean testInitializeClusters(int k){
		System.out.println("Testing cluster initialization...");
		
		initializeClusters(k);
		//test initialization size
		if(clusters.length != k){
			System.err.println("Clusters initialized to the wrong size: " + k + " (k) " + clusters.length + " (# clusters)");
			return false;
		}
		
		//test two random cluster centers for equivalence
		int rand1 = new Random().nextInt(clusters.length);
		int rand2;
		do rand2 = new Random().nextInt(clusters.length);
		while(rand1 == rand2);
		
		boolean equals = true;
		for(int i = 0; i < clusters[rand1].length; i++){
			equals = equals && (clusters[rand2][i] == clusters[1][i]);
			if(!equals) break;
		}
		if(equals){
			System.err.println("Two cluster centers should not be equivalent! (clusters " + rand1 + " and " + rand2);
			return false;
		}
		
		System.out.println("Cluster initialization tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests assignCluster() method
	 * @param verbose: whether testing should print extra information
	 * @return: returns true if all tests pass
	 */
	public static boolean testAssignCluster(boolean verbose){
		System.out.println("Testing cluster assignment...");
		
		if(verbose) printClusters();
		
		int[] inst1 = {0,0,0,2,14,1,0,0,0,0,0,10,12,0,0,0,0,0,8,15,1,2,1,0,0,3,15,5,0,12,7,0,0,10,14,0,6,16,2,0,0,8,16,16,16,12,0,0,0,0,2,4,16,5,0,0,0,0,0,2,13,0,0,0,4};
		int clust = assignCluster(inst1);
		if(verbose) System.out.println("\nInstance: " + instToString(inst1) + "\nCluster: " + clust);
		
		int[] inst2 = {0,0,1,12,16,14,2,0,0,0,13,11,3,16,5,0,0,4,14,0,0,15,6,0,0,6,12,8,13,16,5,0,0,0,9,12,4,10,8,0,0,0,3,0,0,11,5,0,0,0,16,14,5,15,4,0,0,0,3,12,16,11,1,0,9};
		clust = assignCluster(inst2);
		if(verbose) System.out.println("\nInstance: " + instToString(inst2) + "\nCluster: " + clust);
		
		int[] inst3 = {0,0,3,9,14,9,0,0,0,5,16,14,5,0,0,0,0,12,11,3,0,0,0,0,0,13,16,12,1,0,0,0,0,4,11,13,8,0,0,0,0,0,0,7,11,0,0,0,0,0,1,12,12,0,0,0,0,0,2,15,7,0,0,0,5};
		clust = assignCluster(inst3);
		if(verbose) System.out.println("\nInstance: " + instToString(inst3) + "\nCluster: " + clust);
		
		
		System.out.println("Cluster assignment tests pass! :)\n");
		return true;
	}
	
	//TODO
	public static boolean testUpdateClusters(){
		System.out.println("Testing cluster updates...");
		
		System.out.println("Cluster update tests pass! :)\n");
		return true;
	}
	
	//TODO
	public static boolean testCalculateSSE(){
		System.out.println("Testing SSE calculation...");
		
		System.out.println("SSE calculation tests pass! :)\n");
		return true;
	}
	
	//TODO
	public static boolean testCalculateSSS(){
		System.out.println("Testing SSS calculation...");
		
		System.out.println("SSS calculation tests pass! :)\n");
		return true;
	}
	
	//TODO
	public static boolean testCalculateMEntropy(){
		System.out.println("Testing mean entropy calculation...");
		
		System.out.println("Mean entropy calculation tests pass! :)\n");
		return true;
	}
	
	//TODO
	public static boolean testBestIteration(int k){
		System.out.println("Testing best iteration determination...");
		
		System.out.println("Best iteration determination tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests extractData() method
	 * @param trainFile: training data file to be read in
	 * @param testFile: test data file to be read in
	 * @return: returns true if all tests pass
	 */
	public static boolean testExtractData(String trainFile, String testFile){
		System.out.println("Testing data extraction...");
		
		//test training data
		extractData(trainFile, TRAIN);
		if(trainData.size() != 3823){
			System.err.println("Wrong number of training instances read in: " + trainData.size());
			return false;
		}
		for(int i = 0; i < trainData.size(); i++){
			if(trainData.get(i)[0] != 0){
				System.err.println("First value for training instance " + i + "should be 0: " + trainData.get(i)[0]);
				return false;
			}
		}
		if(trainData.get(27)[33] != 2){		//random instance
			System.err.println("Value for training instance 27 feature 33 should be 2: " + trainData.get(27)[33]);
			return false;
		}
		
		//test test data
		extractData(testFile, TEST);
		if(testData.size() != 1797){
			System.err.println("Wrong number of test instances read in: " + testData.size());
			return false;
		}
		for(int i = 0; i < testData.size(); i++){
			if(testData.get(i)[0] != 0){
				System.err.println("First value for test instance " + i + "should be 0: " + testData.get(i)[0]);
				return false;
			}
		}
		if(testData.get(1385)[53] != 16){		//random instance
			System.err.println("Value for test instance 1385 feature 53 should be 16: " + testData.get(1385)[53]);
			return false;
		}
		
		System.out.println("Data extraction tests pass! :)\n");
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
	public static boolean runTests(boolean verbose, String trainFile, String testFile, int k){
		clearData();
		if(!testExtractData(trainFile, testFile)) return false;
		if(!testInitializeClusters(k)) return false;
		if(!testAssignCluster(verbose)) return false;
		if(!testUpdateClusters()) return false;
		if(!testCalculateSSE()) return false;
		if(!testCalculateSSS()) return false;
		if(!testCalculateMEntropy()) return false;
		if(!testBestIteration(k)) return false;
		if(!testClassifyData()) return false;
		return true;
	}

}
