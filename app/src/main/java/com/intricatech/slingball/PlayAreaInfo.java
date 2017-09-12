package com.intricatech.slingball;

import android.graphics.PointF;
import android.graphics.RectF;

/**
* Created by Bolgbolg on 04/12/2015.
*/
class PlayAreaInfo {

    float ratioOfActualToModel;

    int screenWidth, screenHeight;
    int topPanelHeight;
    float scaledBallRadius;
    float scaledBallShadowDiameter;
    float scaledDiameter;
    float scaledOuterCircleThickness;
    float scaledTargetThickness;
    float scaledGapBetweenOrbits;
    float effectiveDiameter;
    float drawArcOffset;
    float circleWidthRatio;

    float xCenterOfCircle;
    float yCenterOfCircle;

    RectF outermostTargetRect;

    public PointF getTargetCenterFromPolarCoors(
            int orbitIndex,
            float alpha) {
        float xPos, yPos;
        float radiusOfOutermostOrbit = ((outermostTargetRect.bottom - outermostTargetRect.top) / 2);
        float radialDist = radiusOfOutermostOrbit
                - ((scaledGapBetweenOrbits + scaledTargetThickness) * orbitIndex);

        xPos = xCenterOfCircle + (float) (radialDist * Math.cos(alpha));
        yPos = yCenterOfCircle + (float) (radialDist * Math.sin(alpha));

        return new PointF(xPos, yPos);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Play area info : \n");
        sb.append("scaled Diameter : " + scaledDiameter + "\n");
        sb.append("scaledOuterCircleThickness : " + scaledOuterCircleThickness + "\n");
        sb.append("scaledTargetThickness : " + scaledTargetThickness + "\n");
        sb.append("scaledGapBetweenOrbits : " + scaledGapBetweenOrbits + "\n");
        sb.append("effectiveDiameter : " + effectiveDiameter + "\n");
        sb.append("outermostTargetRect : " + outermostTargetRect.toString());
        return sb.toString();
    }
}
