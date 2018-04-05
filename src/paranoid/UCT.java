package paranoid;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class UCT {
    static Random random = new Random();
    static double epsilon = 1e-6;


    public static double uctValue(int totalVisit, double fitness, int nodeVisit){
        if(nodeVisit == 0){
            return Integer.MAX_VALUE;
        }
        return ((double) fitness/ (double) nodeVisit) + 1 * Math.sqrt(Math.log(totalVisit)/ (double) nodeVisit) + random.nextDouble()*epsilon;
    }

    public static double uctValueEnemy(int totalVisit, double fitness, int nodeVisit){
        if(nodeVisit == 0){
            return Integer.MAX_VALUE;
        }
        return ((1-(double) fitness/ (double) nodeVisit)) + 1.41 * Math.sqrt(Math.log(totalVisit)/ (double) nodeVisit) + random.nextDouble()*epsilon;
    }

    public static Node findBestNodeWithUCT(Node node){
        final int parentVisit = node.getVisitCount();
        return Collections.max(node.getChildren(), new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double u1 = uctValue(parentVisit, o1.getFitness(), o1.getVisitCount());
                double u2 = uctValue(parentVisit, o2.getFitness(), o2.getVisitCount());
                return Double.compare(u1,u2);
            }
        });
    }

    public static Node findEnemyNodeWithUCT(Node node){
        final int parentVisit = node.getVisitCount();
        return Collections.min(node.getChildren(), new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double u1 = uctValueEnemy(parentVisit, o1.getFitness(), o1.getVisitCount());
                double u2 = uctValueEnemy(parentVisit, o2.getFitness(), o2.getVisitCount());
                return Double.compare(u1,u2);
            }
        });
    }
}
