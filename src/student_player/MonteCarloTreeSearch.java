package student_player;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import boardgame.Board;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class MonteCarloTreeSearch {
    static final double TURN_TIME = 1.8;
    int opponent;
    int player;
    int level;
    private Random rand = new Random(1848);
    int count = 0;
    HashSet<String> previousPositions;
    public MonteCarloTreeSearch() {
    }


    public TablutMove findNextMove(TablutBoardState state){
        Tree tree = new Tree(state);

        long start = System.currentTimeMillis();
        this.level++;
        this.player = state.getTurnPlayer();
        this.opponent = state.getOpponent();
        long end = (long)TURN_TIME * 1000;

        Node rootNode = tree.getRoot();

        while(System.currentTimeMillis() < start + end){
            Node promisingNode = selectPromisingNode(rootNode);
            if(promisingNode.getChildren().size() == 0){
                expandNode(promisingNode);
            }

            Node nodeToExplore = promisingNode;
            if(promisingNode.getChildren().size() > 0){
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            int playoutResult = simulateRandomPlayout(nodeToExplore);
            backPropogation(nodeToExplore, rootNode.getState().getTurnPlayer(), playoutResult);
        }
        Node winnerNode = rootNode.getChildWithMaxScore();
        System.out.println("UCT: " + UCT.uctValue(rootNode.getVisitCount(),winnerNode.getFitness(),winnerNode.getVisitCount()));
        int fit = 0;
        for(Node c: rootNode.getChildren()){
            for(Node d: c.getChildren()) {
                fit += d.getFitness();
            }
        }
        tree.setRoot(winnerNode);
        return winnerNode.getMove();
    }

    private Node selectPromisingNode(Node rootNode){
        Node node = rootNode;
        while(node.getChildren().size() != 0 && rootNode.getState().getTurnPlayer() == player){
            if(node.getState().getTurnPlayer() == player) {
                node = UCT.findBestNodeWithUCT(node);
            }else{
                node = UCT.findEnemyNodeWithUCT(node);
            }
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
        count++;
        Node tempNode = nodeToExplore;
        while(tempNode != null){
            tempNode.incrementVisit();
            tempNode.incrementFitness(fitness);
            tempNode = tempNode.getParent();
        }
    }

    private int simulateRandomPlayout(Node node){

        Node tempNode = new Node((TablutBoardState) node.getState().clone());
        int boardStatus = tempNode.getState().getWinner();
        if(boardStatus == tempNode.getState().getOpponent()){
            if(tempNode.getParent() != null) {
                tempNode.getParent().setFitness(Integer.MIN_VALUE);
            }
        }
        int dept = 0;
        while(boardStatus == Board.NOBODY ){
            TablutMove randomMove = null;

                randomMove = (TablutMove) tempNode.getState().getRandomMove();

            boolean valid = tempNode.getState().isLegal(randomMove);
            tempNode.getState().processMove(randomMove);
            boardStatus = tempNode.getState().getWinner();
            dept++;
        }

        if(boardStatus == this.opponent){
            return 0;
        }else if(boardStatus == this.player){
            return 1;
        }else{
            return 1/2;
        }
        //System.out.println("Dept: " + dept);
        //return boardStatus;
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

    public Move chooseGreedyMove(TablutBoardState bs) {
        int player_id = bs.getTurnPlayer();
        List<TablutMove> options = bs.getAllLegalMoves();

        // Set an initial move as some random one.
        TablutMove bestMove = options.get(rand.nextInt(options.size()));

        // This greedy player seeks to capture as many opponents as possible.
        int opponent = bs.getOpponent();
        int minNumberOfOpponentPieces = bs.getNumberPlayerPieces(opponent);
        boolean moveCaptures = false;

        // Iterate over move options and evaluate them.
        for (TablutMove move : options) {
            // To evaluate a move, clone the boardState so that we can do modifications on
            // it.
            TablutBoardState cloneBS = (TablutBoardState) bs.clone();

            // Process that move, as if we actually made it happen.
            cloneBS.processMove(move);

            // Check how many opponent pieces there are now, maybe we captured some!
            int newNumberOfOpponentPieces = cloneBS.getNumberPlayerPieces(opponent);

            // If this move caused some capturing to happen, then do it! Greedy!
            if (newNumberOfOpponentPieces < minNumberOfOpponentPieces) {
                bestMove = move;
                minNumberOfOpponentPieces = newNumberOfOpponentPieces;
                moveCaptures = true;
            }

            /*
             * If we also want to check if the move would cause us to win, this works for
             * both! This will check if black can capture the king, and will also check if
             * white can move to a corner, since if either of these things happen then a
             * winner will be set.
             */
            if (cloneBS.getWinner() == player_id) {
                bestMove = move;
                moveCaptures = true;
                break;
            }
            if (player_id == TablutBoardState.SWEDE && !moveCaptures) {
                Coord kingPos = bs.getKingPosition();

                // Don't do a move if it wouldn't get us closer than our current position.
                int minDistance = Coordinates.distanceToClosestCorner(kingPos);

                // Iterate over moves from a specific position, the king's position!
                for (TablutMove moveKing : bs.getLegalMovesForPosition(kingPos)) {
                    /*
                     * Here it is not necessary to actually process the move on a copied boardState.
                     * Note that it is more efficient NOT to copy the boardState. Consider this
                     * during implementation...
                     */
                    int moveDistance = Coordinates.distanceToClosestCorner(moveKing.getEndPosition());
                    if (moveDistance < minDistance) {
                        minDistance = moveDistance;
                        bestMove = moveKing;
                    }
                }
            }
        }

        /*
         * The king-functionality below could be included in the above loop to improve
         * efficiency. But, this is written separately for the purpose of exposition to
         * students.
         */

        // If we are SWEDES we also want to check if we can get closer to the closest
        // corner. Greedy!
        // But let's say we'll only do this if we CANNOT do a capture.

        return bestMove;
    }
}
