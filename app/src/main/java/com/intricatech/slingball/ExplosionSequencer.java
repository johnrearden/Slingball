package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 11/03/2016.
 */
public class ExplosionSequencer {

    static final int[] sequenceOfSizes = {1, 0, 1, 2, 3};
    static final int[] timeForEachSizeToAppear = {2, 3, 2, 2, 2};
    int progressionCounter;
    int counter;

    float angle;                    // The angle the explosion is taking place at .... last position of the target.
    int orbit;                      // The orbit in which the explosion is taking place .... where the target was.
    TargetSize size;


    enum State {
        ACTIVE,
        WAITING
    }
    State state;

    ExplosionSequencer() {
        progressionCounter = 0;
        counter = 0;
        state = State.WAITING;
        angle = 0;
        orbit = 0;
        size = null;
    }

    void assignExplosionSequencer(float angle, int orbit, TargetSize size) {
        state = State.ACTIVE;
        counter = 0;
        progressionCounter = 0;
        this.angle = angle;
        this.orbit = orbit;
        this.size = size;
    }

    // Returns the current size of the explosion.... -1 if the sequence has completed.
    int updateExplosionSequencer() {
        if (counter++ >= timeForEachSizeToAppear[progressionCounter]) {
            counter = 0;
            progressionCounter++;
        }
        if (progressionCounter >= sequenceOfSizes.length) {
            progressionCounter = 0;
            state = State.WAITING;
            return -1;
        }
        return sequenceOfSizes[progressionCounter];
    }
}
