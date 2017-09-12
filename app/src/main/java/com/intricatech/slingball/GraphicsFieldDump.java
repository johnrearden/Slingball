package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 28/06/2015.
 * 
 * This class is simply a box to hold the values relevant to the drawing routines.
 */
public class GraphicsFieldDump {
    float ballXPos, ballYPos;
    int ballRadius;
    float projStartX, projStartY, projStopX, projStopY;
    float[] anglesOfTargets;
    TargetType[] typesOfTargets;
    TargetSize[] sizesOfTargets;
    SpriteType[] spriteTypesOfTargets;
    float[] opacitiesOfTargets;
    float[] shieldEnergies;
    float [][] decoyPositionsByOrbit;

    int gameScore;
    float timeRemaining;
    float energy;

    PositionList ballPositionList;
    
    GraphicsFieldDump (int numberOfOrbits) {
    	ballXPos = -100.0f;
    	ballYPos = 0;
    	ballRadius = 0;
        gameScore = 0;
        timeRemaining = 0;
        energy = 0;
        anglesOfTargets = new float[numberOfOrbits];
        typesOfTargets = new TargetType[numberOfOrbits];
        sizesOfTargets = new TargetSize[numberOfOrbits];
        spriteTypesOfTargets = new SpriteType[numberOfOrbits];
        opacitiesOfTargets = new float[numberOfOrbits];
        shieldEnergies = new float[numberOfOrbits];
        decoyPositionsByOrbit = new float[numberOfOrbits][IntRepConsts.DECOY_MAX_POPULATION];
        ballPositionList = new PositionList();
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("ballXPos : " + ballXPos + "\n");
    	sb.append("ballYPos : " + ballYPos + "\n");
    	sb.append("ballRadius : " + ballRadius + "\n");
    	return sb.toString();
    }
}
