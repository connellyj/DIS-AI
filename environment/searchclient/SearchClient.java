package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

		int row = 0;
		boolean agentFound = false;

		int max_col = line.length();
		// dis is expensive and slightly gross
		int max_row = (int) serverMessages.lines().count();
		System.err.println(max_row);

		
		int agentRow;
		int agentCol;
		LinkedList<int[]> boxCoords = new LinkedList<int[]>();
		LinkedList<int[]> goalCoords = new LinkedList<int[]>();

		while (!line.equals("")) {
			for (int col = 0; col < line.length(); col++) {
				char chr = line.charAt(col);

				if (chr == '+') { // Wall.
					Node.walls[row][col] = true;
				} else if ('0' <= chr && chr <= '9') { // Agent.
					if (agentFound) {
						System.err.println("Error, not a single agent level");
						System.exit(1);
					}
					agentFound = true;
					agentRow = row;
					agentCol = col;
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					int[] boxInfo = new int[3];
					boxInfo[0] = row;
					boxInfo[1] = col;
					boxInfo[2] = chr;
					boxCoords.add(boxInfo);
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					int[] goalInfo = new int[3];
					goalInfo[0] = row;
					goalInfo[1] = col;
					goalInfo[2] = chr;
					goalCoords.add(goalInfo);
				} else if (chr == ' ') {
					// Free space.
				} else {
					System.err.println("Error, read invalid level character: " + (int) chr);
					System.exit(1);
				}
			}
			line = serverMessages.readLine();
			row++;
		}

		Node.initNodeStatics(max_row+1, max_col+1);
		this.initialState = new Node(null);

		this.initialState.agentRow = agentRow;
		this.initialState.agentCol = agentCol;

		for (int[] boxInfo : boxCoords) {
			this.initialState[boxInfo[0]][boxInfo[1]] = boxInfo[2];
		}

		for (int[] goalInfo : goalCoords) {
			Node.goals[goalInfo[0]][goalInfo[1]] = goalInfo[2];
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
		// strategy = new StrategyBestFirst(new AStar(client.initialState));
		// You're welcome to test WA* out with different values, but for the report you must at least indicate benchmarks for W = 5.
		// strategy = new StrategyBestFirst(new WeightedAStar(client.initialState, 5));
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
