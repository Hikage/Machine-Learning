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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Kmeans {
	//Training data
	private static final double trainTtl = 3823.0;	
	private static ArrayList<int[]> trainData = new ArrayList<int[]>();
	private static int[][] clusters;
	private static ArrayList<ArrayList<Integer>> clustMembs = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<HashMap<Integer, Integer>> clustClass = new ArrayList<HashMap<Integer, Integer>>();
	
	//Test data
	private static int[] testCt = {178, 182, 177, 183, 181, 182, 181, 179, 174, 180};
	private static final double testTtl = 1797.0;
	private static ArrayList<int[]> testData = new ArrayList<int[]>();
	
	private static final boolean TRAIN = true;
	private static final boolean TEST = false;
	
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
			System.err.println("ERROR! Oh no! An error occurred!\n Error: " + ex);
			System.exit(0);
		}
	}

	/**
	 * Initializes preliminary cluster centers (randomly from training instances)
	 * @param k: number of clusters to initialize
	 */
	public static void initializeClusters(int k){
		if(trainData.isEmpty()){
			System.err.println("ERROR! Training data has not yet been extracted");
			System.exit(0);
		}
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
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		
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
	 * Clusters data
	 * @param test: if the method is being tested
	 */
	public static void clusterData(boolean test){
		if(trainData.isEmpty()){
			System.err.println("ERROR! Training data has not yet been extracted");
			System.exit(0);
		}
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		
		//initialize cluster ArrayLists
		clustMembs.clear();
		clustClass.clear();
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
			
			if(test) System.out.println(instToString(trainData.get(i)) + ": " + clustAsmt + "[" + clsif + "]" + clustClass.get(clustAsmt).get(clsif));
		}		
		if(test) System.out.println();
	}
	
	/**
	 * Updates each cluster center by averaging feature values of its members
	 * @param test: if this method is being tested
	 * @return: returns the average amount the clusters moved
	 */
	public static double updateClusters(boolean test){
		if(trainData.isEmpty()){
			System.err.println("ERROR! Training data has not yet been extracted");
			System.exit(0);
		}
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		
		double var = 0.0;
		
		clusterData(false);
		
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
		if(test) System.out.println();
		
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
			System.err.println("ERROR! Two instances must be of the same size: " + inst1.length + " vs. " + inst2.length);
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
		if(trainData.isEmpty()){
			System.err.println("ERROR! Training data has not yet been extracted");
			System.exit(0);
		}
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		if(clustMembs.isEmpty()){
			System.err.println("ERROR! Training data has not yet been classified");
			System.exit(0);
		}
		
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
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		
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
		if(clustClass.isEmpty()){
			System.err.println("ERROR! Cluster classifications have not yet been counted");
			System.exit(0);
		}
		
		double entropy = 0.0;
		
		HashMap<Integer, Integer> clsifCt = clustClass.get(clust);
		double ttlCt = 0;
		for(Map.Entry<Integer, Integer> cursor : clsifCt.entrySet()){
			ttlCt += cursor.getValue();
		}
		
		int domClass = 0;
		int domClassCt = 0;
		for(Map.Entry<Integer, Integer> cursor : clsifCt.entrySet()){
			//while we're iterating, figure out and save the dominant class for each cluster
			if(cursor.getValue() >= domClassCt){
				if(cursor.getValue() == domClassCt && new Random().nextBoolean());		//if we find two equivalent, randomly choose between doing nothing and updating
				else{
					domClassCt = cursor.getValue();
					domClass = cursor.getKey();
				}
			}
			
			double prob = cursor.getValue()/ttlCt;
			entropy += prob * (Math.log(prob)/Math.log(2));
			if(test) System.out.println("Class " + cursor.getKey() + " count: " + cursor.getValue() + "; total: " + ttlCt + "; prob: " + prob + "; entropy: " + entropy);
		}
		clusters[clust][clusters[clust].length-1] = domClass;
		if(test) System.out.println("Dominant class for cluster " + clust + ": " + domClass);
		
		return entropy * -1;
	}
	
	/**
	 * Calculates the mean entropy for all clusters
	 * 		sum of (instances in cluster/total instances) * cluster entropy
	 * @param test: if the method is being tested
	 * @return: returns the mean entropy value
	 */
	public static double calculateMEntropy(boolean test){
		if(trainData.isEmpty()){
			System.err.println("ERROR! Training data has not yet been extracted");
			System.exit(0);
		}
		if(clustMembs.isEmpty()){
			System.err.println("ERROR! Training data has not yet been classified");
			System.exit(0);
		}
		if(clustClass.isEmpty()){
			System.err.println("ERROR! Cluster classifications have not yet been counted");
			System.exit(0);
		}
		
		double mEntropy = 0.0;
		
		for(int i = 0; i < clustClass.size(); i++){
			int instInClust = clustMembs.get(i).size();
			double entropy = calculateEntropy(i, false);
			mEntropy += (instInClust/trainTtl * entropy);
			if(test) System.out.println("+" + instInClust + "/" + trainTtl + "*" + entropy + " = " + mEntropy);
		}
		
		return mEntropy;
	}
	
	/**
	 * Iterates with multiple cluster center seeds to find the best
	 * @param k: number of clusters
	 * @param test: if the method is being tested
	 */
	public static void bestIteration(int k, boolean test){
		int itCap = 10000;									//set an iteration cap to prevent infinite looping
		double thresh = 0.01;								//threshold for cluster movement convergence
		int numIterations = 5;								//number of iterations to run
		
		double SSE = Double.POSITIVE_INFINITY;
		int[][] bestClusts = new int[k][trainData.get(0).length];
		
		for(int i = 0; i < numIterations; i++){
			initializeClusters(k);
			for(int j = 0; j < itCap; j++){
				if(updateClusters(false) < thresh) break;	//until cluster centers stop moving, update centers
			}
			double newSSE = calculateSSE(false);
			if(newSSE < SSE){
				SSE = newSSE;
				if(test) System.out.println("New SSE: " + SSE);
				bestClusts = clusters.clone();
			}
		}
		clusters = bestClusts.clone();						//keep the best iteration's cluster set
		clusterData(false);									//re-cluster data for final calculations
		long SSS = calculateSSS(false);
		double mEntropy = calculateMEntropy(false);
		
		if(test) printClusters();
		System.out.println("SSE: " + SSE + "; SSS: " + SSS + "; Mean Entropy: " + mEntropy);
	}
	
	/**
	 * Classifies each test instance, building the confusion matrix along the way
	 * @param testFile: test data to be extracted
	 * @return: returns the completed confusion matrix
	 */
	public static int[][] classifyData(String testFile){
		extractData(testFile, TEST);
		
		int[][] conMtrx = new int[testCt.length][testCt.length];
		
		//iterate through each instance of the data and classify
		for(int[] inst : testData){
			int clust = assignCluster(inst);
			int clsif = clusters[clust][clusters[clust].length-1];
			int trueclsif = inst[inst.length-1];
			
			if(clsif < 0){
				System.err.println("ERROR! Error finding maximum probability for classification");
				System.exit(0);
			}
			
			conMtrx[trueclsif][clsif]++;			//build confusion matrix
		}
		
		return conMtrx;
	}
	
	/**
	 * Calculates overall accuracy of test data
	 * @param conMtrx: confusion matrix used to calculate accuracy
	 * @return: returns overall accuracy of test data
	 */
	public static double calcAcc(int[][] conMtrx){
		if(conMtrx == null){
			System.err.println("ERROR! Confusion matrix has not yet been constructed");
			System.exit(0);
		}
		
		int correct = 0;
		for(int i = 0; i < conMtrx.length; i++){
			correct += conMtrx[i][i];
		}
		
		return correct/testTtl;
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
		
		//if(!runTests(trainFile, testFile, k, true)) System.exit(0);
		
		extractData(trainFile, TRAIN);
		System.out.println("Training clusters...");
		bestIteration(k, false);
		System.out.println("\nClassifying data...");
		int[][] conMtrx = classifyData(testFile);
		printConMtrx(conMtrx);
		System.out.println();
		clusterVisualization();
	}
	
	/**** Printing Methods ****/
	
	/**
	 * Converts specified instance to string representation
	 * @param inst: instance to be converted
	 * @return: returns string representation of the indicated instance
	 */
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
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		
		System.out.println("Clusters:");
		for(int[] cluster : clusters){
			System.out.println(instToString(cluster));
		}
	}
	
	/**
	 * Prints cluster visualization
	 */
	public static void clusterVisualization(){
		if(clusters == null){
			System.err.println("ERROR! Clusters have not yet been initiated");
			System.exit(0);
		}
		
		for(int[] cluster : clusters){
			System.out.println("\n" + cluster[cluster.length-1]);
			int size = (int)Math.sqrt(cluster.length);
			for(int i = 0; i < cluster.length-1; i++){
				if((i+1) % size == 0) System.out.println();
				else System.out.print(cluster[i] + "\t");
			}
			System.out.println();
		}
	}
	
	/**
	 * Converts the confusion matrix into a string representation
	 * @param digitConMtrx: confusion matrix for a specific digit, to be converted
	 * @return: returns the string representation of the indicated confusion matrix
	 */
	public static String digitConMtrxToString(int[] digitConMtrx){
		String mtrxStr = "";
		for(int i = 0; i < digitConMtrx.length; i++){
			mtrxStr += (digitConMtrx[i] + "\t");
		}
		return mtrxStr;
	}
	
	/**
	 * Prints the confusion matrix for all digits and overall accuracy
	 * @param conMtrx: confusion matrix to be printed
	 */
	public static void printConMtrx(int[][] conMtrx){
		System.out.println((int)testTtl + " total instances");
		System.out.println("Accuracy: " + pf.format(calcAcc(conMtrx)));
		for(int i = 0; i < conMtrx.length; i++){
			System.out.print("\t" + i);
		}
		System.out.println();
		for(int i = 0; i < conMtrx.length; i++){
			System.out.println(i + "\t" + digitConMtrxToString(conMtrx[i]));
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
			System.err.println("ERROR! Wrong number of training instances read in: " + trainData.size());
			return false;
		}
		for(int i = 0; i < trainData.size(); i++){
			if(trainData.get(i)[0] != 0){
				System.err.println("ERROR! First value for training instance " + i + "should be 0: " + trainData.get(i)[0]);
				return false;
			}
		}
		if(trainData.get(27)[33] != 2){		//random instance
			System.err.println("ERROR! Value for training instance 27 feature 33 should be 2: " + trainData.get(27)[33]);
			return false;
		}
		
		//test test data
		extractData(testFile, TEST);
		if(testData.size() != 1797){
			System.err.println("ERROR! Wrong number of test instances read in: " + testData.size());
			return false;
		}
		for(int i = 0; i < testData.size(); i++){
			if(testData.get(i)[0] != 0){
				System.err.println("ERROR! First value for test instance " + i + "should be 0: " + testData.get(i)[0]);
				return false;
			}
		}
		if(testData.get(1385)[53] != 16){		//random instance
			System.err.println("ERROR! Value for test instance 1385 feature 53 should be 16: " + testData.get(1385)[53]);
			return false;
		}
		
		clearData();
		System.out.println("Data extraction tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests initializeClusters() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @return: returns true if all tests pass
	 */
	public static boolean testInitializeClusters(String trainFile, int k){
		System.out.println("Testing cluster initialization...");
		
		extractData(trainFile, TRAIN);
		
		initializeClusters(k);
		//test initialization size
		if(clusters.length != k){
			System.err.println("ERROR! Clusters initialized to the wrong size: " + k + " (k) " + clusters.length + " (# clusters)");
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
			System.err.println("ERROR! Two cluster centers should not be equivalent! (clusters " + rand1 + " and " + rand2 + ")");
			return false;
		}

		clearData();
		System.out.println("Cluster initialization tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests assignCluster() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: returns true if all tests pass
	 */
	public static boolean testAssignCluster(String trainFile, int k, boolean verbose){
		System.out.println("Testing cluster assignment...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
		
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
		
		clearData();
		System.out.println("Cluster assignment tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests clusterData() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: returns true if all tests pass
	 */
	public static boolean testClusterData(String trainFile, int k, boolean verbose){
		System.out.println("Testing clustering data...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
		
		clusterData(verbose);
		
		int ttlCt = 0;
		for(HashMap<Integer, Integer> clustCt : clustClass){
			for(Map.Entry<Integer, Integer> cursor : clustCt.entrySet()){
				ttlCt += cursor.getValue();
			}
		}
		if(ttlCt != trainData.size()){
			System.err.println("ERROR! Instance mismatch between classification (" + ttlCt + ") and original training data (" + trainData.size() + ")");
			return false;
		}
		
		clearData();
		System.out.println("Clustering data tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests updateClusters() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testUpdateClusters(String trainFile, int k, boolean verbose){
		System.out.println("Testing cluster updates...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
				
		double var = updateClusters(verbose);
		System.out.println("Average variance: " + var);
		
		clearData();
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
			System.err.println("ERROR! Squared error across two identical instances should be 0: " + sqErr);
			return false;
		}
		
		int[] inst3 = {0,0,1,12,16,14,2,0,0,0,13,11,3,16,5,0,0,4,14,0,0,15,6,0,0,6,12,8,13,16,5,0,0,0,9,12,4,10,8,0,0,0,3,0,0,11,5,0,0,0,16,14,5,15,4,0,0,0,3,12,16,11,1,0,9};
		sqErr = calculateSqErr(inst1, inst3);
		
		if(sqErr != 3387){
			System.err.println("ERROR! Squared error should be 3387: " + sqErr);
			return false;
		}
		
		int[] inst4 = {0,0,3,9,14,9,0,0,0,5,16,14,5,0,0,0,0,12,11,3,0,0,0,0,0,13,16,12,1,0,0,0,0,4,11,13,8,0,0,0,0,0,0,7,11,0,0,0,0,0,1,12,12,0,0,0,0,0,2,15,7,0,0,0,5};
		sqErr = calculateSqErr(inst1, inst4);

		if(sqErr != 2478){
			System.err.println("ERROR! Squared error should be 2478: " + sqErr);
			return false;
		}
		
		System.out.println("Squared error calculation tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests calculateSSE() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testCalculateSSE(String trainFile, int k, boolean verbose){
		System.out.println("Testing SSE calculation...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
		updateClusters(false);
		
		long SSE = calculateSSE(verbose);
		System.out.println("SSE: " + SSE);

		clearData();
		System.out.println("SSE calculation tests pass! :)\n");
		return true;
	}
	
	
	/**
	 * Tests calculateSSS() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testCalculateSSS(String trainFile, int k, boolean verbose){
		System.out.println("Testing SSS calculation...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
		
		if(verbose) printClusters();
		
		long SSS = calculateSSS(verbose);
		System.out.println(SSS);

		clearData();
		System.out.println("SSS calculation tests pass! :)\n");
		return true;
	}
	

	/**
	 * Tests calculateEntropy() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: always returns true (manual/printing validation)
	 */
	public static boolean testCalculateEntropy(String trainFile, int k, boolean verbose){
		System.out.println("Testing entropy calculation...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
		updateClusters(false);
		
		double entropy = calculateEntropy(0, verbose);
		System.out.println("Entropy: " + entropy);

		clearData();
		System.out.println("Entropy calculation tests pass! :)\n");
		return true;
	}
		

	/**
	 * Tests calculateMEntropy() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testCalculateMEntropy(String trainFile, int k, boolean verbose){
		System.out.println("Testing mean entropy calculation...");
		
		extractData(trainFile, TRAIN);
		initializeClusters(k);
		updateClusters(false);
		
		double mEntropy = calculateMEntropy(verbose);
		System.out.println("Mean entropy: " + mEntropy);

		clearData();
		System.out.println("Mean entropy calculation tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests bestIteration() method
	 * @param trainFile: training data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if additional test information should be printed
	 * @return: always returns true (manual/printing verification)
	 */
	public static boolean testBestIteration(String trainFile, int k, boolean verbose){
		System.out.println("Testing best iteration determination...");
		
		extractData(trainFile, TRAIN);
		bestIteration(k, verbose);

		clearData();
		System.out.println("Best iteration determination tests pass! :)\n");
		return true;
	}
	
	/**
	 * Tests classifyData() method
	 * @param trainFile: training data to be extracted
	 * @param testFile: test data to be extracted
	 * @param k: number of clusters
	 * @param verbose: if extra test information should be printed
	 * @return: returns true if all tests pass
	 */
	public static boolean testClassifyData(String trainFile, String testFile, int k, boolean verbose){
		System.out.println("Testing data classification...");

		extractData(trainFile, TRAIN);
		bestIteration(k, false);
		
		int[][] conMtrx = classifyData(testFile);
		if(verbose){
			printClusters();
			printConMtrx(conMtrx);
		}
		
		for(int i = 0; i < conMtrx.length; i++){
			if(conMtrx[i][i] == testCt[i]){
				System.err.println("ERROR! Perfect accuracy very unlikely for " + i + ": " + conMtrx[i][i]);
				return false;
			}
			int isum = 0;
			for(int j = 0; j < conMtrx[i].length; j++) isum += conMtrx[i][j];
			if(isum != testCt[i]){
				System.err.println("ERROR! Confusion matrix for digit " + i + " should sum to total instance count (" + testCt[i] + "): " +
						digitConMtrxToString(conMtrx[i]));
				return false;
			}
		}		
		
		clearData();
		System.out.println("Data classification tests pass! :)\n");
		return true;
	}
	
	/**
	 * Driver for unit tests
	 * @param trainFile: training file to be extracted
	 * @param testFile: test file to be extracted
	 * @param k: number of clusters
	 * @param verbose: if tests should print additional information
	 * @return: returns true if all tests pass
	 */
	public static boolean runTests(String trainFile, String testFile, int k, boolean verbose){
		clearData();
		if(!testExtractData(trainFile, testFile)) return false;
		if(!testInitializeClusters(trainFile, k)) return false;
		if(!testAssignCluster(trainFile, k, verbose)) return false;
		if(!testClusterData(trainFile, k, false)) return false;
		if(!testUpdateClusters(trainFile, k, verbose)) return false;
		if(!testCalculateSqErr()) return false;
		if(!testCalculateSSE(trainFile, k, false)) return false;
		if(!testCalculateSSS(trainFile, k, verbose)) return false;
		if(!testCalculateEntropy(trainFile, k, verbose)) return false;
		if(!testCalculateMEntropy(trainFile, k, verbose)) return false;
		if(!testBestIteration(trainFile, k, verbose)) return false;
		if(!testClassifyData(trainFile, testFile, k, verbose)) return false;
		return true;
	}

}
