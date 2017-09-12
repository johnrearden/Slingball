package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 23/08/2017.
 */
public interface TargetCollisionPredictor {

    public boolean willBallHitTarget(float angle, int cyclesUntilArrival, float ballAngularWidth);

}
