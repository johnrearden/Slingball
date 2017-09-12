package com.intricatech.slingball;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import static java.lang.Math.PI;

/**
 * Created by Bolgbolg on 24/06/2015.
 *
 * Class implements the Singleton design pattern.
 *
 * The key information contained in the map is coded in the blue component of the colour - this identifies
 * the targets orbit index.
 *
 * Supplement. The red factor is used to detect a collision with the thin end of the target.
 */
public class InternalCollisionMap {

    private Bitmap collisionMap;
    private Bitmap fixturesMap;
    private int diameter, xCenter, yCenter;
    private int effectiveDiameter;
    private int targetThicknessPlusPadding;
    private static final int outerCircleThickness = IntRepConsts.OUTER_CIRCLE_THICKNESS;
    private static final int outerCircleRadius = IntRepConsts.OUTER_CIRCLE_RADIUS;
    private static final int targetThickness = IntRepConsts.TARGET_THICKNESS;
    private static Paint.Cap CAP_TYPE = Paint.Cap.SQUARE;

    InternalCollisionMap() {
        createCollisionMap();
    }
    
    public Bitmap getCollisionMap() {
    	return collisionMap;
    }
    
    public Bitmap getFixturesMap() {
    	return fixturesMap;
    }

    private void createCollisionMap() {

        // Create a blank Bitmap of the correct size.
        diameter = IntRepConsts.DIAMETER;
        effectiveDiameter = diameter - (2 * outerCircleThickness);
        targetThicknessPlusPadding = targetThickness + IntRepConsts.GAP_BETWEEN_ORBITS;
        xCenter = IntRepConsts.X_CENTER_OF_ROTATION;
        yCenter = IntRepConsts.Y_CENTER_OF_ROTATION;
        collisionMap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        fixturesMap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        
        // get a canvas for the bitmap, and set the background color.
        Canvas c = new Canvas(collisionMap);
        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setARGB(255, 100, 100, IntRepConsts.BACKGROUND_BLUE_FACTOR);
        c.drawRect(0, 0, diameter, diameter, backgroundPaint);
        
        // Set the stroke screenWidth for the targets.
        Paint targetPaint = new Paint();
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeCap(CAP_TYPE);
        targetPaint.setStrokeWidth(IntRepConsts.TARGET_THICKNESS);

        // Iterate through the possible TargetSizes, and set the reference angle for each TargetSize.
        for (TargetSize size : TargetSize.values()){
        	double referenceAngle = getReferenceAngle(size);
            //System.out.println("referenceAngle = " + referenceAngle);

            // Iterate through the possible orbits, setting the blueFactor for each orbit
            // in turn, and draw an arc to represent the template target in each orbit.
            for (int i = 0; i < IntRepConsts.MAX_NUMBER_OF_ORBITS; i++){
                
            	// Set the blue parameter to represent the different possible orbits.
            	// The red parameter differentiates between the rounded end of the target and the target proper.
                // This is necessary to handle collisions between the ball and the edges of the targets, where we want the target to
                // reverse direction.
            	int blue = getBlueFactorCode(size, i);

                // First, draw the full shape of the target using the appropriate blue factor and
                // a red factor of 255.
                targetPaint.setARGB(255, 255, 0, blue);
                targetPaint.setStrokeCap(CAP_TYPE);
                float left = outerCircleThickness + (i * targetThicknessPlusPadding) + (targetThicknessPlusPadding / 2);
                float top = left;
                float right = diameter - (outerCircleThickness + (i * targetThicknessPlusPadding) + (targetThicknessPlusPadding / 2));
                float bottom = right;
                RectF boundingRect = new RectF(left, top, right, bottom);
                c.drawArc(boundingRect,
                          getStartAngle(size, referenceAngle),
                          getSweepAngle(size, size.getAngularSize()),
                          false,
                          targetPaint);

                // Next, draw the square edged section of the target using a red factor of zero. This leaves the rounded
                // ends of the targets as the only section that has a non-zero red factor.
                targetPaint.setARGB(255, 0, 0, blue);
                targetPaint.setStrokeCap(Paint.Cap.BUTT);

                c.drawArc(boundingRect,
                        getStartAngle(size, referenceAngle),
                        getSweepAngle(size, size.getAngularSize()),
                        false,
                        targetPaint);
            }
        }
        

        
        // get a context for the bufferedImage fixturesMap, and set the background color.
        Canvas f = new Canvas(fixturesMap);
        Paint fixturePaint = new Paint();
        fixturePaint.setStyle(Paint.Style.FILL);
        fixturePaint.setARGB(255, 0, 0, IntRepConsts.FIXTURES_BLUE_FACTOR);

    }
	public static double getReferenceAngle(TargetSize size) {
		double referenceAngle = -(PI / 2) + ((size.getIndex() * PI) / 2);
		return referenceAngle;
	}
    
    // Static method useful for whole app. Note difference in sign between drawArc's scheme for representing angles,
    // and that used by all of the Math trigonometry methods. These methods return the angles in degrees, suitable for use by
    // android.graphics.drawArc() and swing.graphics.drawArc().
    static float getStartAngle(TargetSize size, double referenceAngle) {
        double temp = referenceAngle - ((size.getAngularSize()) / 2);
        float startAngle = (float) Math.toDegrees(temp);
        return startAngle;			// drawArc treats positive as anticlockwise!!?? ( in java not in android )
    }
    
    // Static method useful for whole app.
    static float getSweepAngle(TargetSize size, double referenceAngle) {
        double temp = size.getAngularSize();
        float sweepAngle = (float) Math.toDegrees(temp);
        return sweepAngle;			// drawArc treats positive as anticlockwise!!??
    }
    
    // Static method to calculate the blueFactor coding for a given TargetSize and orbitIndex.
    static int getBlueFactorCode(TargetSize size, int orbitIndex){
    	return (10 * size.ordinal()) + orbitIndex;
    }

    // Static method to retrieve an orbitIndex from the blueFactor detected in a collision, and thus the identity of the
    // target collided with by the ball.
    static int decodeBlueFactorToOrbitIndex(int blueFactor) {
    	int temp = blueFactor % 10;
    	return temp;
    }
}
