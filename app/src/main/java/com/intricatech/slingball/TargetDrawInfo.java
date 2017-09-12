package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 15/12/2015.
 */
public class TargetDrawInfo {
    float alpha;
    float opacity;
    float shieldHealth;
    TargetType type;
    SpriteType spriteType;
    TargetSize size;
    float[] decoyPositions;


    public TargetDrawInfo () {
        alpha = 0;
        opacity = 0;
        type = null;
        size = null;
        decoyPositions = new float[IntRepConsts.DECOY_MAX_POPULATION];
    }
}
