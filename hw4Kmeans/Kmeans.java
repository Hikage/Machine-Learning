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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Kmeans {
	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static ArrayList<int[]> testData = new ArrayList<int[]>();
	private static int[][] clusters;
	private static ArrayList<ArrayList<Integer>> clustMembs = new ArrayList<ArrayList<Integer>>();
	//number of clusters<number of classification types<number of instances>>
	private static ArrayList<HashMap<Integer, Integer>> clustClass = new ArrayList<HashMap<Integer, Integer>>();
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
			for(int j = 0; j < clusters[0].length-1; j++){
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
	
	/**
	 * Updates each cluster center by averaging feature values of its members
	 * @param test: if this method is being tested
	 * @return: returns the average amount the clusters moved
	 */
	public static double updateClusters(boolean test){
		double var = 0.0;
		
		//initialize cluster ArrayLists
		for(int i = 0; i < clusters.length; i++){
			clustMembs.add(new ArrayList<Integer>());
			clustClass.add(new HashMap<Integer, Integer>());
		}		
		
		//classify each instance, adding its index to the appropriate cluster
		for(int i = 0; i < trainData.size(); i++){
			int clustAsmt = assignCluster(trainData.get(i));
			clustMembs.get(clustAsmt).add(i);
			
			//keep track of how many instances per class are in each cluster
			int clsif = trainData.get(i)[trainData.get(i).length-1];
			if(clustClass.get(clustAsmt).containsKey(clsif))
				clustClass.get(clustAsmt).put(clsif, clustClass.get(clustAsmt).get(clsif) + 1);
			else clustClass.get(clustAsmt).put(clsif, 1);
			
			//if(test) System.out.println(instToString(trainData.get(i)) + ": " + clustAsmt + "[" + clsif + "]" + clustClass.get(clustAsmt).get(clsif));
		}
		
		//if(test) System.out.println();
		
		//iterate through the clusters, averaging each's members to obtain the new cluster center
		for(int i = 0; i < clustMembs.size(); i++){
			if(test){
				System.out.println("Cluster: " + instToString(clusters[i]));
				//for(int node : clustmembers.get(i)) System.out.println("Inst: " + instToString(trainData.get(node)));
			}
			
			for(int j = 0; j < clusters[i].length-1; j++){			//iterate through each feature (last value is classification)
				double avg = 0.0;
				for(int node : clustMembs.get(i)){					//pull out each member's feature value for averaging
					avg += trainData.get(node)[j];
				}
				int newClustFeat = (int)Math.round(avg/clustMembs.get(i).size());
				var += (Math.abs(newClustFeat - clusters[i][j]));	//add up variance from each feature change
				clusters[i][j] = newClustFeat;
			}

			if(test) System.out.println("New cluster: " + instToString(clusters[i]));
		}
		
		//return amount of variance from previous cluster set - average across all features of all clusters
		return var/clusters.length/(clusters[0].length-1);
	}
	
	/**
	 * Calculates the squared error between two instances:
	 * 		(member - cluster)^2
	 * @param inst1: first instance to be used for calculation
	 * @param inst2: second instance to be used for calculation
	 * @return: returns the total squared error between the two instances
	 */
	public static long calculateSqErr(int[] inst1, int[] inst2){
		if(inst1.length != inst2.length){
			System.err.println("Two instances must be of the same size: " + inst1.length + " vs. " + inst2.length);
			System.exit(0);
		}
		
		long sqErr = 0;
		
		for(int i = 0; i < inst1.length-1; i++){
			sqErr += Math.pow(inst1[i] - inst2[i], 2);
		}
		
		return sqErr;
	}
	
	/**
	 * Calculates the current sum-squared error:
	 * 		sum of all sqErrs between members and their clusters
	 * @param test: if this method is being tested
	 * @return: returns the SSE value
	 */
	public static long calculateSSE(boolean test){
		long SSE = 0;
		
		for(int i = 0; i < clusters.length; i++){					//for each cluster
			if(test) System.out.println("SSE cluster: " + instToString(clusters[i]));
			for(int memb : clustMembs.get(i)){						//for each cluster member
				if(test) System.out.println("SSE inst: " + instToString(trainData.get(memb)));
				SSE += calculateSqErr(trainData.get(memb), clusters[i]);
			}
		}
		
		return SSE;
	}
	
	/**
	 * Calculates sum-squared separation:
	 * 		sum of sqErrs between all distinct pairs of clusters
	 * @param test: if this method is being tested
	 * @return: returns the sum-squared separation value
	 */
	public static long calculateSSS(boolean test){
		long SSS = 0;
		
		for(int i = 0; i < clusters.length; i++){
			for(int j = i+1; j < clusters.length; j++){
				SSS += calculateSqErr(clusters[i], clusters[j]);
				if(test) System.out.println(i + " & " + j + ": " + SSS);
			}
		}
		
		return SSS;
	}
	
	/**
	 * Calculates the entropy for a specified cluster
	 * 		probability * log2(probability)
	 * 		(probability = instances in classification/total instances in cluster)
	 * @param clust: cluster for which to have entropy calculated
	 * @param test: if this method is being tested
	 * @return
	 */
	public static double calculateEntropy(int clust, boolean test){
		double entropy = 0.0;
		
		HashMap<Integer, Integer> clsifCt = clustClass.get(clust);
		double ttlCt = 0;
		for(Map.Entry<Integer, Integer> cursor : clsifCt.entrySet()){
			ttlCt += cursor.getValue();
		}
		
		for(Map.Entry<Integer, Integer> cursor : clsifCt.entrySet()){
			double prob = cursor.getValue()/ttlCt;
			entropy += prob * (Math.log(prob)/Math.log(2));
			if(test) System.out.println("Class count: " + cursor.getValue() + "; total: " + ttlCt + "; prob: " + prob + "; entropy: " + entropy);
		}
		
		return entropy * -1;
	}
	
	//TODO
	public static double calculateMEntropy(){
		double mEntropy = 0.0;
		
		for(HashMap<Integer, Integer> clsifCt : clustClass){
			
		}
		
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
				if(updateClusters(false) < thresh) break;	//until cluster centers stop moving, update centers
			}
			double newSSE = calculateSSE(false);
			if(newSSE < SSE){
				SSE = newSSE;
				//maintain cluster set
			}
		}
		long SSS = calculateSSS(false);
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
		
		//extractData(trainFile, TRAIN);
		//bestIteration(k);
		//extractData(testFile, TEST);
		//classifyData();
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
		clustMembs.clear();
		clustClass.clear();
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
			System.err.println("Two cluster centers should not be equivalent! (clusters " + rand1 + " and " + rand2 + ")");
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
	
	
	/**
	 * Tests updateClusters() method
	 * @param verbose: print extra test info
	 * @return: returns true if all tests pass (includes manual/printing verification)
	 */
	public static boolean testUpdateClusters(boolean verbose){
		System.out.println("Testing cluster updates...");
		
		double var = updateClusters(verbose);
		System.out.println("Average variance: " + var);
		
		int ttlCt = 0;
		for(HashMap<Integer, Integer> clustCt : clustClass){
			for(Map.Entry<Integer, Integer> cursor : clustCt.entrySet()){
				ttlCt += cursor.getValue();
			}
		}
		if(ttlCt != trainData.size()){
			System.err.println("Instance mismatch between classification (" + ttlCt + ") and original training data (" + trainData.size() + ")");
			return false;
		}
		
		System.out.println("Cluster update tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests calculateSqErr() method
	 * @return: returns true if all tests pass
	 */
	public static boolean testCalculateSqErr(){
		System.out.println("Testing squared error calculation...");
		
		int[] inst1 = {0,0,0,2,14,1,0,0,0,0,0,10,12,0,0,0,0,0,8,15,1,2,1,0,0,3,15,5,0,12,7,0,0,10,14,0,6,16,2,0,0,8,16,16,16,12,0,0,0,0,2,4,16,5,0,0,0,0,0,2,13,0,0,0,4};
		int[] inst2 = {0,0,0,2,14,1,0,0,0,0,0,10,12,0,0,0,0,0,8,15,1,2,1,0,0,3,15,5,0,12,7,0,0,10,14,0,6,16,2,0,0,8,16,16,16,12,0,0,0,0,2,4,16,5,0,0,0,0,0,2,13,0,0,0,4};
		
		long sqErr = calculateSqErr(inst1, inst2);
		if(sqErr != 0){
			System.err.println("Squared error across two identical instances should be 0: " + sqErr);
			return false;
		}
		
		int[] inst3 = {0,0,1,12,16,14,2,0,0,0,13,11,3,16,5,0,0,4,14,0,0,15,6,0,0,6,12,8,13,16,5,0,0,0,9,12,4,10,8,0,0,0,3,0,0,11,5,0,0,0,16,14,5,15,4,0,0,0,3,12,16,11,1,0,9};
		sqErr = calculateSqErr(inst1, inst3);
		
		if(sqErr != 3387){
			System.err.println("Squared error should be 3387: " + sqErr);
			return false;
		}
		
		int[] inst4 = {0,0,3,9,14,9,0,0,0,5,16,14,5,0,0,0,0,12,11,3,0,0,0,0,0,13,16,12,1,0,0,0,0,4,11,13,8,0,0,0,0,0,0,7,11,0,0,0,0,0,1,12,12,0,0,0,0,0,2,15,7,0,0,0,5};
		sqErr = calculateSqErr(inst1, inst4);

		if(sqErr != 2478){
			System.err.println("Squared error should be 2478: " + sqErr);
			return false;
		}
		
		System.out.println("Squared error calculation tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests calculateSSE() method
	 * @param verbose: print extra test info
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testCalculateSSE(boolean verbose){
		System.out.println("Testing SSE calculation...");
		
		long SSE = calculateSSE(verbose);
		System.out.println("SSE: " + SSE);
		
		System.out.println("SSE calculation tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests calculateSSS() method
	 * @param verbose: if additional testing information should be printed
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testCalculateSSS(boolean verbose){
		System.out.println("Testing SSS calculation...");
		
		if(verbose) printClusters();
		
		long SSS = calculateSSS(verbose);
		System.out.println(SSS);
		
		System.out.println("SSS calculation tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests calculateEntropy() method
	 * @param verbose: if extra test info should be printed
	 * @return: always returns true (manual/printing validation)
	 */
	public static boolean testCalculateEntropy(boolean verbose){
		System.out.println("Testing entropy calculation...");
		
		double entropy = calculateEntropy(0, verbose);
		System.out.println("Entropy: " + entropy);
		
		System.out.println("Entropy calculation tests pass! :)\n");
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
		if(!testUpdateClusters(verbose)) return false;
		if(!testCalculateSqErr()) return false;
		if(!testCalculateSSE(false)) return false;
		if(!testCalculateSSS(verbose)) return false;
		if(!testCalculateEntropy(verbose)) return false;
		if(!testCalculateMEntropy()) return false;
		if(!testBestIteration(k)) return false;
		if(!testClassifyData()) return false;
		return true;
	}

}
