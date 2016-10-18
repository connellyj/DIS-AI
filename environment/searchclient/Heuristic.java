package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.lang.Math;

import searchclient.NotImplementedException;


public abstract class Heuristic implements Comparator<Node> {

	public class HeuristicUtil {
	public static int coordsToIdx(int row, int col, int cols) {
		return row * cols + col;
	}

	public static int[] idxToCoords(int idx, int cols) {
		int[] result = new int[2];
		result[0] = idx / cols; // row
		result[1] = idx % cols; // col
		return result;
	}

	public static int computeManhattan(int r1, int c1, int r2, int c2) {
		return Math.abs(r1-r2) + Math.abs(c1-c2);
	}
}

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		HashMap<int, char> boxes = new HashMap<int, char>();
		HashMap<int, char> goals = new HashMap<int, char>();

		for(int r = 0;  r < Node.MAX_ROW; r++) {
			for(int c = 0; c < Node.MAX_COL; c++) {
				char boxChr = initalState.boxes[r][c];
				if(boxChr != null) {
					boxes.add(coordsToIdx(r, c, Node.MAX_COL+1), boxChr);
				}
				char goalChr = initalState.goals[r][c];
				if(goalChr != null) {
					goals.add(coordsToIdx(r, c, Node.MAX_COL+1), goalChr);
				}
			}
		}
	}

	public int h(Node n) {
		return 0;
	}

	public static int coordsToIdx(int row, int col, int cols) {
		return row * cols + col;
	}

	public static int[] idxToCoords(int idx, int cols) {
		int[] result = new int[2];
		result[0] = idx / cols; // row
		result[1] = idx % cols; // col
		return result;
	}

	public static int computeManhattan(int r1, int c1, int r2, int c2) {
		return Math.abs(r1-r2) + Math.abs(c1-c2);
	}

	private int boxAgentManhattan(Node n) {

	}

	private int[] findClosestBoxDistance(int row, int col, Node n) {
		return findClosestBoxDistance(row, col, n, null);

	}

	private int findClosestBoxDistance(int row, int col, Node n, char chr) {
		int minDist = Node.MAX_ROW + Node.MAX_COL;
		for (idx = 0; idx < Node.MAX_ROW * Node.MAX_COL; idx++) {
			if ((chr == null && boxes.get(idx) != null) || boxes.get(idx) == chr) {
				boxCoords = HeuristicUtil.idxToCoords(idx, Node.MAX_COL);
				int dist = HeuristicUtil.computeManhattan(boxCoords[0], boxCoords[1], row, col);
				if (dist < minDist) {
					minDist = dist;
				}
			}
		}
		return minDist;
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int W) {
			super(initialState);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
