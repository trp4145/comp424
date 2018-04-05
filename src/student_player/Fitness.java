package student_player;

import coordinates.Coord;
import coordinates.Coordinates;
import javafx.scene.control.Tab;
import tablut.TablutBoard;
import tablut.TablutBoardState;
import tablut.TablutMove;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Fitness {
    public static int countNumberOfEdgePieces(TablutBoardState state){
        int player_id = state.getTurnPlayer();
        int result = 0;
        HashSet<Coord> pieces = state.getPlayerPieceCoordinates();
        for(Coord piece: pieces){
            if(Coordinates.getNeighbors(piece).size() != 4){
                result++;
            }
        }
        return result;
    }

    public static int manhattenDistanceKing(TablutBoardState state){
        Coord kingPosition = state.getKingPosition();
        int closestCornerDistance = Coordinates.distanceToClosestCorner(kingPosition);
        return closestCornerDistance;
    }

    public static int potentialNextDistanceKing(TablutBoardState state){
        Coord kingPosition = state.getKingPosition();
        List<Coord> cornersList = Coordinates.getCorners();
        int shortestDistance = Integer.MAX_VALUE;
        ArrayList<TablutMove> legalMovesList = state.getLegalMovesForPosition(kingPosition);

        for(TablutMove move: legalMovesList) {
            TablutBoardState tempState = (TablutBoardState) state.clone();
            tempState.processMove(move);
            int distance = Coordinates.distanceToClosestCorner(tempState.getKingPosition());
            if(distance < shortestDistance){
                shortestDistance = distance;
            }
        }

        return shortestDistance;
    }

    public static int potentialSqueeze(TablutBoardState state){
        int player_id  = state.getTurnPlayer();
        HashSet<Coord> playerPieceList = state.getPlayerPieceCoordinates();
        HashSet<Coord> opponentPieceList = state.getOpponentPieceCoordinates();

        ArrayList<Coord[]> listOfPlayerPiecesNextToOpponent = new ArrayList<>();
        for(Coord playerPiece: playerPieceList){
            List<Coord> neighbors = Coordinates.getNeighbors(playerPiece);
            for(Coord neighbor: neighbors){
                if(opponentPieceList.contains(neighbor)){
                    Coord[] pair = new Coord[2];
                    pair[0] = playerPiece;
                    pair[1] = neighbor;
                    listOfPlayerPiecesNextToOpponent.add(pair);
                }
            }
        }

        for(Coord[] duo: listOfPlayerPiecesNextToOpponent){
            Coord playerPiece = duo[0];
            Coord opponentPiece = duo[1];

            Coord potentialSqueezeLocationList = null;
            try {
                potentialSqueezeLocationList = Coordinates.getSandwichCoord(playerPiece,opponentPiece);
            } catch (Coordinates.CoordinateDoesNotExistException e) {
                e.printStackTrace();
            }

        }
        return listOfPlayerPiecesNextToOpponent.size();
    }


}
