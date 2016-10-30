package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.lang.Math;

import searchclient.NotImplementedException;
import searchclient.HeuristicUtil.*;


public abstract class Heuristic implements Comparator<Node> {
	protected HashMap<Character, LinkedList<Integer>> goalMap;
    
    public Heuristic(Node initialState) {  	
        // Here's a chance to pre-process the static parts of the level.
      
        // goalMap: maps goal char to its board locations (idx).
        goalMap = new HashMap<Character, LinkedList<Integer>>();

        // Populate goals
        for(int r = 0;  r < Node.MAX_ROW; r++) {
            for(int c = 0; c < Node.MAX_COL; c++) {
                char goalChr = initialState.goals[r][c];
                if(goalChr != '\u0000') {
                	int idx = HeuristicUtil.coordsToIdx(r,c, Node.MAX_COL);
                	if (goalMap.get(goalChr) == null) {
                		LinkedList<Integer> newList = new LinkedList<Integer>();
                		newList.add(idx);
                		goalMap.put(goalChr, newList);
                	}
                	else {
                		goalMap.get(goalChr).add(idx);
                	}
                }
            }
            
        }
    }

    public int h(Node n) {
        return HeuristicUtil.agentToClosestBoxManhattan(n) 
        	+ HeuristicUtil.sumGoalsToClosestBoxManhattan(n, goalMap);
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
