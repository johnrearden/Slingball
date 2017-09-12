package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 04/12/2015.
 */
public enum TargetType {
    BLOCKER(TargetColor.YELLOW),
    DARTER(TargetColor.PURPLE),
    CRUISER(TargetColor.GREEN),
    OSCILLATOR(TargetColor.RED),
    TADPOLE(TargetColor.SILVER),
    DODGER(TargetColor.OFF_WHITE),
    FLICKER(TargetColor.OFF_WHITE),
    DECOYS(TargetColor.DECOY_RED),
    KILLER(TargetColor.OFF_WHITE),
    REWARDER(TargetColor.SILVER);

    public TargetColor color;

    TargetType(TargetColor color) {
        this.color = color;
    }

    public TargetColor getTargetColor() {
        return color;
    }
}
