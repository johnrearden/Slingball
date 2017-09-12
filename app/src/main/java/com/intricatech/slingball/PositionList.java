package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 24/02/2017.
 */
class PositionList {
    Position[] previousBallPositions;

    PositionList() {
        previousBallPositions = new Position[IntRepConsts.NUMBER_OF_PREVIOUS_POSITIONS];
        for (int i = 0; i < previousBallPositions.length; i++) {
            previousBallPositions[i] = new Position();
        }
    }

    void addCurrentPosiiton(float x, float y) {
        for (int i = previousBallPositions.length - 1; i > 0; i--) {
            previousBallPositions[i].assignValues(previousBallPositions[i - 1]);
        }
        previousBallPositions[0].assignValues(x, y);
    }

    void clearAll() {
        for (Position p : previousBallPositions) {
            p.occupied = false;
        }
    }


    void copyPositionListFrom(PositionList source) {
        for (int i = 0; i < previousBallPositions.length; i++) {
            previousBallPositions[i].assignValues(source.previousBallPositions[i]);
        }
    }

}
