package student_player;

import tablut.TablutBoardState;

public class Tree {
    Node root;

    public Tree(TablutBoardState state){
        this.root = new Node(state);

    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
