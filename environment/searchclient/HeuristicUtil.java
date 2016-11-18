package searchclient;

import java.lang.Math;
import java.lang.Character;
import java.util.HashMap;
import java.util.LinkedList;

import searchclient.NotImplementedException;


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

    public static int agentToClosestBoxManhattan(Node n) {
        // Subtract 1 because the agent only has to be next to the box to act on it.
        return findClosestBoxDistance(n.agentRow, n.agentCol, n) - 1;
    }

    public static int sumGoalsToClosestBoxManhattan(Node n, 
        HashMap<Character, LinkedList<Integer>> goalMap) {
        int totalDist = 0;
        // loop over all goals, find the closest box for each one.
        for (char goal : goalMap.keySet()) {
            LinkedList<Integer> idxs = goalMap.get(goal);
            for (int idx : idxs) {
                int[] loc = HeuristicUtil.idxToCoords(idx, Node.MAX_COL);
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
                    int dist = computeManhattan(r, c, srcRow, srcCol);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
            }
        }
        return minDist;
    }

    public static int[][] makeAPSP() {
        int[][] distances = new int[Node.MAX_ROW][Node.MAX_COL];
        for(int v = 0; v < Node.MAX_COL * Node.MAX_ROW; i++) {
            distances[v][v] = 0;
        }

    }

    private int[][] initDistanceMatrix() {
        int numVertices = Node.MAX_ROW * Node.MAX_COL;
        int[][] distances = new int[numVertices][numVertices];
        for(int u = 0; u < numVertices; u++) {
            for(int v = 0; v < numVertices; v++) {
                int[] uLocCoords = idxToCoords(u, Node.MAX_COL);
                int[] vLocCoords = idkToCoords(v, Node.MAX_COL);
                if(u == v) {
                    distance[u][v] = 0;
                }
                if(cellIsNotWall(r + 1, c)) {
                    distance[locIdx][coordsToIdx(r + 1, c, Node.MAX_COL)] = 1;
                }else {
                    distance[locIdx][coordsToIdx(r + 1, c, Node.MAX_COL)] = Integer.MAX_VALUE;
                }
                if(cellIsNotWall(r - 1, c)) {
                    distance[locIdx][coordsToIdx(r - 1, c, Node.MAX_COL)] = 1;
                }else {
                    distance[locIdx][coordsToIdx(r - 1, c, Node.MAX_COL)] = Integer.MAX_VALUE;
                }
                if(cellIsNotWall(r, c + 1)) {
                    distance[locIdx][coordsToIdx(r, c + 1, Node.MAX_COL)] = 1;
                }else {
                    distance[locIdx][coordsToIdx(r, c + 1, Node.MAX_COL)] = Integer.MAX_VALUE;
                }
                if(cellIsNotWall(r, c - 1)) {
                    distance[locIdx][coordsToIdx(r, c - 1, Node.MAX_COL)] = 1;
                }else {
                    distance[locIdx][coordsToIdx(r, c - 1, Node.MAX_COL)] = Integer.MAX_VALUE;
                }
            }
        }



        HashMap<Integer, LinkedList<Integer>> adjacencyList = new HashMap<Integer, LinkedList<Integer>>();
        for(int r = 0; r < Node.MAX_ROW; r++) {
            for(int c = 0; c < Node.MAX_COL; c++) {
                LinkedList<Integer> adjacentNodes = new LinkedList<Integer>();
                if(cellIsNotWall(r + 1, c)) {
                    adjacentNodes.add(coordsToIdx(r + 1, c, Node.MAX_COL));
                }
                if(cellIsNotWall(r - 1, c)) {
                    adjacentNodes.add(coordsToIdx(r - 1, c, Node.MAX_COL));
                }
                if(cellIsNotWall(r, c + 1)) {
                    adjacentNodes.add(coordsToIdx(r, c + 1, Node.MAX_COL));
                }
                if(cellIsNotWall(r, c - 1)) {
                    adjacentNodes.add(coordsToIdx(r, c - 1, Node.MAX_COL));
                }
                adjacencyList.put(coordsToIdx(r, c, Node.MAX_COL), adjacentNodes);
            }
        }
        return adjacencyList;
    }

    private boolean cellIsNotWall(int r, int c) {
        if(r >= Node.MAX_ROW || r < 0 || c < 0 || c >= Node.MAX_COL) {
            return false;
        }
        return !Node.walls[r][c];
    }

    private boolean cellsAreAdjacent(int r1, int c1, int r2, int c2) {
        if(r1 == r2 && Math.abs(c1 - c2) == 1) return true;
        if(c1 == c2 && Math.abs(r1 - r2) == 1) return true;
        return false;
    }
}