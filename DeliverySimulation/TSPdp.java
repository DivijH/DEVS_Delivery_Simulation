package Component.DeliverySimulation;

/**
 * 
 * Time Complexity: O(n^2 * 2^n)
 * Space Complexity: O(n * 2^n)
 *
 **/

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class responsible for deciding the paths of all trucks for the day
 * 
 * @author Divij and Arit
 */
public class TSPdp {

	// Variables containing the information about the delivery
	private final int N, start;
	private final double[][] distance;
	private List<Integer> tour = new ArrayList<>();
	private double minTourCost = Double.POSITIVE_INFINITY;
	private boolean ranSolver = false;

	/**
	 * Parameterized Constructor for the class
	 * @param distance Array of array containing the distances of the zones with each other
	 */
	public TSPdp(double[][] distance) {
		this(0, distance);
	} 

	/**
	 * Parameterized constructor for the class
	 * @param start Starting position of the truck
	 * @param distance Array of array containing the distances of the zones with each other
	 */
	public TSPdp(int start, double[][] distance) {
	    N = distance.length;
	
	    if(N != distance[0].length)
	    	throw new IllegalStateException("Matrix must be square (n x n)");
	    if(start<0 || start>=N)
	    	throw new IllegalArgumentException("Invalid start node.");

	    this.start = start;
	    this.distance = distance;
	}
	
	/**
	 * Function returning the optimal tour for the traveling salesman problem
	 * @return List of integers with the optimal tour
	 */
	public List<Integer> getTour() {
		if (!ranSolver) solve();
			return tour;
	}
	
	/**
	 * Function returning the minimal tour cost
	 * @return Value of the minimal tour cost
	 */
	public double getTourCost() {
		if(!ranSolver)
			solve();
		return minTourCost;
	}
	
	/**
	 * Function responsible for solving the travelling salesman problem and caches solution
	 */
	public void solve() {
		if(ranSolver)
			return;
	
		final int END_STATE = (1 << N) - 1;
		Double[][] memo = new Double[N][1 << N];
	
		// Add all outgoing edges from the starting node to memo table.
		for(int end=0; end<N; end++) {
			if(end==start)
				continue;
			memo[end][(1 << start) | (1 << end)] = distance[start][end];
		}

		for(int r=3; r<=N; r++) {
			for(int subset : combinations(r, N)) {
				if(notIn(start, subset))
					continue;
				for(int next=0; next<N; next++) {
					if(next==start || notIn(next, subset))
						continue;
					int subsetWithoutNext = subset ^ (1 << next);
					double minDist = Double.POSITIVE_INFINITY;
					for(int end=0; end<N; end++) {
						if(end==start || end==next || notIn(end, subset))
							continue;
						double newDistance = memo[end][subsetWithoutNext] + distance[end][next];
						if(newDistance < minDist) {
							minDist = newDistance;
						}
					}
					memo[next][subset] = minDist;
				}
			}
		}

		// Connect tour back to starting node and minimize cost.
		for(int i=0; i<N; i++) {
			if(i==start)
				continue;
			double tourCost = memo[i][END_STATE] + distance[i][start];
			if(tourCost < minTourCost) {
				minTourCost = tourCost;
			}
		}

		int lastIndex = start;
		int state = END_STATE;
		tour.add(start);

		// Reconstruct TSP path from memo table.
		for(int i=1; i<N; i++) {
			int index = -1;
			for(int j=0; j<N; j++) {
				if(j==start || notIn(j, state))
					continue;
				if(index == -1)
					index = j;
				double prevDist = memo[index][state] + distance[index][lastIndex];
				double newDist  = memo[j][state] + distance[j][lastIndex];
				if(newDist < prevDist) {
					index = j;
				}
			}

			tour.add(index);
			state = state ^ (1 << index);
			lastIndex = index;
		}
		tour.add(start);
		Collections.reverse(tour);
		ranSolver = true;
	}
	
	private static boolean notIn(int elem, int subset) {
		return ((1 << elem) & subset) == 0;
	}
	
	/**
	 * Function generating all bit sets of size n where r bits are set to one. The result is returned as a list of integer masks.
	 */
	public static List<Integer> combinations(int r, int n) {
		List<Integer> subsets = new ArrayList<>();
		combinations(0, 0, r, n, subsets);
		return subsets;
	}
	
	/**
	 * Function responsible for finding all the combinations of size r we need to recurse until we have 
	 * selected r elements (aka r = 0), otherwise if r != 0 then we still need to select
	 * an element which is found after the position of our last selected element
	 */
	private static void combinations(int set, int at, int r, int n, List<Integer> subsets) {
	
		// Return early if there are more elements left to select than what is available.
		int elementsLeftToPick = n - at;
		if(elementsLeftToPick < r)
			return;
	
		// We selected 'r' elements so we found a valid subset!
		if(r==0) {
			subsets.add(set);
		} else {
			for (int i = at; i < n; i++) {
				set |= 1 << i;
				combinations(set, i + 1, r - 1, n, subsets);
				set &= ~(1 << i);
			}
		}
	}
	
	/**
	 * Starting point of the Travelling Salesman Problem
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		// Create adjacency matrix
		double[][] distanceMatrix = {
			{0,10,22},
			{10,0,42},
			{22,42,0}
		};
		int startNode = 0;
		TSPdp solver = new TSPdp(startNode, distanceMatrix);

		System.out.println("Tour: " + solver.getTour());
		System.out.println("Tour cost: " + solver.getTourCost());
	}
}