package student_player;

import tablut.TablutBoardState;
import tablut.TablutMove;
import boardgame.Board;


import java.util.ArrayList;

public class MonteCarloTreeSearch {
    static final double TURN_TIME = 1.7;

    public MonteCarloTreeSearch() {
    }

    public TablutMove findNextMove(TablutBoardState state){

        Tree tree = new Tree(state);
        long start = System.currentTimeMillis();
        Node rootNode = tree.getRoot();

        while(System.currentTimeMillis() < start + TURN_TIME * 1000){
            Node promisingNode = selectPromisingNode(rootNode);
            if(promisingNode.getChildren().size() == 0){
                expandNode(promisingNode);
            }

            Node nodeToExplore = promisingNode;
            if(promisingNode.getChildren().size() > 0){
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            int playoutResult = simulateRandomPlayout(nodeToExplore);
            backPropogation(nodeToExplore, nodeToExplore.getState().getOpponent() == TablutBoardState.MUSCOVITE ? TablutBoardState.SWEDE: TablutBoardState.MUSCOVITE, playoutResult);
        }
        System.out.println((System.currentTimeMillis()-start)/1000);
        Node winnerNode = rootNode.getChildWithMaxScore();
        tree.setRoot(winnerNode);
        return winnerNode.getMove();
    }

    private Node selectPromisingNode(Node rootNode){
        Node node = rootNode;
        while(node.getChildren().size() != 0){
            node = UCT.findBestNodeWithUCT(node);
        }
        return node;
    }

    private void expandNode (Node node){
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

    private void backPropogation(Node nodeToExplore, int player, double fitness){
        Node tempNode = nodeToExplore;
        while(tempNode != null){
            tempNode.incrementVisit();
            if(tempNode.getState().getTurnPlayer() == player){
                tempNode.incrementFitness(fitness);
            }
            tempNode = tempNode.getParent();
        }
    }

    private int simulateRandomPlayout(Node node){

        Node tempNode = new Node((TablutBoardState) node.getState().clone());
        int boardStatus = tempNode.getState().getWinner();
        if(boardStatus == tempNode.getState().getOpponent()){
            tempNode.getParent().setFitness(Integer.MIN_VALUE);
        }
        int dept = 0;
        while(boardStatus == Board.NOBODY && dept < 100){
            TablutMove randomMove = (TablutMove) tempNode.getState().getRandomMove();
            boolean valid = tempNode.getState().isLegal(randomMove);
            tempNode.getState().processMove(randomMove);
            boardStatus = tempNode.getState().getWinner();
        }

        return fitnessFunction(tempNode);
    }

    private int fitnessFunction(Node node){
        int fitness = 0;

        int player = node.getState().getTurnPlayer();
        int opponent = player == TablutBoardState.MUSCOVITE ? TablutBoardState.SWEDE : TablutBoardState.MUSCOVITE;

        if(player == TablutBoardState.MUSCOVITE){
            fitness+= 10*(16-node.getState().getNumberPlayerPieces(player));
        }else{
            fitness+= 10 *(9 - node.getState().getNumberPlayerPieces(player));
        }
        int winner = node.getState().getWinner();
        fitness += node.getState().getWinner() == player ? 20 : 0 + node.getState().getWinner() == opponent ? -20 : 0;

        Node parentNode = node.getParent();

        if(parentNode != null && parentNode.getState().getNumberPlayerPieces(opponent)> node.getState().getNumberPlayerPieces(opponent)){
            fitness+= 100;
        }



        return fitness;
    }
}
