package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 25/03/2016.
 */
public enum SpriteType {
    CRUISER(TargetType.CRUISER),
    DARTER(TargetType.DARTER),
    OSCILLATOR(TargetType.OSCILLATOR),
    TADPOLE(TargetType.TADPOLE),
    TADPOLE_HALOED(TargetType.TADPOLE),
    BLOCKER(TargetType.BLOCKER),
    DODGER_ON(TargetType.DODGER),
    DODGER_OFF(TargetType.DODGER),
    FLICKER_ON_1(TargetType.FLICKER),
    FLICKER_ON_2(TargetType.FLICKER),
    FLICKER_ON_3(TargetType.FLICKER),
    FLICKER_OFF(TargetType.FLICKER),
    DECOY_NORMAL(TargetType.DECOYS),
    DECOY_TRANSPARENT(TargetType.DECOYS),
    REWARDER(TargetType.REWARDER),
    KILLER_ON(TargetType.KILLER),
    KILLER_OFF(TargetType.KILLER);

    TargetType targetType;

    SpriteType(TargetType targetType) {
        this.targetType = targetType;
    }

    TargetType getTargetType() {
        return targetType;
    }
}
