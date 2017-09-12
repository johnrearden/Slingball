package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 16/12/2015.
 *
 * The display manages the y-coordinate of a digitBitmap created and scaled by the calling object, GameActivity or
 * GameController. The display uses an internal coordinate scale from 0 to 260, and calls to update adjust
 * and return the coordinate of each digit to match the current target supplied as an argument. An initial value
 * of -1 corresponds to a blank display.
 *
 * All arrays order the digits from least significant to most significant, that is, right to left. The exception
 * to this is the return array from the update() method, which orders them in natural order for printing and
 * reading, that is, from left to right.
 * 
 * The bitmap that the coordinate refers to should be in the form of a vertical sequence of the following digits :
 * 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, [blank], 1. The coordinates returned by the update() method refer to the top 
 * of the glyph to be drawn.
 *
 */
public class ScrollingDigitDisplay {
    private int numberOfDigits;             // The total number of possible digits in the display.
    private int[] digitTargetValues;        // Array containing the target value for each digit.
    private int[] digitCurrentCoordinates;  // Array containing the current coordinated of each digit.
    private int numberToDisplay;            // The number that the digitDisplay should display.
    private int currentNumberDisplayed;     // The number that is currently displayed.
    private int coordinateIncrementPerCall; // The amount each coordinate can change by per call, out of 600, max 600.
    private IncrementRate incrementRate;

    private static int NUMBER_OF_GLYPHS = 13;
    private static int UNITS_PER_GLYPH = 30;
    private static int ZERO_HIGH_START = UNITS_PER_GLYPH * 10;
    private static int ONE_HIGH_START = UNITS_PER_GLYPH * 12;
    private static int BLANK = UNITS_PER_GLYPH * 11;

    /**
     * State-naming enum to facilitate transition from one digit to the next for all but the least significant
     * digit. Each digit, once it has started to increment, can then take care of its own incrementing without reference
     * to other digits. This makes the process simpler and reduces the potential for bugs.
     */
    public enum CurrentState {
        INCREMENTING,
        NOT_INCREMENTING
    }
    private CurrentState[] currentStates;

    /**
     * This enumerated messageType allows the client to specify an increment rate (per call) from a specified
     * range of values. This removes the need for parameter validation.
     */
    public enum IncrementRate {
        VERY_SLOW(1),
        SLOW(2),
        MEDIUM(3),
        FAST(5),
        VERY_FAST(10),
        LIGHTENING(15);

        int rate;
        IncrementRate(int rate) {
            this.rate = rate;
        }
        public int getRate() {
            return rate;
        }
    }

    public ScrollingDigitDisplay(int numberOfDigits, int initValue, IncrementRate rate) {
        this.numberOfDigits = numberOfDigits;
        this.numberToDisplay = initValue;
        this.coordinateIncrementPerCall = rate.getRate();
        this.incrementRate = rate;
        currentStates = new CurrentState[numberOfDigits];
        
        digitCurrentCoordinates = new int[numberOfDigits];
        digitTargetValues = new int[numberOfDigits];
        calculateDigitTargetValues(numberToDisplay);
        
        // Set the initial coordinates of the digits, and their current IncrementState. This is done once only
        int highestIndexOfUsedDigits = (int) Math.log10(numberToDisplay);
        for (int i = 0; i < numberOfDigits; i++) {
        	currentStates[i] = CurrentState.NOT_INCREMENTING;
        	digitCurrentCoordinates[i] = digitTargetValues[i] * UNITS_PER_GLYPH;
        	if (i > highestIndexOfUsedDigits) digitCurrentCoordinates[i] = BLANK;
        }
        // If the number to be displayed is zero, set accordingly
        if (numberToDisplay == 0) {
            digitCurrentCoordinates[0] = 0;
        }
        currentNumberDisplayed = calculateCurrentNumberDisplayed();
    }

