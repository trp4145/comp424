package student_player;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("MAXN");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    private static Random rand = new Random(System.currentTimeMillis());

    public static final int TURN_TIME = 1800;
    private static int player = 0;
    private static int opponent = 0;
    static long startTime;
    static long randomMoveTime = 0;
    static long processMoveTime = 0;
    static long statusTime = 0;
    static long promisEnd, promisStart, expandEnd, expandStart, playEnd, playStart,backStart,backEnd,getMoveStart,getMoveEnd,childStart,childEnd,simulaStart,randomMoveStart,randomMoveEnd,moveSize;
    public Move chooseMove(TablutBoardState boardState) {
        if(statusTime == 1) {
            Node rootNode = new Node(boardState);
            player = boardState.getTurnPlayer();
            opponent = boardState.getOpponent();
            startTime = System.currentTimeMillis();
            int numSimulationAvailable = Integer.MAX_VALUE;
            int simulationsComplete = 0;
            double averageSimulationTime = 0;

            while ((System.currentTimeMillis() - startTime) < TURN_TIME) {
                expandStart = 0;
                expandEnd = 0;
                long simultationStartTime = System.currentTimeMillis();
                promisStart = System.currentTimeMillis();
                Node promisingNode = selectPromisingNode(rootNode);
                promisEnd = System.currentTimeMillis();

                if (promisingNode.getChildren().size() == 0) {
                    expandStart = System.currentTimeMillis();
                    expandNode(promisingNode);
                    expandEnd = System.currentTimeMillis();

                }
                Node nodeToExplore = promisingNode;
                playStart = System.currentTimeMillis();
                double playoutResult = simulateRandomPlayout(nodeToExplore);
                playEnd = System.currentTimeMillis();
                backStart = System.currentTimeMillis();
                backPropogation(nodeToExplore, playoutResult);
                backEnd = System.currentTimeMillis();
                long simultaionEndTime = System.currentTimeMillis();
                simulationsComplete++;
                averageSimulationTime = (((simulationsComplete - 1) * averageSimulationTime) + (simultaionEndTime - simultationStartTime)) / (double) simulationsComplete;
                //numSimulationAvailable = (int)((TURN_TIME-System.currentTimeMillis()+startTime)/averageSimulationTime);
            }
            long sortStart = System.currentTimeMillis();
            Node winnerNode = rootNode.getChildWithMaxScore();
            long sortEnd = System.currentTimeMillis();
            return winnerNode.getMove();
        }else{
            MonteCarloTreeSearch mt = new MonteCarloTreeSearch();
            return mt.findNextMove(boardState);
        }
    }
    private static Node selectPromisingNode(Node rootNode){
        Node node = rootNode;
        while(node.getChildren().size() != 0){
            node = UCT.findBestNodeWithUCT(node, node.getState().getTurnPlayer() == player);
        }

        return node;
    }

    private static boolean outOfTime(){
        return System.currentTimeMillis()-startTime > TURN_TIME;
    }
    private static void expandNode (Node node){
        getMoveStart = System.currentTimeMillis();
        ArrayList<TablutMove> possibleStates = node.getState().getAllLegalMoves();
        getMoveEnd = System.currentTimeMillis();
        childStart = System.currentTimeMillis();
        for(TablutMove move: possibleStates){
            TablutBoardState clonedState = (TablutBoardState) node.getState().clone();
            clonedState.processMove(move);
            Node newNode = new Node(clonedState);
            newNode.setMove(move);
            newNode.setParent(node);
            node.getChildren().add(newNode);
        }
        childEnd = System.currentTimeMillis();
    }

    private static void backPropogation(Node nodeToExplore, double playoutPoints){
        Node tempNode = nodeToExplore;
        while(tempNode != null){
            tempNode.incrementVisit();
            tempNode.incrementFitness(playoutPoints,true);
            tempNode.incrementFitness(1- playoutPoints, false);
            tempNode = tempNode.getParent();
        }
    }

    private static double simulateRandomPlayout(Node node){
        Node tempNode = new Node((TablutBoardState) node.getState().clone());
        tempNode.setParent(node.getParent());
        int boardStatus = tempNode.getState().getWinner();
        if(boardStatus == opponent){
            if(tempNode.getParent() == null) {
                return Integer.MIN_VALUE;
            }
        }else if(boardStatus == player){
            if(tempNode.getParent() == null) {
                return Integer.MAX_VALUE;
            }
        }
        simulaStart = System.currentTimeMillis();
        while(boardStatus == Board.NOBODY && System.currentTimeMillis()-startTime < TURN_TIME ){
            if(outOfTime())break;
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ArrayList<TablutMove> moveList = tempNode.getState().getAllLegalMoves();
            // improve this, choose bettr than random?
            moveSize = moveList.size();
            randomMoveStart = System.currentTimeMillis();
            TablutMove randomMove = moveList.get(rand.nextInt(moveList.size()));
            randomMoveEnd = (System.currentTimeMillis());
            if(outOfTime())break;
            tempNode.getState().processMove(randomMove);
            processMoveTime = (System.currentTimeMillis() - startTime);
            if(outOfTime())break;
            boardStatus = tempNode.getState().getWinner();
            statusTime = (System.currentTimeMillis()-startTime);
            //System.out.println("Random: " + randomMoveTime + " Process: " + processMoveTime + " Status: " + statusTime);
        }


        if(boardStatus == opponent){
            return 0;
        }else if(boardStatus == player){
            return 1;
        }else{
            return 0.5;
        }
    }
}