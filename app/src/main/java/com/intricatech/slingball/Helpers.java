/*
 * This class contains static fields and methods that are read by the other classes.
 * They are grouped here for convenience
 */

package com.intricatech.slingball;

import android.graphics.Bitmap;

public class Helpers {

    private static final String TAG = "Helpers";
    private static double PI = Math.PI;
    private static double PIby2 = PI * 2;
	
	private Helpers() {}
	
	/*public static double resolveAngle(double angle) {
		if (angle > Math.PI){angle = angle - (Math.PI * 2); resolveAngle(angle);}
		if (angle <= -(Math.PI)){angle = angle + (Math.PI * 2); resolveAngle(angle);}
		
		return angle;
	}*/

    public static double resolveAngle(double angle) {
        if (angle > PI) {
            angle -= PIby2 * ((int) (angle / PIby2));
            if (angle > PI) {
                angle -= PIby2;
            }
        }
        if (angle <= -PI) {
            angle += PIby2 * ((int) (-angle / PIby2));
            if (angle < -PI) {
                angle += PIby2;
            }
        }

        return angle;
    }

	public static int getPixel4Byte(Object image, int xCoor, int yCoor){
		int argb = 0;
		try {
			argb = ((Bitmap)image).getPixel(xCoor, yCoor);
			return argb;
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} finally {
			return argb;
		}
	}

	/**
	 * Method takes a vararg array of floats and returns the smallest non-negative float, or -1 if
	 * the array is empty or all values are less than zero. If values are duplicated, the last
	 * value supplied in the vararg list will be returned.
	 *
	 * @param floats The array of floats to be evaluated.
	 * @return The smallest non-negative float from the array supplied.
	 */
	public static float minNonNegativeValue (float ...floats) {
		float currentLowestValue = Float.MAX_VALUE;
		for (int i = 0; i < floats.length; i++) {
			// A duplicate must replace its predecessor to avoid the edge case where the lowest
			// non-negative value is itself Float.MAX_VALUE.
			if (floats[i] >= 0 && floats[i] <= currentLowestValue) {
				currentLowestValue = floats[i];
			}
		}
		return currentLowestValue == Float.MAX_VALUE ? -1 : currentLowestValue;
	}
	
}
