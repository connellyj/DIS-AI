package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

import searchclient.Memory;
import searchclient.Strategy.*;
import searchclient.Heuristic.*;

public class SearchClient {
    public Node initialState;

    public SearchClient(BufferedReader serverMessages) throws Exception {
        // Read lines specifying colors
        String line = serverMessages.readLine();
        if (line.matches("^[a-z]+:\\s*[0-9A-Z](\\s*,\\s*[0-9A-Z])*\\s*$")) {
            System.err.println("Error, client does not support colors.");
            System.exit(1);
        }

        boolean agentFound = false;

        int max_col = line.length();
        int max_row = 0;
        ArrayList<String> tmpServerMessages = new ArrayList<>();
        tmpServerMessages.add(line);
        while(!line.equals("")) {
            line = serverMessages.readLine();
            tmpServerMessages.add(line);
            max_row++;
        }

        Node.initNodeStatics(max_row, max_col);
        this.initialState = new Node(null);

        int row = 0;

        for(String tmpLine : tmpServerMessages) {
            for (int col = 0; col < tmpLine.length(); col++) {
                char chr = tmpLine.charAt(col);

                if (chr == '+') { // Wall.
                    this.initialState.walls[row][col] = true;
                } else if ('0' <= chr && chr <= '9') { // Agent.
                    if (agentFound) {
                        System.err.println("Error, not a single agent level");
                        System.exit(1);
                    }
                    agentFound = true;
                    this.initialState.agentRow = row;
                    this.initialState.agentCol = col;
                } else if ('A' <= chr && chr <= 'Z') { // Box.
                    this.initialState.boxes[row][col] = chr;
                } else if ('a' <= chr && chr <= 'z') { // Goal.
                    this.initialState.goals[row][col] = chr;
                } else if (chr == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) chr);
                    System.exit(1);
                }
            }
            row++;
        }
    }

    public LinkedList<Node> Search(Strategy strategy) throws IOException {
        System.err.format("Search starting with strategy %s.\n", strategy.toString());
        strategy.addToFrontier(this.initialState);

        int iterations = 0;
        while (true) {
            if (iterations == 1000) {
                System.err.println(strategy.searchStatus());
                iterations = 0;
            }

            if (strategy.frontierIsEmpty()) {
                return null;
            }

            Node leafNode = strategy.getAndRemoveLeaf();

            if (leafNode.isGoalState()) {
                return leafNode.extractPlan();
            }

            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Read level and create the initial state of the problem
        SearchClient client = new SearchClient(serverMessages);

        Strategy strategy;
        strategy = new StrategyBFS();
        // Ex 1:
        //strategy = new StrategyDFS();

        // Ex 3:
        //strategy = new StrategyBestFirst(new AStar(client.initialState));
        // You're welcome to test WA* out with different values, but for the report you must at least indicate benchmarks for W = 5.
        //strategy = new StrategyBestFirst(new WeightedAStar(client.initialState, 5));
        // strategy = new StrategyBestFirst(new Greedy(client.initialState));

        LinkedList<Node> solution;
        try {
            solution = client.Search(strategy);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
            solution = null;
        }

        if (solution == null) {
            System.err.println(strategy.searchStatus());
            System.err.println("Unable to solve level.");
            System.exit(0);
        } else {
            System.err.println("\nSummary for " + strategy.toString());
            System.err.println("Found solution of length " + solution.size());
            System.err.println(strategy.searchStatus());

            for (Node n : solution) {
                String act = n.action.toString();
                System.out.println(act);
                String response = serverMessages.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
                    System.err.format("%s was attempted in \n%s\n", act, n.toString());
                    break;
                }
            }
        }
    }
}
