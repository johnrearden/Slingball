package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 10/03/2017.
 */
public class TapToContinueMonitor {

    private boolean fingerLiftedOnce;

    public TapToContinueMonitor () {
        fingerLiftedOnce = false;
    }

    public boolean hasTapOccurred(boolean fingerDown) {
        if (!fingerDown) {
            fingerLiftedOnce = true;
            return false;
        }
        if (fingerDown && fingerLiftedOnce) {
            return true;
        } else {
            return false;
        }
    }
}
