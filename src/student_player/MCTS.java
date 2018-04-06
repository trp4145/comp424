package student_player;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

import java.util.ArrayList;

public class MCTS {
    public static final int TURN_TIME = 1700;
    private static int player = 0;
    private static int opponent = 0;
    static long startTime;
    static long randomMoveTime = 0;
    static long processMoveTime = 0;
    static long statusTime = 0;
    public static Move findNextMove(TablutBoardState state){

        Node rootNode = new Node(state);
        player = state.getTurnPlayer();
        opponent = state.getOpponent();
        startTime = System.currentTimeMillis();
        int numSimulationAvailable = Integer.MAX_VALUE;
        int simulationsComplete = 0;
        double averageSimulationTime = 0;

        while((System.currentTimeMillis() - startTime) < TURN_TIME && numSimulationAvailable > 0){
            long simultationStartTime = System.currentTimeMillis();
            Node promisingNode = selectPromisingNode(rootNode);
            if(promisingNode.getChildren().size() == 0){
                expandNode(promisingNode);
            }
            Node nodeToExplore = promisingNode;
            double playoutResult = simulateRandomPlayout(nodeToExplore);
            backPropogation(nodeToExplore, playoutResult);
            long simultaionEndTime = System.currentTimeMillis();
            simulationsComplete++;
            averageSimulationTime = (((simulationsComplete-1)*averageSimulationTime) + (simultaionEndTime-simultationStartTime))/(double)simulationsComplete;
            numSimulationAvailable = (int)((TURN_TIME-System.currentTimeMillis()+startTime)/averageSimulationTime);
        }

        Node winnerNode = rootNode.getChildWithMaxScore();
        return winnerNode.getMove();
    }

    private static Node selectPromisingNode(Node rootNode){
        Node node = rootNode;
        while(node.getChildren().size() != 0){
            node = UCT.findBestNodeWithUCT(node, node.getState().getTurnPlayer() == player);
        }

        return node;
    }

    private static void expandNode (Node node){
        ArrayList<TablutMove> possibleStates = node.getState().getAllLegalMoves();
        for(TablutMove move: possibleStates){
            TablutBoardState clonedState = (TablutBoardState) node.getState().clone();
            clonedState.processMove(move);
            Node newNode = new Node(clonedState);
            newNode.setMove(move);
            newNode.setParent(node);
            node.getChildren().add(newNode);
        }
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

        while(boardStatus == Board.NOBODY && System.currentTimeMillis()-startTime < TURN_TIME ){
            TablutMove randomMove = (TablutMove) tempNode.getState().getRandomMove();
            randomMoveTime = (System.currentTimeMillis() - startTime);
            tempNode.getState().processMove(randomMove);
            processMoveTime = (System.currentTimeMillis() - startTime);

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
