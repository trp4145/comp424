package student_player;

import boardgame.Move;
import boardgame.Player;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoard;
import tablut.TablutBoardState;
import tablut.TablutMove;
import boardgame.Board;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class MonteCarloTreeSearch {
    static final double TURN_TIME = 1850;
    int opponent;
    int player;
    int level;
    private Random rand = new Random(1848);
    int count = 0;
    long start;
    long processMoveTime = 0;
    long statusTime = 0;
    long totalTime, totalExpand, totalPlay,totalMove, totalPromis, innerExpandStart, innerExpandEnd, totalInnerExpand,totalBack, totalTimeKing, kingEnd, kingStart, startTime,promisEnd, promisStart, expandEnd, expandStart, playEnd, playStart,backStart,backEnd,getMoveStart,getMoveEnd,childStart,childEnd,simulaStart,randomMoveStart,randomMoveEnd,moveSize;
    Tree tree;
    HashSet<String> previousPositions;
    public MonteCarloTreeSearch() {
    }


    public TablutMove findNextMove(TablutBoardState state){
        tree = new Tree(state);
        start = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        this.level++;
        this.player = state.getTurnPlayer();
        this.opponent = state.getOpponent();
        long end = (long)(TURN_TIME * 1000);

        Node rootNode = tree.getRoot();
        startTime = System.currentTimeMillis();
        ArrayList<Node> vistitedNodes = new ArrayList<>();
        int numTestLeft = Integer.MAX_VALUE;
        int numTestDone = 0;
        double averageSimultationTime = 0;
        if(player == TablutBoardState.SWEDE) {
            for (TablutMove move : state.getLegalMovesForPosition(state.getKingPosition())) {
                TablutBoardState cloneState = (TablutBoardState) state.clone();
                cloneState.processMove(move);
                if (cloneState.getWinner() == player) {
                    return move;
                }
            }
        }

        while((System.currentTimeMillis() - startTime) < TURN_TIME){
            expandEnd = 0;
            expandStart = 0;
            if(outOfTime())break;

            long simultionStartTime = System.currentTimeMillis();
            promisStart = System.currentTimeMillis();
            Node promisingNode = selectPromisingNode(rootNode);
            promisEnd = System.currentTimeMillis();
            if(outOfTime())break;

            if(promisingNode.getChildren().size() == 0){
                expandStart = System.currentTimeMillis();
                expandNode(promisingNode);
                expandEnd = System.currentTimeMillis();

            }

            Node nodeToExplore = promisingNode;
            playStart = System.currentTimeMillis();

            //System.out.println("Selected: " + (System.currentTimeMillis()-start));
            double playoutResult = simulateRandomPlayout(nodeToExplore);
            playEnd = System.currentTimeMillis();
            backStart = System.currentTimeMillis();
            if(outOfTime())break;
            //System.out.println("Simulated: " + (System.currentTimeMillis()-start));
            backPropogation(nodeToExplore, rootNode.getState().getTurnPlayer(), playoutResult);
            backEnd = System.currentTimeMillis();
            if(outOfTime())break;
            //System.out.println("Back: " + (System.currentTimeMillis()-start));
            numTestDone++;
            long simultationEndTime = System.currentTimeMillis();
            averageSimultationTime = ((numTestDone-1)*averageSimultationTime + (simultationEndTime-simultionStartTime))/(double)numTestDone;
            numTestLeft =(int)((TURN_TIME*1000-(System.currentTimeMillis()-start))/averageSimultationTime);

            vistitedNodes.add(nodeToExplore);
            totalBack += backEnd-backStart;
            totalPlay += playEnd-playStart;
            totalPromis += promisEnd - promisStart;
            totalTime += simultationEndTime - simultionStartTime;
            totalExpand += expandEnd-expandStart;
            try {
                Thread.sleep(0,50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println(numTestLeft + "Remaining: " + (System.currentTimeMillis()-start));
        }

        Node winnerNode = rootNode.getChildWithMaxScore();
        //System.out.println("UCT: " + UCT.uctValue(rootNode.getVisitCount(),winnerNode.getFitness(),winnerNode.getVisitCount()));
        int fit = 0;
        end = System.currentTimeMillis();
        //state.printBoard();
        //System.out.println("Total Time: " + totalTime + " innerExpandTime: " + (innerExpandEnd-innerExpandStart) + " ChildTime: " + (childEnd-childStart) + " innerExpandEnd: " + (startTime- innerExpandEnd) + " innerExpandStart: " + (startTime- innerExpandStart) + " totalPlay: " + totalPlay);
        return winnerNode.getMove();
    }

    private Node selectPromisingNode(Node rootNode){
        Node node = rootNode;

        while(node.getChildren().size() != 0){
            node = UCT.findBestNodeWithUCT(node, node.getState().getTurnPlayer() == player);
        }

        return node;
    }

    private void expandNode (Node node){
        getMoveStart = System.currentTimeMillis();
        ArrayList<TablutMove> possibleStates = node.getState().getAllLegalMoves();
        getMoveEnd = System.currentTimeMillis();
        childStart = System.currentTimeMillis();

        for(TablutMove move: possibleStates){
            if(outOfTime()){
                innerExpandStart = System.currentTimeMillis();
                System.out.println("HELP");
                break;
            }
            innerExpandStart = System.currentTimeMillis();

            TablutBoardState clonedState = (TablutBoardState) node.getState().clone();
            innerExpandEnd = System.currentTimeMillis();

            clonedState.processMove(move);

            Node newNode = new Node(clonedState);
                newNode.setMove(move);
                newNode.setParent(node);
                node.getChildren().add(newNode);
                kingStart = System.currentTimeMillis();
                /*if(newNode.getKingDistance() == Integer.MIN_VALUE && newNode.getState().getKingPosition() != null) {
                    int kingDistance1 = Coordinates.distanceToClosestCorner(newNode.getState().getKingPosition());
                    newNode.setKingDistance(kingDistance1);
                }else if(newNode.getState().getKingPosition() == null){
                    newNode.setKingDistance(Integer.MAX_VALUE);
                }*/
            Coord kingPos = newNode.getState().getKingPosition();
            if(outOfTime()){
                System.out.println(System.currentTimeMillis()-startTime);
                System.out.println("HELP1");
                break;
            }
            if(newNode.getKingDistance() == Integer.MIN_VALUE && kingPos != null) {
                    List<Coord> corners = Coordinates.getCorners();
                    int minDistance = Integer.MAX_VALUE;
                    for (Coord corner : corners) {
                        int distance = kingPos.distance(corner);
                        if (distance < minDistance) {
                            minDistance = distance;
                        }
                        if(outOfTime()){
                            innerExpandStart = System.currentTimeMillis();
                            System.out.println("HELP2");
                            break;
                        }
                    }
                    newNode.setKingDistance(minDistance);
                }else if(newNode.getState().getKingPosition() == null){
                    newNode.setKingDistance(Integer.MAX_VALUE);
                }
                kingEnd = System.currentTimeMillis();
                if(outOfTime())break;
                totalTimeKing += kingEnd - kingStart;
                totalInnerExpand += innerExpandEnd - innerExpandStart;


        }
        childEnd = System.currentTimeMillis();
        totalMove = getMoveEnd - getMoveStart;
    }

    private void backPropogation(Node nodeToExplore, int player, double fitness){
        count++;
        Node tempNode = nodeToExplore;
        while(tempNode != null){
            tempNode.incrementVisit();
            if(fitness != Integer.MIN_VALUE && fitness != Integer.MAX_VALUE ) {
                tempNode.incrementFitness(fitness, true);
                tempNode.incrementFitness(1 - fitness, false);
                tempNode = tempNode.getParent();
            }else if(fitness == Integer.MAX_VALUE){
                tempNode.incrementFitness(fitness, true);
                tempNode.incrementFitness(Integer.MIN_VALUE, false);
                tempNode = tempNode.getParent();
            }else{
                tempNode.incrementFitness(fitness, true);
                tempNode.incrementFitness(Integer.MAX_VALUE, false);
                tempNode = tempNode.getParent();
            }
        }
    }

    private double simulateRandomPlayout(Node node){

        Node tempNode = new Node((TablutBoardState) node.getState().clone());
        tempNode.setParent(node.getParent());
        int boardStatus = tempNode.getState().getWinner();
        int dept = 0;
        if(tempNode.getState().getKingPosition() != null && System.currentTimeMillis() -startTime < 1800) {
            if(tempNode.getParent() == tree.getRoot()) {
                Coord kingPosition = tempNode.getState().getKingPosition();
                for (TablutMove move : tempNode.getState().getLegalMovesForPosition(kingPosition)) {
                    if(outOfTime())break;
                    TablutBoardState cloneState = (TablutBoardState) tempNode.getState().clone();
                    cloneState.processMove(move);
                    if (cloneState.getWinner() == opponent) {
                        return Integer.MIN_VALUE;
                    } else if (cloneState.getWinner() == player) {
                        return Integer.MAX_VALUE;
                    }
                }
            }
        }else if(System.currentTimeMillis() -startTime < 1800){
            if(player == TablutBoardState.SWEDE) {
                return Integer.MIN_VALUE;
            }else{
                return Integer.MAX_VALUE;
            }
        }
        //System.out.println("Simulated: " + (System.currentTimeMillis()-start));
        while(boardStatus == Board.NOBODY && TURN_TIME*1000 > System.currentTimeMillis()-start ){
            TablutMove randomMove = null;
            randomMoveStart = System.currentTimeMillis();
            randomMove = (TablutMove) tempNode.getState().getRandomMove();
            randomMoveEnd = (System.currentTimeMillis());
            if (outOfTime())break;
            //System.out.println("Random: " + (System.currentTimeMillis()-start));
            //System.out.println("ValidMove : " + (System.currentTimeMillis()-start));
            tempNode.getState().processMove(randomMove);
            processMoveTime = (System.currentTimeMillis() - startTime);
            if(outOfTime())break;

            boardStatus = tempNode.getState().getWinner();
            statusTime = (System.currentTimeMillis()-startTime);

            dept++;

        }
        //System.out.println("SimulatedEnd: " + (System.currentTimeMillis()-start));
        if(dept == 1 && boardStatus == this.opponent){
            return Integer.MIN_VALUE;
        }else if(dept == 1 && boardStatus == this.player){
            return Integer.MAX_VALUE;
        }
        if(boardStatus == this.opponent){
            return 0;
        }else if(boardStatus == this.player){
            return 1;
        }else{
            return 0.5;
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

    private  boolean outOfTime(){
        return System.currentTimeMillis()-startTime > TURN_TIME;
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
