package aren;

import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
    // private Random rand = new Random(System.currentTimeMillis());
    private Random rand = new Random(System.currentTimeMillis());
    private static final int NUMBEROFTRIES = 10;
    private static final int FIRSTTURNTIME = 4000;
    private static final int TURNTIME = 2000;
    private static final int UCTCONSTANT = 2;

    //private double avgSimTime = 0;
    //private int totalPerformedTests = 0;
    private boolean firstMove = true;
    long startime;
    long endtime;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260684709");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {

        startime = System.currentTimeMillis();

        // if(turnNum == 0) {
        // //first move (30s)
        // endtime = startime + FIRSTTURNTIME;
        //
        // // You probably will make separate functions in MyTools.
        // // For example, maybe you'll need to load some pre-processed best opening
        // // strategies...
        //
        // MyTools.getSomething();
        //
        //
        // }
        // else {
        // //other moves (2s)
        // endtime = startime + TURNTIME;
        //
        //
        // }

        // Is random the best you can do?
		/*
		 * Move bestMove = boardState.getRandomMove(); /
		 */

        endtime = startime + TURNTIME;
        List<TablutMove> allMoves = boardState.getAllLegalMoves();
        List<TablutMove> intermediateMoves = null;

        TablutMove bestMove = allMoves.get(rand.nextInt(allMoves.size()));
        float bestMoveGoodness = -1;
        float moveGoodness = 0;

        int numMovesRem = allMoves.size();
        int numPerformedTests = 0;
        double numTestsLeft = Integer.MAX_VALUE;
        int totalPerformedTests = 0;

        double avgSimTime = 0;
        long simStartTime = 0;
        long simEndTime = 0;
        long timeRemaining = TURNTIME;
//		System.out.println(firstMove);

        if (firstMove) {
            endtime = startime + FIRSTTURNTIME;
            for (TablutMove move : allMoves) {

                while (numPerformedTests <= numTestsLeft) {

                    simStartTime = System.currentTimeMillis();

                    TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
                    clonedBoardState.processMove(move);

                    // This basically simulates a game by choosing random moves until the game ends
                    while (clonedBoardState.getWinner() == Board.NOBODY) {

                        intermediateMoves = clonedBoardState.getAllLegalMoves();
                        // improve this, choose bettr than random?
                        TablutMove newmove = intermediateMoves.get(rand.nextInt(intermediateMoves.size()));
                        clonedBoardState.processMove(newmove);
                    }

                    // if the game ends in victory, the original mov choice improves in "goodness"
                    if (clonedBoardState.getWinner() == player_id) {
                        moveGoodness++;
                    }

                    simEndTime = System.currentTimeMillis();

                    timeRemaining = endtime - TURNTIME - System.currentTimeMillis();
                    if (timeRemaining < avgSimTime)
                        break;

                    numPerformedTests++;
                    totalPerformedTests++;
                    avgSimTime = (((totalPerformedTests - 1) * avgSimTime) + (simEndTime - simStartTime))
                            / totalPerformedTests;

                    // might want to change numMovesRem
                    if (avgSimTime == 0) {
                        if (numMovesRem == 0) {
                            break;
                        }
                        numTestsLeft = (int) (timeRemaining / (numMovesRem));
                    } else {
                        numTestsLeft = (int) (timeRemaining / (avgSimTime * numMovesRem));
                    }

                }

                if (timeRemaining < avgSimTime)
                    break;

//				System.out.print(numPerformedTests + "-");

                numPerformedTests = 0;
                numTestsLeft = Integer.MAX_VALUE;
                numMovesRem--;

            }

            //setup to get to next part


            //moveGoodness = 0;//Handled Later

            numMovesRem = allMoves.size();
            //numPerformedTests = 0;//Handled Before
            //numTestsLeft = Integer.MAX_VALUE;//Handled Before
            //totalPerformedTests = 0;//NO NEED

            //avgSimTime = 0; //No Need
            //simStartTime = 0;//Handled later
            //simEndTime = 0;//handled later
            //timeRemaining = TURNTIME; //handled later

            firstMove = false;
//			System.out.println("test Finished");
        }//End First Move


        // For all moves

        for (TablutMove move : allMoves) {

            moveGoodness = 0;

            while (numPerformedTests <= numTestsLeft) {

                simStartTime = System.currentTimeMillis();

                TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
                clonedBoardState.processMove(move);

                // This basically simulates a game by choosing random moves until the game ends
                while (clonedBoardState.getWinner() == Board.NOBODY) {

                    intermediateMoves = clonedBoardState.getAllLegalMoves();
                    // improve this, choose bettr than random?
                    TablutMove newmove = intermediateMoves.get(rand.nextInt(intermediateMoves.size()));
                    clonedBoardState.processMove(newmove);
                }

                // if the game ends in victory, the original mov choice improves in "goodness"
                if (clonedBoardState.getWinner() == player_id) {
                    moveGoodness++;
                }

                simEndTime = System.currentTimeMillis();

                timeRemaining = endtime - System.currentTimeMillis();
                if (timeRemaining < avgSimTime)
                    return bestMove;

                numPerformedTests++;
                totalPerformedTests++;
                avgSimTime = (((totalPerformedTests - 1) * avgSimTime) + (simEndTime - simStartTime))
                        / totalPerformedTests;

                // might want to change numMovesRem
                if (avgSimTime == 0) {
                    if (numMovesRem == 0) {
                        return bestMove;
                    }
                    numTestsLeft = (int) (timeRemaining / (numMovesRem));
                } else {
                    numTestsLeft = (int) (timeRemaining / (avgSimTime * numMovesRem));
                }

            }

            // *
            moveGoodness = moveGoodness / numPerformedTests;
			/*
			 * / //using UCT moveGoodness = (float) ((moveGoodness / numPerformedTests) +
			 * Math.sqrt(UCTCONSTANT * Math.log(totalPerformedTests)/numPerformedTests)); //
			 */
            if (bestMoveGoodness < moveGoodness) {
                bestMoveGoodness = moveGoodness;
                bestMove = move;
            }

//			System.out.print(numPerformedTests + "-");

            numPerformedTests = 0;
            numTestsLeft = Integer.MAX_VALUE;
            numMovesRem--;

        }

//		System.out.println("total is " + totalPerformedTests);
        // */
        // Return your move to be processed by the server.

            return bestMove;

    }
}