    public int[] setScoreWithoutScrolling (int score) {
        currentNumberDisplayed = score;
        numberToDisplay = score;
        for (int i = 0; i < numberOfDigits; i++) {
            currentStates[i] = CurrentState.NOT_INCREMENTING;
        }

        digitTargetValues = new int[numberOfDigits];
        calculateDigitTargetValues(numberToDisplay);

        // Calculate the y-value for each digit in the score.
        int highestIndexOfUsedDigits = (int) Math.log10(numberToDisplay);
        for (int i = 0; i < numberOfDigits; i++) {
            currentStates[i] = CurrentState.NOT_INCREMENTING;
            digitCurrentCoordinates[i] = digitTargetValues[i] * UNITS_PER_GLYPH;
            if (i > highestIndexOfUsedDigits) digitCurrentCoordinates[i] = BLANK;
        }
        // If the number to be displayed is zero, set accordingly
        if (numberToDisplay == 0) {
            digitCurrentCoordinates[0] = 0;
        }

        return digitCurrentCoordinates;
    }
    public int[] update(int targetNumber) {
        // First check to see if the number currently displayed needs to increase.
        currentNumberDisplayed = calculateCurrentNumberDisplayed();
        calculateDigitTargetValues(targetNumber);
        if (targetNumber > currentNumberDisplayed) {
            // Slow the increment rate as the targetNumber is approached.
            if (targetNumber - currentNumberDisplayed < 0.5f) {
                coordinateIncrementPerCall = IncrementRate.VERY_SLOW.getRate();
            } else if (targetNumber - currentNumberDisplayed < 2.0f) {
                coordinateIncrementPerCall = IncrementRate.MEDIUM.getRate();
            } else {
                coordinateIncrementPerCall = incrementRate.getRate();
            }
        	// Check if any digit has reached ZERO_HIGH or ONE_HIGH and reduce them if necessary
        	for (int i = 0; i < numberOfDigits; i++) {
        		if (digitCurrentCoordinates[i] >= ZERO_HIGH_START && 
        				digitCurrentCoordinates[i] < BLANK) {
        			digitCurrentCoordinates[i] -= 10 * UNITS_PER_GLYPH;
        		}
        		if (digitCurrentCoordinates[i] >= ONE_HIGH_START) {
        			digitCurrentCoordinates[i] -= 11 * UNITS_PER_GLYPH;
        		}
        	}
        	
        	// The least significant digit, at a minimum, must increment if the targetNumber
        	// has not been reached.
        	currentStates[0] = CurrentState.INCREMENTING;
        	digitCurrentCoordinates[0] += coordinateIncrementPerCall;
            if (digitCurrentCoordinates[0] > digitTargetValues[0] * UNITS_PER_GLYPH) {
                digitCurrentCoordinates[0] = digitTargetValues[0] * UNITS_PER_GLYPH;
            }
        	
        	// Check if each digit in turn should begin to increment. A digit only begins to increment
        	// when the digit to the right is changing from 9 to 0.
        	for (int i = 1; i < numberOfDigits; i++) {
        		if (currentStates[i - 1] == CurrentState.INCREMENTING &&
        				digitCurrentCoordinates[i - 1] > (9 * UNITS_PER_GLYPH) &&
        				digitCurrentCoordinates[i - 1] <= 10 * UNITS_PER_GLYPH) {
        			currentStates[i] = CurrentState.INCREMENTING;
        			digitCurrentCoordinates[i] += coordinateIncrementPerCall;
                    // Check for overrun.
                    if (digitCurrentCoordinates[i] > digitTargetValues[i] * UNITS_PER_GLYPH) {
                        digitCurrentCoordinates[i] = digitTargetValues[i] * UNITS_PER_GLYPH;
                    }
        		}
        	}
        	
        	
        } else {
        	// Set all digits to be NOT_INCREMENTING
        	for (int i = 0; i < numberOfDigits; i++) {
        		currentStates[i] = CurrentState.NOT_INCREMENTING;
        	}
            //correctExactPlacement();
        	currentNumberDisplayed = calculateCurrentNumberDisplayed();
        	return digitCurrentCoordinates;
        }
        currentNumberDisplayed = calculateCurrentNumberDisplayed();
        
        // reset all digits to be notincrementing.
        for(int i = 0; i < numberOfDigits; i++) {
        	currentStates[i] = CurrentState.NOT_INCREMENTING;
        }
        return digitCurrentCoordinates;
    }

    /**
     * HACK !!!
     * Method will check that digits that are not incrementing are set to the correct glyph. This
     * is necessary because the deceleration as the numberDisplayed closes on the targetNumber
     * has introduced a bug.
     */
    private void correctExactPlacement() {
        for (int i = 0; i < numberOfDigits; i++) {
            if (digitCurrentCoordinates[i] % UNITS_PER_GLYPH != 0) {
                digitCurrentCoordinates[i] -= digitCurrentCoordinates[i] % 30;
            }
        }
    }
    
    public int getTotalUnits() {
    	return NUMBER_OF_GLYPHS * UNITS_PER_GLYPH;
    }
    
    /**
     * Calculates the actual number that a set of digit coordinates represents. If a digit being
     * displayed is between 2 integer values then the lower of the 2 is taken as the value.
     *
     * @return the number currently displayed.
     */
    private int calculateCurrentNumberDisplayed() {
        int cumulativeTotal = 0;
        int currentDigit;
        int amountToAdd;
        for (int i = 0; i < numberOfDigits; i++) {
            if (digitCurrentCoordinates[i] >= ZERO_HIGH_START && digitCurrentCoordinates[i] < ONE_HIGH_START) {
                currentDigit = 0;
            } else if (digitCurrentCoordinates[i] >= ONE_HIGH_START) {
                currentDigit = 1;
            } else {
                currentDigit = (int)(digitCurrentCoordinates[i] / UNITS_PER_GLYPH);
            }
            amountToAdd = (int)(currentDigit * Math.pow(10, i));
            cumulativeTotal += amountToAdd;
        }
        return cumulativeTotal;
    }

    /**
     * Method calculates the coordinate of each digit in order to represent a given integer.
     *
     * @return an array containing the coordinates for each digit.
     */
    private int[] getCoordinatesFromNumber(int number) {
        // First check if the number is blank.
        int[] returnArray = new int[numberOfDigits];
        if (number == -1) {
            for (int i = 0; i < numberOfDigits; i++) {
                returnArray[i] = BLANK;
            }
            return returnArray;
        }
        return null;
    }

    private void calculateDigitTargetValues(int number) {
        digitTargetValues[0] = number % 10;
    	for (int i = 0; i < numberOfDigits; i++) {
            digitTargetValues[i] = number % 10;
            number = number / 10;
        }
    } 
    
    public int getCurrentNumberDisplayed() {
    	return currentNumberDisplayed;
    }
}