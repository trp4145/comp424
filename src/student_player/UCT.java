package student_player;

import java.util.Collections;
import java.util.Comparator;

public class UCT {
    public static double uctValue(int totalVisit, double fitness, int nodeVisit){
        if(nodeVisit == 0){
            return Integer.MAX_VALUE;
        }
        return ((double) fitness/ (double) nodeVisit) + 1.41 * Math.sqrt(Math.log(totalVisit)/ (double) nodeVisit);
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
}
