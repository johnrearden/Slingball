package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 29/06/2015.
 */
public enum TargetSize {/*TINY (0, 0.25f, 1),*/
                        SMALL (1, 0.35f, 3),
                        MEDIUM (2, 0.4f, 3),
                        LARGE (3, 0.45f, 3);

    private final int index;            // the index of the size, which decides the angular offset
                                        // of the template Target in the collisionMap.

    private final float angularSize;   // the size of the target in radians.

    private final int numberOfStripes;


    TargetSize(int index, float angularSize, int numberOfStripes) {
        this.index = index;
        this.angularSize = angularSize;
        this.numberOfStripes = numberOfStripes;
    }
    public int getIndex() {
        return index;
    }

    public float getAngularSize() {
        return angularSize;
    }

}


/**
 Original target sizes as per beta release 1.01 :
 TINY (0, 0.2f, 1),
 SMALL (1, 0.3f, 3),
 MEDIUM (2, 0.35f, 3),
 LARGE (3, 0.45f, 3)
 */