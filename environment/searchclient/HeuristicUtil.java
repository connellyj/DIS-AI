package searchclient;

import java.lang.Math;
import java.lang.Character;
import java.util.HashMap;
import java.util.LinkedList;

import searchclient.NotImplementedException;


public class HeuristicUtil {

    static HeuristicUtil instance;
    private int[][] distancesMap;
    private HashMap<Character, LinkedList<Integer>> goalMap;

    public static void initHeuristic(Node initialState) {
        if(instance == null) {
            instance = new HeuristicUtil();
        }
        instance.makeAPSP();
        instance.initGoalMap(initialState);
    }

    private void initGoalMap(Node initialState) {
        // goalMap: maps goal char to its board locations (idx).
        goalMap = new HashMap<Character, LinkedList<Integer>>();

        // Populate goals
        for(int r = 0;  r < Node.MAX_ROW; r++) {
            for(int c = 0; c < Node.MAX_COL; c++) {
                char goalChr = initialState.goals[r][c];
                if(goalChr != '\u0000') {
                    int idx = HeuristicUtil.coordsToIdx(r, c);
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

    public static int coordsToIdx(int row, int col) {
        return row * Node.MAX_COL + col;
    }

    public static int[] idxToCoords(int idx) {
        int[] result = new int[2];
        result[0] = idx / Node.MAX_COL; // row
        result[1] = idx % Node.MAX_COL; // col
        return result;
    }

    public static int computeManhattan(int r1, int c1, int r2, int c2) {
        return Math.abs(r1-r2) + Math.abs(c1-c2);
    }

    public static int agentToClosestBoxManhattan(Node n) {
        // Subtract 1 because the agent only has to be next to the box to act on it.
        return findClosestBoxDistance(n.agentRow, n.agentCol, n) - 1;
    }

    public static int sumGoalsToClosestBoxManhattan(Node n) {
        int totalDist = 0;
        // loop over all goals, find the closest box for each one.
        for (char goal : instance.goalMap.keySet()) {
            LinkedList<Integer> idxs = instance.goalMap.get(goal);
            for (int idx : idxs) {
                int[] loc = HeuristicUtil.idxToCoords(idx);
                int dist = findClosestBoxDistance(loc[0], loc[1], n, goal);
                totalDist += dist;
            }
        }
 
        return totalDist;

    }

    // If there's no target box char, send flag value of '0'.
    public static int findClosestBoxDistance(int row, int col, Node n) {
        return findClosestBoxDistance(row, col, n, '0');
    }

    // Needs to take the target box letter as a parameter
    public static int findClosestBoxDistance(int srcRow, int srcCol, Node n, char targetChr) {
        targetChr = Character.toUpperCase(targetChr);
        int minDist = Node.MAX_ROW + Node.MAX_COL;
        for (int r = 0; r < Node.MAX_ROW; r++) {
            for (int c = 0; c < Node.MAX_COL; c++) {
                char box = n.boxes[r][c];
                if ((targetChr == '0' && box != '\u0000') || box  == targetChr) {
                    int dist = instance.distancesMap[coordsToIdx(r, c)][coordsToIdx(srcRow, srcCol)];
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
            }
        }
        return minDist;
    }

    private void makeAPSP() {
        distancesMap = initDistanceMatrix();
        int numVertices = Node.MAX_ROW * Node.MAX_COL;
        for(int k = 1; k < numVertices; k++) {
            for(int i = 1; i < numVertices; i++) {
                for(int j = 1; j < numVertices; j++) {
                    if(distancesMap[i][j] > distancesMap[i][k] + distancesMap[k][j]) {
                        distancesMap[i][j] = distancesMap[i][k] + distancesMap[k][j];
                    }
                }
            }
        }
    }

    private int[][] initDistanceMatrix() {
        int numVertices = Node.MAX_ROW * Node.MAX_COL;
        int[][] distances = new int[numVertices][numVertices];
        for(int u = 0; u < numVertices; u++) {
            for(int v = 0; v < numVertices; v++) {
                int[] uLocCoords = idxToCoords(u);
                int[] vLocCoords = idxToCoords(v);
                if(u == v) {
                    distances[u][v] = 0;
                }else if(cellsAreAdjacent(uLocCoords, vLocCoords) && cellIsNotWall(uLocCoords) && cellIsNotWall(vLocCoords)) {
                    distances[u][v] = 1;
                }else {
                    distances[u][v] = numVertices;
                }
            }
        }
        return distances;
    }

    private boolean cellIsNotWall(int[] loc) {
        if(loc[0] >= Node.MAX_ROW || loc[0] < 0 || loc[1] < 0 || loc[1] >= Node.MAX_COL) {
            return false;
        }
        return !Node.walls[loc[0]][loc[1]];
    }

    private boolean cellsAreAdjacent(int[] loc1, int[] loc2) {
        if(loc1[0] == loc2[0] && Math.abs(loc1[1] - loc2[1]) == 1) return true;
        if(loc1[1] == loc2[1] && Math.abs(loc1[0] - loc2[0]) == 1) return true;
        return false;
    }
}