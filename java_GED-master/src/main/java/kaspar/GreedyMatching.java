package kaspar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class GreedyMatching {



	// only used for some particular evaluations
	private double assignmentCost = 0;

	// getter for the "pure" assignment costs
	public double getAssignmentCost() {
		return assignmentCost;
	}




	/**
	 * VERSION 1a
	 * pure greedy assignment
	 */
	public int[][] getMatching(double[][] cm) {
		this.assignmentCost = 0;
		int[][] assignment = new int[cm.length][2];
		int[] usedIndices = new int[cm.length];
		ArrayList<Integer> unusedIndices = new ArrayList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndices.add(k);
		}
		for (int i = 0; i < cm.length; i++){
			int iIndex = unusedIndices.get(i);
			int greedyMatch = -1;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < cm.length; j++){
				if (usedIndices[j] == 0){
					if (cm[iIndex][j] <= min){
						min = cm[iIndex][j];
						greedyMatch = j;
					}
				}
			}
			assignment[iIndex][0] = iIndex;
			assignment[iIndex][1] = greedyMatch;
			usedIndices[greedyMatch] = 1;
			this.assignmentCost += min;
		}
		return assignment;
	}

	/**
	 * VERSION 1b
	 * pure greedy assignment that seeks for maximum
	 * rather than minimum
	 */
	public int[][] getMaxMatching(double[][] cm, int s, int t) {
		int[][] assignment = new int[cm.length][2];
		int[] usedIndices = new int[cm.length];
		LinkedList<Integer> unusedIndices = new LinkedList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndices.add(k);
		}
		if (s > 0 && t > 0){
			unusedIndices = this.reorderIndices(cm, "RowMax", s, t);
		}
		for (int i = 0; i < cm.length; i++){
			int iIndex = unusedIndices.get(i);
			int greedyMatch = -1;
			double max = Double.NEGATIVE_INFINITY;
			for (int j = 0; j < cm.length; j++){
				if (usedIndices[j] == 0){
					if (cm[iIndex][j] >= max){
						max = cm[iIndex][j];
						greedyMatch = j;
					}
				}
			}
			assignment[iIndex][0] = iIndex;
			assignment[iIndex][1] = greedyMatch;
			usedIndices[greedyMatch] = 1;
		}
		return assignment;
	}
	

	/**
	 * VERSION 1c 
	 * greedy with tie resolution
	 */
	public int[][] getMatchingTieResolution(double[][] cm) {
		this.assignmentCost = 0;

		int[][] assignment = new int[cm.length][2];
		int[] usedIndices = new int[cm.length];
		ArrayList<Integer> unusedIndices = new ArrayList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndices.add(k);
		}
		LinkedList<Integer> tieIndices = new LinkedList<Integer>();
		for (int i = 0; i < cm.length; i++){
			tieIndices.clear();
			int iIndex = unusedIndices.get(i);
			int greedyMatch = -1;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < cm.length; j++){
				if (usedIndices[j] == 0){
					if (cm[iIndex][j] < min){
						tieIndices.clear();
						min = cm[iIndex][j];
						greedyMatch = j;
						tieIndices.add(greedyMatch);
					}
					if (cm[iIndex][j] == min){
						tieIndices.add(j);
					}
				}
			}
			if (i < cm.length-1){
				if (tieIndices.size() > 1){
					double maxMin = -1;
					greedyMatch = -1;
					for (int tieIndex : tieIndices){
						min = Double.MAX_VALUE;
						for (int k = i+1; k < cm.length; k++){
							if (cm[k][tieIndex] < min){
								min = cm[k][tieIndex];
							}
						}
						if (min > maxMin){
							maxMin = min;
							greedyMatch = tieIndex;
						}
					}
				} 
			}
			assignment[iIndex][0] = iIndex;
			assignment[iIndex][1] = greedyMatch;
			usedIndices[greedyMatch] = 1;
			this.assignmentCost += cm[iIndex][greedyMatch];
		}
		return assignment;
	}
	
	
	

	/**
	 * VERSION 2a
	 * greedy with refinement procedure: basically
	 * we seek for the minimum in both thw row and the column
	 * the minium of both is evetually selected
	 */
	public int[][] getRefinedMatching(double[][] cm) {
		int[][] assignment = new int[cm.length][2];
		int[] usedIndicesOfG2 = new int[cm.length];
		int[] usedIndicesOfG1 = new int[cm.length];

		LinkedList<Integer> unusedIndicesOfG1 = new LinkedList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndicesOfG1.add(k);
		}
		while (!unusedIndicesOfG1.isEmpty()){
			int iIndex = unusedIndicesOfG1.removeFirst();
			usedIndicesOfG1[iIndex]=1;
			int greedyMatch = -1;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < cm.length; j++){
				if (usedIndicesOfG2[j] == 0){
					if (cm[iIndex][j] <= min){
						min = cm[iIndex][j];
						greedyMatch = j;
					}
				}
			}
			// iIndex-->greedyMatch is minimal is greedyMatch-->iIndex also?
			int greedyReverseMatch = -1;
			min = Double.MAX_VALUE;
			for (int i = 0; i < cm.length; i++){
				if (usedIndicesOfG1[i] == 0){
					if (cm[i][greedyMatch] < min){
						min = cm[i][greedyMatch];
						greedyReverseMatch = i;
					}
				}
			}
			if (min < cm[iIndex][greedyMatch]){ // there is a better matching available
				usedIndicesOfG1[iIndex]=0;
				assignment[greedyReverseMatch][0] = greedyReverseMatch;
				assignment[greedyReverseMatch][1] = greedyMatch;
				usedIndicesOfG1[greedyReverseMatch]=1;
				usedIndicesOfG2[greedyMatch] = 1;
				unusedIndicesOfG1.remove(new Integer(greedyReverseMatch));
				unusedIndicesOfG1.add(iIndex); 
			} else {
				assignment[iIndex][0] = iIndex;
				assignment[iIndex][1] = greedyMatch;
				usedIndicesOfG2[greedyMatch] = 1;
			}	
		}
		return assignment;
	}



	/**
	 * VERSION 2b
	 * greedy with refinement procedure (see 2a) 
	 * including tie resolution
	 */
	public int[][] getRefinedMatchingTie(double[][] cm) {
		this.assignmentCost = 0;

		int[][] assignment = new int[cm.length][2];
		int[] usedIndicesOfG2 = new int[cm.length];
		int[] usedIndicesOfG1 = new int[cm.length];

		LinkedList<Integer> unusedIndicesOfG1 = new LinkedList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndicesOfG1.add(k);
		}
		LinkedList<Integer> tieIndices = new LinkedList<Integer>();
		while (!unusedIndicesOfG1.isEmpty()){
			tieIndices.clear();
			int iIndex = unusedIndicesOfG1.removeFirst();
			usedIndicesOfG1[iIndex]=1;
			int greedyMatch = -1;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < cm.length; j++){
				if (usedIndicesOfG2[j] == 0){
					if (cm[iIndex][j] < min){
						tieIndices.clear();
						min = cm[iIndex][j];
						greedyMatch = j;
						tieIndices.add(greedyMatch);
					}
					if (cm[iIndex][j] == min){
						tieIndices.add(j);
					}
				}
			}
			// tie resolution
			if (unusedIndicesOfG1.size()>0){
				if (tieIndices.size() > 1){
					double maxMin = -1;
					greedyMatch = -1;
					for (int tieIndex : tieIndices){
						min = Double.MAX_VALUE;
						for (int k : unusedIndicesOfG1){
							if (cm[k][tieIndex] < min){
								min = cm[k][tieIndex];
							}
						}
						if (min > maxMin){
							maxMin = min;
							greedyMatch = tieIndex;
						}

					}
				}
			}

			// iIndex-->greedyMatch is minimal is greedyMatch-->iIndex also?
			int greedyReverseMatch = -1;
			min = Double.MAX_VALUE;
			for (int i = 0; i < cm.length; i++){
				if (usedIndicesOfG1[i] == 0){
					if (cm[i][greedyMatch] < min){
						min = cm[i][greedyMatch];
						greedyReverseMatch = i;
					}
				}
			}
			if (min < cm[iIndex][greedyMatch]){ // there is a better matching available
				usedIndicesOfG1[iIndex]=0;
				assignment[greedyReverseMatch][0] = greedyReverseMatch;
				assignment[greedyReverseMatch][1] = greedyMatch;
				this.assignmentCost += cm[greedyReverseMatch][greedyMatch];
				usedIndicesOfG1[greedyReverseMatch]=1;
				usedIndicesOfG2[greedyMatch] = 1;
				unusedIndicesOfG1.remove(new Integer(greedyReverseMatch));
				unusedIndicesOfG1.add(iIndex); 
			} else {
				assignment[iIndex][0] = iIndex;
				assignment[iIndex][1] = greedyMatch;
				usedIndicesOfG2[greedyMatch] = 1;
				this.assignmentCost += cm[iIndex][greedyMatch];
			}	
		}
		return assignment;
	}





	/**
	 * VERSION 3 
	 * pure greedy assignment with random row order
	 * the complete assignment process is repeated *iter* times
	 * and the best assignment is returned
	 */

	public int[][] getRandomizedMatching(double[][] cm, int iter) {
		this.assignmentCost = 0;

		double bestAssignmentCost = Double.MAX_VALUE;
		int[][] bestAssignment = new int[cm.length][2];

		for (int t = 0; t < iter; t++){
			double assignmentCost = 0;
			int[][] assignment = new int[cm.length][2];
			int[] usedIndices = new int[cm.length];
			ArrayList<Integer> unusedIndices = new ArrayList<Integer>(); 
			for (int k = 0; k < cm.length; k++){
				unusedIndices.add(k);
			}
			Collections.shuffle(unusedIndices);
			for (int i = 0; i < cm.length; i++){
				int iIndex = unusedIndices.get(i);
				int greedyMatch = -1;
				double min = Double.MAX_VALUE;
				for (int j = 0; j < cm.length; j++){
					if (usedIndices[j] == 0){
						if (cm[iIndex][j] <= min){
							min = cm[iIndex][j];
							greedyMatch = j;
						}
					}
				}
				assignment[iIndex][0] = iIndex;
				assignment[iIndex][1] = greedyMatch;
				usedIndices[greedyMatch] = 1;
				assignmentCost += cm[iIndex][greedyMatch];
			}
			if (assignmentCost < bestAssignmentCost){
				bestAssignmentCost = assignmentCost;
				bestAssignment = assignment;
			}
		}
		return bestAssignment;
	}

	
	/**
	 * Version 4
	 * greedy assignment with specific loss computation
	 */
	public int[][] getMatchingUsingLoss(double[][] cm) {
		this.assignmentCost = 0;
		int[][] assignment = new int[cm.length][2];
		int[] usedIndicesOfG2 = new int[cm.length];
		int[] usedIndicesOfG1 = new int[cm.length];

		LinkedList<Integer> unusedIndicesOfG1 = new LinkedList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndicesOfG1.add(k);
		}
		while (!unusedIndicesOfG1.isEmpty()){
			int i = unusedIndicesOfG1.removeFirst();
			usedIndicesOfG1[i]=1;
			int phi_i_1 = -1, phi_i_2 = -1;
			double c_iphi_i_1 = Double.MAX_VALUE;
			double c_iphi_i_2 = Double.MAX_VALUE;
			for (int j = 0; j < cm.length; j++){
				if (usedIndicesOfG2[j] == 0){
					if (cm[i][j] <= c_iphi_i_1){ // better found
						// LADIES AND GENTLEMAN:
						// Now follows the well-known 
						// ****complicated**** 
						// tie management!!!
						// needs massive refactoring (seriously!!!)
						if (cm[i][j] == c_iphi_i_1){
							// assign i better to j or phi_i_1?
							double min1 = Double.MAX_VALUE, min2 = Double.MAX_VALUE;
							for (int k = 0; k < cm.length; k++){
								if (usedIndicesOfG1[k]==0){
									if (cm[k][phi_i_1] < min1){
										min1 = cm[k][phi_i_1];
									}
									if (cm[k][j] < min2){
										min2 = cm[k][j];
									}
								}
							}
							if (min1 < min2){ // we change
								if (c_iphi_i_1 <= c_iphi_i_2){ // is previously best better than second best
									if (c_iphi_i_1 == c_iphi_i_2){
										min1 = Double.MAX_VALUE; 
										min2 = Double.MAX_VALUE;
										for (int k = 0; k < cm.length; k++){
											if (usedIndicesOfG1[k]==0){
												if (cm[k][phi_i_1] < min1){
													min1 = cm[k][phi_i_1];
												}
												if (cm[k][phi_i_2] < min2){
													min2 = cm[k][phi_i_2];
												}
											}
										}
										if (min1 > min2){ 
											c_iphi_i_2 = c_iphi_i_1;
											phi_i_2 = phi_i_1;
										}
									} else {
										c_iphi_i_2 = c_iphi_i_1;
										phi_i_2 = phi_i_1;
									}
								}
								phi_i_1 = j;
								c_iphi_i_1 = cm[i][j];
							} else { // we do not change
								if (cm[i][j] <= c_iphi_i_2){// is current better than second best
									if (cm[i][j] == c_iphi_i_2){
										min1 = Double.MAX_VALUE; 
										min2 = Double.MAX_VALUE;
										for (int k = 0; k < cm.length; k++){
											if (usedIndicesOfG1[k]==0){
												if (cm[k][j] < min1){
													min1 = cm[k][j];
												}
												if (cm[k][phi_i_2] < min2){
													min2 = cm[k][phi_i_2];
												}
											}
										}
										if (min1 > min2){ 
											c_iphi_i_2 = cm[i][j];
											phi_i_2 = j;
										}
									} else {
										c_iphi_i_2 = cm[i][j];
										phi_i_2 = j;
									}
								}
							}
						} else { // we change anyway
							if (c_iphi_i_1 <= c_iphi_i_2){ // is previously best better than second best
								if (c_iphi_i_1 == c_iphi_i_2 && phi_i_1>-1){
									double min1 = Double.MAX_VALUE; 
									double min2 = Double.MAX_VALUE;
									for (int k = 0; k < cm.length; k++){
										if (usedIndicesOfG1[k]==0){
											if (cm[k][phi_i_1] < min1){
												min1 = cm[k][phi_i_1];
											}
											if (cm[k][phi_i_2] < min2){
												min2 = cm[k][phi_i_2];
											}
										}
									}
									if (min1 > min2){ 
										c_iphi_i_2 = c_iphi_i_1;
										phi_i_2 = phi_i_1;
									}
								} else {
									c_iphi_i_2 = c_iphi_i_1;
									phi_i_2 = phi_i_1;
								}
							}
							phi_i_1 = j;
							c_iphi_i_1 = cm[i][j];
						}
					} else { // it is not better than best, but at least better than second best?
						if (cm[i][j] <= c_iphi_i_2){// is current better than second best
							if (cm[i][j] == c_iphi_i_2){
								double min1 = Double.MAX_VALUE; 
								double min2 = Double.MAX_VALUE;
								for (int k = 0; k < cm.length; k++){
									if (usedIndicesOfG1[k]==0){
										if (cm[k][j] < min1){
											min1 = cm[k][j];
										}
										if (cm[k][phi_i_2] < min2){
											min2 = cm[k][phi_i_2];
										}
									}
								}
								if (min1 > min2){ 
									c_iphi_i_2 = cm[i][j];
									phi_i_2 = j;
								}
							} else {
								c_iphi_i_2 = cm[i][j];
								phi_i_2 = j;
							}
						}
					}
				}
			}

			if (phi_i_1 == phi_i_2){
				System.err.println("FATAL ERROR "+i+" "+phi_i_1+" "+phi_i_2);
				System.exit(0);
			}

			// i-->phi_i_1 is minimal is phi_i_1-->i also?
			int k = -1;
			double c_kphi_i = Double.MAX_VALUE;
			for (int q = 0; q < cm.length; q++){
				if (usedIndicesOfG1[q] == 0){
					if (cm[q][phi_i_1] < c_kphi_i){
						c_kphi_i = cm[q][phi_i_1];
						k = q;
					}
				}
			}

			if (c_kphi_i < c_iphi_i_1){ // there is possibly a better matching available -> compute better of both 
				// compute "second best" option for index k;
				double c_kphi_k = Double.MAX_VALUE;
				int phi_k = -1;
				for (int j = 0; j < cm.length; j++){
					if (usedIndicesOfG2[j] == 0 && j != phi_i_1){
						if (cm[k][j] <= c_kphi_k){
							// *complicated* tie management
							if (cm[k][j] == c_kphi_k){
								// assign k better to j or phi_k?
								double min1 = Double.MAX_VALUE,min2 = Double.MAX_VALUE;
								for (int p = 0; p < cm.length; p++){
									if (usedIndicesOfG1[p]==0){
										if (cm[p][phi_k] < min1){
											min1 = cm[p][phi_k];
										}
										if (cm[p][j] < min2){
											min2 = cm[p][j];
										}
									}
								}
								if (min1 < min2){ // we change
									c_kphi_k = cm[k][j];
									phi_k = j;
								}
							} else {
								c_kphi_k = cm[k][j];
								phi_k = j;
							}						
						}
					}
				}

				double sum1 = cm[i][phi_i_1] + cm[k][phi_k];
				double sum2 = cm[k][phi_i_1] + cm[i][phi_i_2];

				//				this.printCurrentSituation(i, phi_i_1, phi_k, phi_i_2, k, cm);

				if (sum2 < sum1){
					assignment[k][0] = k;
					assignment[k][1] = phi_i_1;
					usedIndicesOfG1[k]=1;
					usedIndicesOfG2[phi_i_1] = 1;
					unusedIndicesOfG1.remove(new Integer(k));
					assignment[i][0] = i;
					assignment[i][1] = phi_i_2;
					usedIndicesOfG2[phi_i_2] = 1;
					this.assignmentCost+=cm[k][phi_i_1];
					this.assignmentCost+=cm[i][phi_i_2];


				} else {
					assignment[i][0] = i;
					assignment[i][1] = phi_i_1;
					usedIndicesOfG2[phi_i_1] = 1;
					assignment[k][0] = k;
					assignment[k][1] = phi_k;
					usedIndicesOfG1[k]=1;
					usedIndicesOfG2[phi_k] = 1;
					unusedIndicesOfG1.remove(new Integer(k));
					this.assignmentCost+=cm[i][phi_i_1];
					this.assignmentCost+=cm[k][phi_k];
				}

			} else {
				assignment[i][0] = i;
				assignment[i][1] = phi_i_1;
				usedIndicesOfG2[phi_i_1] = 1;
				this.assignmentCost+=cm[i][phi_i_1];
			}	
		}
		return assignment;
	}








	/**
	 * Version 5
	 * greedy algorithms that works on a list rather than a 
	 * matrix
	 */
	public int[][] greedySort(double[][] cm, int sSize, int tSize) {
		this.assignmentCost = 0;
		int[][] assignment = new int[sSize][2];
		int[] usedIndicesOfG2 = new int[tSize];
		int[] usedIndicesOfG1 = new int[sSize];
		LinkedList<Integer> unusedIndicesOfG1 = new LinkedList<Integer>(); 
		LinkedList<Integer> unusedIndicesOfG2 = new LinkedList<Integer>(); 
		for (int k = 0; k < sSize; k++){
			unusedIndicesOfG1.add(k);
		}
		for (int k = 0; k < tSize; k++){
			unusedIndicesOfG2.add(k);
		}
		LinkedList<AssignmentAndCost> substitutions = new LinkedList<AssignmentAndCost>();
		for (int i = 0; i < sSize; i++){
			for (int j = 0; j < tSize; j++){
				AssignmentAndCost aAndc = new AssignmentAndCost(i,j,cm[i][j]);
				substitutions.add(aAndc);
			}
		}
		Collections.sort(substitutions);
		Iterator<AssignmentAndCost> iter = substitutions.iterator();
		while (!unusedIndicesOfG2.isEmpty()){
			AssignmentAndCost aAndc = iter.next();
			int from = aAndc.getFrom();
			if (usedIndicesOfG1[from]==0){
				int to = aAndc.getTo();
				if (usedIndicesOfG2[to]==0){
					usedIndicesOfG1[from]=1;
					usedIndicesOfG2[to]=1;
					assignment[from][0] = from;
					assignment[from][1] = to;
					this.assignmentCost += cm[from][to];
					unusedIndicesOfG1.remove(new Integer(from));
					unusedIndicesOfG2.remove(new Integer(to));
				}
			}
		}
		while (!unusedIndicesOfG1.isEmpty()){
			int from = unusedIndicesOfG1.removeFirst();
			assignment[from][0] = from;
			assignment[from][1] = tSize+1;
			if (tSize>0){
				this.assignmentCost += cm[from][tSize+1];
			} else {
				this.assignmentCost += cm[from][tSize];
			}
		}
		return assignment;
	}
	
	
	

	


	/**
	 * Version 6
	 * greedy algorithm that works with specifically
	 * ordered rows
	 */
	public int[][] getMatchingWithReordering(double[][] cm, String reorderCrit, int s, int t) {
		this.assignmentCost = 0;
		int[][] assignment = new int[cm.length][2];
		int[] usedIndices = new int[cm.length];
		LinkedList<Integer> unusedIndices = new LinkedList<Integer>(); 
		for (int k = 0; k < cm.length; k++){
			unusedIndices.add(k);
		}

		if (s > 0 && t > 0){
			unusedIndices = this.reorderIndices(cm, reorderCrit, s, t);
		}

		for (int i = 0; i < cm.length; i++){
			int iIndex = unusedIndices.get(i);
			int greedyMatch = -1;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < cm.length; j++){
				if (usedIndices[j] == 0){
					if (cm[iIndex][j] <= min){
						min = cm[iIndex][j];
						greedyMatch = j;
					}
				}
			}
			assignment[iIndex][0] = iIndex;
			assignment[iIndex][1] = greedyMatch;
			usedIndices[greedyMatch] = 1;
			this.assignmentCost += min;
		}
		return assignment;
	}




	/**
	 * Reordering algorithm
	 */
	private LinkedList<Integer> reorderIndices(double[][] cm, String reorderCrit, int s, int t) {
		LinkedList<Integer> reorderedIndices = new LinkedList<Integer>();
		ArrayList<IndexAndCost> indices = new ArrayList<IndexAndCost>();

		if (reorderCrit.equals("RowMin")) {
			indices = new ArrayList<IndexAndCost>();
			for (int i = 0; i < s; i++) {
				double rowMin = Double.MAX_VALUE;
				for (int j = 0; j < t; j++) {
					if (cm[i][j] < rowMin) {
						rowMin = cm[i][j];
					}
				}
				IndexAndCost iac = new IndexAndCost(i, rowMin);
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = 0; i < indices.size(); i++) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}
			return reorderedIndices;

		} else if (reorderCrit.equals("RowMax")) {
			indices = new ArrayList<IndexAndCost>();
			for (int i = 0; i < s; i++) {
				double rowMax = Double.MIN_VALUE;
				for (int j = 0; j < t; j++) {
					if (cm[i][j] > rowMax) {
						rowMax = cm[i][j];
					}
				}
				IndexAndCost iac = new IndexAndCost(i, rowMax);
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = indices.size()-1; i >= 0; i--) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}
			return reorderedIndices;

		// the greater interval is, the easier is the matching...	
		} else if (reorderCrit.equals("Interval")) {
			indices = new ArrayList<IndexAndCost>();
			for (int i = 0; i < s; i++) {
				double rowMax = Double.MIN_VALUE;
				double rowMin = Double.MAX_VALUE;
				for (int j = 0; j < t; j++) {
					if (cm[i][j] > rowMax) {
						rowMax = cm[i][j];
					}
					if (cm[i][j] < rowMin) {
						rowMin = cm[i][j];
					}
				}
				IndexAndCost iac = new IndexAndCost(i, -(rowMax-rowMin));
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = 0; i < indices.size(); i++) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}
			return reorderedIndices;
			
		} else if (reorderCrit.equals("Certainty1")) {
			indices = new ArrayList<IndexAndCost>();
			for (int i = 0; i < s; i++) {
				double rowMin = Double.MAX_VALUE;
				double rowMin2 = Double.MAX_VALUE;
				for (int j = 0; j < t; j++) {
					if (cm[i][j] < rowMin) {
						if (rowMin < rowMin2) {
							rowMin2 = rowMin;
						}
						rowMin = cm[i][j];
					} else {
						if (cm[i][j] < rowMin2) {
							rowMin2 = cm[i][j];
						}
					}
				}
				IndexAndCost iac = new IndexAndCost(i, (rowMin2+1)/(rowMin+1)); // index and certainty
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = 0; i < s; i++) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}
			return reorderedIndices;

		} else if (reorderCrit.equals("Certainty2")) {
			indices = new ArrayList<IndexAndCost>();
			for (int i = 0; i < s; i++) {
				double rowMin = Double.MAX_VALUE;
				double mean = 0.;
				for (int j = 0; j < t; j++) {
					mean += cm[i][j];
					if (cm[i][j] < rowMin) {
						rowMin = cm[i][j];
					} 
				}
				mean /= t;
				IndexAndCost iac = new IndexAndCost(i, rowMin - mean); // index and certainty
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = 0; i < s; i++) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}
			return reorderedIndices;
			
		} else if (reorderCrit.equals("Certainty3")) {
			indices = new ArrayList<IndexAndCost>();
			for (int i = 0; i < s; i++) {
				double rowMin = Double.MAX_VALUE;
				int minIndex = -1;
				for (int j = 0; j < t; j++) {
					if (cm[i][j] < rowMin) {
						rowMin = cm[i][j];
						minIndex =j;
					} 
				}
				int position = 0;
				for (int k = 0; k < s; k++){
					if (cm[minIndex][k] < rowMin){
						position++;
					}
				}
				IndexAndCost iac = new IndexAndCost(i, position); // index and certainty
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = 0; i < s; i++) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}
			return reorderedIndices;

		} else if (reorderCrit.equals("NumOfTies")) {
			indices = new ArrayList<IndexAndCost>(); // index and numOfTies!!!
			for (int i = 0; i < s; i++) {
				double rowMin = Double.MAX_VALUE;
				int numOfTies = 0;
				for (int j = 0; j < t; j++) {
					if (cm[i][j] < rowMin) {
						rowMin = cm[i][j];
						numOfTies = 0; // reset
					} else {
						if (cm[i][j] == rowMin) {
							numOfTies++;
						}
					}
					
				}
				IndexAndCost iac = new IndexAndCost(i, numOfTies);
				indices.add(iac);
			}
			Collections.sort(indices);
			for (int i = 0; i < indices.size(); i++) {
				reorderedIndices.add(indices.get(i).getIndex());
			}
			for (int i = s; i < (s + t); i++) {
				reorderedIndices.add(i);
			}

			return reorderedIndices;
		} else {
			System.err.println("ERROR NO VALID REORDER CRITERION GIVEN!!!");
			System.exit(0);
		}
		return null;
	}


	/**
	 * prints out the assignment (for debugging)
	 */
	public void printAssignment(int[][] assignment, double[][] cm) {
		System.out.println("Assignment:");
		for (int i = 0; i < assignment.length; i++){
			System.out.println(assignment[i][0] +" --> "+assignment[i][1] +" ("+cm[assignment[i][0]][assignment[i][1]]+")");
		}
	}



	

	


	





	
	
	
	



}
