package student_player;

import coordinates.Coordinates;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class UCT {
    static Random random = new Random();
    static double epsilon = 1e-6;
    final static double HEURISTIC_CONSTANT = 0;

    public static double uctValue(int totalVisit, double fitness, int nodeVisit, int playerTurn, int kingDistance){
        if(nodeVisit == 0){
            return Integer.MAX_VALUE;
        }
        double heuristic =  (10-kingDistance)/8.0*0;
        double mean = ((double) fitness/ (double) nodeVisit)+random.nextDouble()*epsilon;;
        return mean + 1.6  * Math.sqrt(Math.log(totalVisit)/ (double) nodeVisit) + random.nextDouble()*epsilon + heuristic * HEURISTIC_CONSTANT/((1-mean)*nodeVisit + 1);
    }

    public static Node findBestNodeWithUCT(Node node, final boolean isPlayer){
        final int parentVisit = node.getVisitCount();
        final int turn = node.getState().getTurnNumber();
        /*for(Node child : node.getChildren()){
            if(child.getKingDistance() == Integer.MIN_VALUE && child.getState().getKingPosition() != null) {
                int kingDistance1 = Coordinates.distanceToClosestCorner(child.getState().getKingPosition());
                child.setKingDistance(kingDistance1);
            }else if(child.getState().getKingPosition() == null){
                child.setKingDistance(Integer.MAX_VALUE);
            }
        }*/
        return Collections.max(node.getChildren(), new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double childFitness1 = isPlayer ? o1.getFitness()[0]: o1.getFitness()[1];
                double childFitness2 = isPlayer ? o2.getFitness()[0]: o2.getFitness()[1];

                double u1 = uctValue(parentVisit, childFitness1, o1.getVisitCount(),turn, o1.kingDistance);
                double u2 = uctValue(parentVisit, childFitness2, o2.getVisitCount(),turn, o2.kingDistance);
                return Double.compare(u1,u2);
            }
        });
    }

    public static double sigmoidFunction(int x){
        return 1.0/(1+Math.exp(-x));
    }

    /*public static Node findWorstNodeWithUCT(Node node){
        final int parentVisit = node.getVisitCount();
        return Collections.min(node.getChildren(), new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double u1 = uctValue(parentVisit, o1.getFitness(), o1.getVisitCount());
                double u2 = uctValue(parentVisit, o2.getFitness(), o2.getVisitCount());
                return Double.compare(u1,u2);
            }
        });
    }*/
}
