
package aren;

import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayerC extends TablutPlayer {
	// private Random rand = new Random(System.currentTimeMillis());
	private Random rand = new Random(System.currentTimeMillis());
	private static final int FIRSTTURNTIME = 6000;
	private static final int TURNTIME = 2000;
	private static final int MULTIPLIER = 2;
	private static final int NUMSIMULATIONSFIRTSMOVE = 100;

	// private double avgSimTime = 0;
	// private int totalPerformedTests = 0;
	private boolean firstMove = true;
	long startime;
	long endtime;

	private TablutBoardState rootNode = null;

	/**
	 * You must modify this constructor to return your student number. This is
	 * important, because this is what the code that runs the competition uses to
	 * associate you with your agent. The constructor should do nothing else.
	 */
	public StudentPlayerC() {
		super("260684709");
	}

	/**
	 * This is the primary method that you need to implement. The ``boardState``
	 * object contains the current state of the game, which your agent must use to
	 * make decisions.
	 */
	public Move chooseMove(TablutBoardState boardState) {

		startime = System.currentTimeMillis();
		endtime = startime + TURNTIME;

		List<TablutMove> allMoves = boardState.getAllLegalMoves();
		List<TablutMove> intermediateMoves = null;

		TablutBoardState clonedBoardState = null;

		TablutMove bestMove = allMoves.get(rand.nextInt(allMoves.size()));
		double bestMoveGoodness = -1;
		double moveGoodness = 0;
		double[] moveGoodnessList = new double[allMoves.size()];
		int[] numSimList = new int[allMoves.size()];
		TablutMove[] movesList = new TablutMove[allMoves.size()];

		// --int numMovesRem = allMoves.size();
		int numPerformedTests = 0;
		double numTestsLeft = Integer.MAX_VALUE;
		int totalPerformedTests = 0;

		double avgSimTime = 0;
		long simStartTime = 0;
		long simEndTime = 0;
		long timeRemaining = TURNTIME;
		int numStepsToEnd = 0;

		//variables for heuristics
		int totalAllocatedHeuristicPoints = 0;
		int totAllocHeurPtsTemp;
		int numOpponentsInitially = 0;
		int heuristicVal = 0;
		int index = 0;
		int[] heuristicValues;

		// Calculating heuristic values for all children of current board state
		heuristicValues = new int[allMoves.size()];
		for (TablutMove move : allMoves) {
			clonedBoardState = (TablutBoardState) boardState.clone();
			numOpponentsInitially = boardState.getNumberPlayerPieces(boardState.getOpponent());
			clonedBoardState.processMove(move);
			heuristicVal = getHeuristic(clonedBoardState,
					numOpponentsInitially - clonedBoardState.getNumberPlayerPieces(boardState.getOpponent()));
			if (heuristicVal == Integer.MAX_VALUE)
				return move;
			heuristicValues[index] = heuristicVal;
			if(heuristicVal >= 0)
				totalAllocatedHeuristicPoints += heuristicVal;
			index++;
		}
		index = 0;
		totAllocHeurPtsTemp = totalAllocatedHeuristicPoints;

		// First turn
		if (firstMove) {
			endtime = startime + FIRSTTURNTIME;

			// creating the tree
			// TablutBoardState rootBoard = (TablutBoardState) boardState.clone();
			// TreeNode rootNode = new TreeNode(rootBoard, null, null);
			//
			// for(TablutMove move : allMoves)

			// Warmup for Monte-Carlo
			
			timeRemaining = endtime - TURNTIME - System.currentTimeMillis();
			
			while (timeRemaining > avgSimTime) {
				
				//for all possible moves
				for (TablutMove move : allMoves) {

					//running simulations for a given move
					while (numPerformedTests <= numTestsLeft) {
//					while (numPerformedTests <= NUMSIMULATIONSFIRTSMOVE) {	

						simStartTime = System.currentTimeMillis();

						clonedBoardState = (TablutBoardState) boardState.clone();
						clonedBoardState.processMove(move);

						// This basically simulates a game by choosing random moves until the game ends
						while (clonedBoardState.getWinner() == Board.NOBODY) {

							intermediateMoves = clonedBoardState.getAllLegalMoves();
							// improve this, choose bettr than random?
							clonedBoardState.processMove(intermediateMoves.get(rand.nextInt(intermediateMoves.size())));
//ADD CHILD TO CURRENT HEAD
						}

						// if the game ends in victory, the original mov choice improves in "goodness"
						// if (clonedBoardState.getWinner() == player_id) {
						// moveGoodness++;
						// }

						simEndTime = System.currentTimeMillis();

						timeRemaining = endtime - TURNTIME - System.currentTimeMillis();
						if (timeRemaining < avgSimTime)
							break;

						numPerformedTests++;
						totalPerformedTests++;
						//getting a good starting value for the next turns
						avgSimTime = (((totalPerformedTests - 1) * avgSimTime) + (simEndTime - simStartTime))
								/ totalPerformedTests;

//						//DECIDING HOW MANY MORE SIMULATIONS TO RUN FOR A GIVEN MOVE
//						// might want to change numMovesRem
//						if (avgSimTime == 0) {
//							// --numTestsLeft = (int) (timeRemaining / (numMovesRem));
//							numTestsLeft = (int) ((timeRemaining * heuristicValues[index])
//									/ (totalAllocatedHeuristicPoints));
//						} else {
//							// --numTestsLeft = (int) (timeRemaining / (avgSimTime * numMovesRem));//Equal
//							numTestsLeft = (int) ((timeRemaining * heuristicValues[index])
//									/ (avgSimTime * totAllocHeurPtsTemp));// Distribution based on heuristic
//						}
						

					}

					if (timeRemaining < avgSimTime)
						break;

					// System.out.print(numPerformedTests + "-");

					numPerformedTests = 0;
					numTestsLeft = Integer.MAX_VALUE;
					// --numMovesRem--;
					totAllocHeurPtsTemp -= heuristicValues[index];
					index++;

				} 
			}
			

			// setup to get to next part

			// --numMovesRem = allMoves.size() - 1;
			firstMove = false;
			// System.out.println("test Finished");
		} // End First Move

		// For all moves

		for (TablutMove move : allMoves) {

			moveGoodness = 0;

			if (heuristicValues[index] >= 0) {
				while (numPerformedTests <= numTestsLeft) {

					simStartTime = System.currentTimeMillis();

					clonedBoardState = (TablutBoardState) boardState.clone();
					clonedBoardState.processMove(move);

					// This basically simulates a game by choosing random moves until the game ends
					while (clonedBoardState.getWinner() == Board.NOBODY) {
						// xxnumStepsToEnd++;
						intermediateMoves = clonedBoardState.getAllLegalMoves();
						clonedBoardState.processMove(intermediateMoves.get(rand.nextInt(intermediateMoves.size())));
					}

					// if the game ends in victory, the original mov choice improves in "goodness"
					if (clonedBoardState.getWinner() == player_id) {
						moveGoodness++;
						// xxmoveGoodness += (1 / numStepsToEnd);
					}
					numStepsToEnd = 0;

					simEndTime = System.currentTimeMillis();

					timeRemaining = endtime - System.currentTimeMillis();
					if (timeRemaining < avgSimTime)
						return bestMove;
					//					break;

					numPerformedTests++;
					totalPerformedTests++;
					avgSimTime = (((totalPerformedTests - 1) * avgSimTime) + (simEndTime - simStartTime))
							/ totalPerformedTests;

					// might want to change numMovesRem
					if (avgSimTime == 0) {
						// --numTestsLeft = (int) (timeRemaining / (numMovesRem));
						numTestsLeft = (int) ((timeRemaining * heuristicValues[index])
								/ (totalAllocatedHeuristicPoints));// Distribution based on heuristic
					} else {
						// --numTestsLeft = (int) (timeRemaining / (avgSimTime * numMovesRem));//Equal
						// Distribution
						numTestsLeft = (int) ((timeRemaining * heuristicValues[index])
								/ (avgSimTime * totalAllocatedHeuristicPoints));// Distribution based on heuristic
					}

				} 
			
			if (timeRemaining < avgSimTime)
				break;

			// *
			// TODO: modify in order to give more value to moves performed more often
			//moveGoodness = moveGoodness * Math.log(numPerformedTests) / numPerformedTests;
			moveGoodness = moveGoodness / Math.sqrt(numPerformedTests);
			//moveGoodness = moveGoodness / numPerformedTests;
			
			
//			moveGoodnessList[index] = moveGoodness;
//			numSimList[index] = numPerformedTests;
//			movesList[index] = move;

					 if (bestMoveGoodness < moveGoodness) {
					 bestMoveGoodness = moveGoodness;
					 bestMove = move;
					 }
					 
					System.out.print(numPerformedTests + "-");

			numPerformedTests = 0;
			numTestsLeft = Integer.MAX_VALUE;
			// --numMovesRem--;
			totalAllocatedHeuristicPoints -= heuristicValues[index];
			index++;
			}
			else {
				System.out.println("none-");
			}
		}

		System.out.println("total is " + totalPerformedTests);
		// */
		// Return your move to be processed by the server.

//		for (int i = 0; i < allMoves.size(); i++) {
//
//			moveGoodness = (float) ((moveGoodnessList[i] / numSimList[i])
//					+ Math.sqrt(UCTCONSTANT * Math.log(totalPerformedTests) / numSimList[i])); //
//
//			if (bestMoveGoodness < moveGoodness) {
//				bestMoveGoodness = moveGoodness;
//				bestMove = movesList[i];
//			}
//
//		}

		return bestMove;

	}

	public int getHeuristic(TablutBoardState boardState, int numCaptured) {
		int heuristic = 10;// baseValue
		if (player_id == TablutBoardState.MUSCOVITE) {
			// Black
			return 1;
		} else {
			// SWEDE - White
			// need to capture the king
			
			//THINGS THAT DO NOT MATTER
			// number of my players
			//
			
			Coord kingPos = boardState.getKingPosition();

			// if we have already won
			if (boardState.getWinner() == player_id)
				return Integer.MAX_VALUE;
			
			 for(TablutMove move : boardState.getLegalMovesForPosition(kingPos)) {
				// if king has direct path to a corner
				 if(Coordinates.isCorner(move.getEndPosition()))
					 return -1;
				 if (move.getEndPosition().x == 0 || move.getEndPosition().y == 0 || move.getEndPosition().x == 7 || move.getEndPosition().y == 7 )
					 heuristic -= 1; //TODO
			 }
			 

			// number of opponents
			//heuristic += boardState.getNumberPlayerPieces(boardState.getOpponent());
			heuristic += 2*numCaptured;

			

			// king distance from corner

			Coordinates.distanceToClosestCorner(kingPos);

			// + if king gets outside of center area
			// + if king can be killed with one move
			// - number of exposed teammates
			// - number of opponents around the king

			return heuristic;
		}

	}

}