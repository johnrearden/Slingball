package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 24/02/2017.
 */
class Position {
    float xPos, yPos;
    boolean occupied;

    Position() {
        xPos = -100;
        yPos = 0;
        occupied = false;
    }

    Position(float x, float y) {
        this.xPos = x;
        this.yPos = y;
        occupied = true;
    }

    void assignValues(Position p) {
        this.xPos = p.xPos;
        this.yPos = p.yPos;
        occupied = p.occupied;
    }

    void assignValues(float x, float y) {
        this.xPos = x;
        this.yPos = y;
        occupied = true;
    }
}
