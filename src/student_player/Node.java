package student_player;

import tablut.TablutBoardState;
import tablut.TablutMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Node {
    private TablutBoardState state;
    private Node parent;
    private ArrayList<Node> children;
    private TablutMove move;

    private int visitCount;
    private double[] fitness;
    int kingDistance;

    public Node(TablutBoardState state) {
        this.state = state;
        this.visitCount = 0;
        this.fitness = new double[2];
        this.children = new ArrayList<>();
        this.parent = null;
        this.move = null;
        kingDistance = Integer.MIN_VALUE;
    }

    public Node(TablutBoardState state, Node parent, ArrayList<Node> children, TablutMove move, int visitCount, double[] fitness) {
        this.state = state;
        this.parent = parent;
        this.children = children;
        this.move = move;
        this.visitCount = visitCount;
        this.fitness = fitness;
    }


    public void incrementVisit(){
        visitCount++;
    }

    public void incrementFitness(double fitness,boolean isRootPlayer){
        if(isRootPlayer) {
            this.fitness[0] += fitness;
        }else{
            this.fitness[1] += fitness;
        }
    }

    public TablutMove getMove() {
        return move;
    }

    public void setState(TablutBoardState state) {
        this.state = state;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public void setMove(TablutMove move) {
        this.move = move;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public Node getRandomChildNode() {
        int noOfPossibleMoves = this.children.size();
        int selectRandom = (int) (Math.random() * ((noOfPossibleMoves - 1) + 1));
        return this.children.get(selectRandom);
    }

    public int getKingDistance() {
        return kingDistance;
    }

    public void setKingDistance(int kingDistance) {
        this.kingDistance = kingDistance;
    }

    public Node getChildWithMaxScore() {
        ArrayList<Node> candidates = new ArrayList<>();
        double bestfitness = Integer.MIN_VALUE;
        for(Node child: children){
            if(child.getFitness()[0]> bestfitness){
                bestfitness = child.getFitness()[0];
                candidates = new ArrayList<>();
                candidates.add(child);
            }else if (child.getFitness()[0] == bestfitness){
                candidates.add(child);
            }
        }
        if(candidates.size() >= 1){
            return Collections.max(candidates, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {

                    return Double.compare(o1.visitCount,o2.visitCount);
                }
            });
        }else{
            return candidates.get(0);
        }


    }

    public double[] getFitness() {
        return fitness;
    }

    public void setFitness(double[] fitness) {
        this.fitness = fitness;
    }

    public TablutBoardState getState() {
        return state;
    }

    public Node getParent() {
        return parent;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
