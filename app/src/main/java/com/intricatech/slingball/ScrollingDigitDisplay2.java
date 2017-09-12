package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 15/02/2017.
 * <p/>
 * This class represents a number display made up of independently moving spinners with decimal digits
 * arranged on them. The class does not handle the graphical representation of these, but instead
 * stores the coordinate position used by a client to draw the digit.
 * <p/>
 * The bitmap that the coordinate refers to should be in the form of a vertical sequence of the following digits :
 * 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, [blank], 1. The coordinates returned by the update() method refer to the top
 * of the glyph to be drawn.
 * <p/>
 * The class contains an update method which should be called on each redrawing of the client graphic.
 * The refresh rate is assumed to be 60fps
 */
public class ScrollingDigitDisplay2 {

    String TAG;

    static final int NUMBER_OF_GLYPHS = 13;
    static final int UNITS_PER_GLYPH = 100;
    private static int ZERO_HIGH_START = UNITS_PER_GLYPH * 10;
    private static int ONE_HIGH_START = UNITS_PER_GLYPH * 12;
    private static int BLANK = UNITS_PER_GLYPH * 11;

    private final int numberOfDigits;
    private float currentNumberDisplayed;
    private IncrementRate incrementRate;
    private int mostSignificantDigit;

    private int[] returnValues;

    private DigitNode baseNode;

    private class DigitNode {

        int position;  // counting from the right, i.e. least significant digit.
        DigitNode nextNode; // the node representing the next most significant digit.
        int targetDigit;
        int currentCoordinate;
        int currentDigitDisplayed;
        Behaviour behaviour;

        boolean caughtAndMirroring;
        int fractionalPartMirrored;
        int nextDigit;

        DigitNode(int position) {
            this.position = position;
            behaviour = Behaviour.STOPPED;
        }

        void lessSigDigPassedNine() {
            caughtAndMirroring = true;
            nextDigit = currentDigitDisplayed + 1; // for 9, this will give a value of 10, corresponding to zero-high on the graphic.
        }

        void lessSigPassedZero() {
            caughtAndMirroring = false;
            currentCoordinate = nextDigit * UNITS_PER_GLYPH;
        }

        void calculateCurrentDigitDisplayed() {
            if (currentCoordinate >= BLANK) {
                currentDigitDisplayed = 0;
            } else {
                currentDigitDisplayed = currentCoordinate / UNITS_PER_GLYPH;
            }

        }

        void setFractionalPartMirrored(int i) {
            this.fractionalPartMirrored = i;
        }

    }

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
     * Represents the current behaviour of a digitNode.
     */
    enum Behaviour {
        INCREMENTING, DECREMENTING, STOPPED
    }

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
        LIGHTENING(60);

        int rate;

        IncrementRate(int rate) {
            this.rate = rate;
        }

        public int getRate() {
            return rate;
        }
    }

    public ScrollingDigitDisplay2(int numberOfDigits, int initValue) {

        TAG = getClass().getSimpleName();

        this.numberOfDigits = numberOfDigits;
        mostSignificantDigit = calculateMostSignificantDigit(initValue);

        // Create the linked list.
        baseNode = new DigitNode(0);
        int counter = 1;
        DigitNode currentNode = baseNode;
        while (counter < numberOfDigits) {
            currentNode.nextNode = new DigitNode(counter);
            currentNode = currentNode.nextNode;
            counter++;
        }

        initializeDigitList(initValue);

        calculateTargetCoordinates(initValue);

        returnValues = new int[numberOfDigits];

    }

    public int[] update(int targetNumber) {

        evaluateCurrentNumberDisplayed();
        setRateOfChange(currentNumberDisplayed, targetNumber);

        int delta;

        // First process the least significant digit.
        DigitNode currentNode = baseNode;
        if (currentNumberDisplayed > targetNumber) {
            /*baseNode.behaviour = Behaviour.DECREMENTING;
            delta = -incrementRate.getRate();*/
            delta = 0;
        } else if (currentNumberDisplayed < targetNumber) {
            baseNode.behaviour = Behaviour.INCREMENTING;
            delta = incrementRate.getRate();

        } else {
            baseNode.behaviour = Behaviour.STOPPED;
            delta = 0;
        }
        baseNode.currentCoordinate += delta;

        // Iterate through the list, informing each digit in turn of its neighbours position. We only
        // need to move as far as the second last digit.
        while (currentNode != null && currentNode.nextNode != null) {

            if (currentNode.caughtAndMirroring) {
                currentNode.currentCoordinate = currentNode.currentDigitDisplayed * UNITS_PER_GLYPH
                        + currentNode.fractionalPartMirrored;
            }

            if (currentNode.currentCoordinate > 9 * UNITS_PER_GLYPH
                    && currentNode.currentCoordinate < 10 * UNITS_PER_GLYPH
                    && currentNode.nextNode.caughtAndMirroring == false) {
                currentNode.nextNode.lessSigDigPassedNine();
            } else if (currentNode.currentCoordinate >= 10 * UNITS_PER_GLYPH
                    && currentNode.currentCoordinate < 11 * UNITS_PER_GLYPH
                    && currentNode.nextNode.caughtAndMirroring == true) {
                currentNode.nextNode.lessSigPassedZero();

            }

            if (currentNode.nextNode.caughtAndMirroring) {
                currentNode.nextNode.setFractionalPartMirrored(currentNode.currentCoordinate - 9 * UNITS_PER_GLYPH);
            }
            currentNode = currentNode.nextNode;
        }

        // Iterate through the list, performing 3 operations.
        currentNode = baseNode;
        while (currentNode != null) {

            // 1 : if the current coordinate has exceeded the wrap-around values, reduce it.

            if (currentNode.currentCoordinate >= ZERO_HIGH_START
                    && currentNode.currentCoordinate < BLANK) {
                currentNode.currentCoordinate -= ZERO_HIGH_START;
            }
            if (currentNode.currentCoordinate >= ONE_HIGH_START) {
                currentNode.currentCoordinate -= ONE_HIGH_START;
            }

            // 2 : calculate the current digit displayed (integer)
            currentNode.calculateCurrentDigitDisplayed();

            // 3 : fill the relevant entry in the return array.
            returnValues[currentNode.position] = currentNode.currentCoordinate;
            currentNode = currentNode.nextNode;
        }

        return returnValues;
    }

    public int[] setScoreWithoutScrolling(int score) {
        int[] returnArray = new int[numberOfDigits];
        mostSignificantDigit = calculateMostSignificantDigit(score);
        DigitNode curNode = baseNode;

        while (curNode != null) {
            if (curNode.position > mostSignificantDigit) {
                curNode.currentCoordinate = BLANK;
            } else {
                int i = isolateDigitValueFromNumber(score, curNode.position);
                curNode.currentCoordinate = i * UNITS_PER_GLYPH;
            }
            returnArray[curNode.position] = curNode.currentCoordinate;
            curNode = curNode.nextNode;
        }
        evaluateCurrentNumberDisplayed();

        return returnArray;
    }

    private void evaluateCurrentNumberDisplayed() {
        DigitNode currentNode;
        float result = (float) baseNode.currentCoordinate / UNITS_PER_GLYPH;
        currentNode = baseNode.nextNode;
        while (currentNode != null) {
            result += currentNode.currentDigitDisplayed * Math.pow(10, currentNode.position);
            currentNode = currentNode.nextNode;
        }
        currentNumberDisplayed = result;
    }

    private void calculateTargetCoordinates(int value) {
        DigitNode currentNode = baseNode;
        while (currentNode != null) {
            currentNode.targetDigit = isolateDigitValueFromNumber(value, currentNode.position);
            currentNode = currentNode.nextNode;
        }
    }

    private void initializeDigitList(int initialValue) {
        DigitNode currentNode = baseNode;
        while (currentNode != null) {
            currentNode.currentDigitDisplayed = isolateDigitValueFromNumber(initialValue, currentNode.position);
            if (currentNode.position > mostSignificantDigit) {
                currentNode.currentCoordinate = BLANK;
            } else {
                currentNode.currentCoordinate = currentNode.currentDigitDisplayed * UNITS_PER_GLYPH;
            }
            currentNode = currentNode.nextNode;
        }
    }

    /**
     * Method isolates one digit from a number, and returns that digit. Relies on integer division,
     * so refactoring should not change the primitive types of the parameters.
     *
     * @param number
     * @param digitPosition as represented by digitNode, i.e. zero-based.
     * @return
     */
    private int isolateDigitValueFromNumber(int number, int digitPosition) {
        int result = (int) (number / Math.pow(10, digitPosition)); // remove digits to right.
        result = result % 10; // remove digits to left.

        return result;
    }

    private void setRateOfChange(float currentNumberDisplayed, int targetNumber) {
        float difference = Math.abs(targetNumber - currentNumberDisplayed);
        if (difference < 0.1f) {
            incrementRate = IncrementRate.VERY_SLOW;
        } else if (difference < 0.2f) {
            incrementRate = IncrementRate.SLOW;
        } else if (difference < 0.5f) {
            incrementRate = IncrementRate.MEDIUM;
        } else if (difference < 1.0f) {
            incrementRate = IncrementRate.FAST;
        } else if (difference < 2.0f) {
            incrementRate = IncrementRate.VERY_FAST;
        } else {
            incrementRate = IncrementRate.LIGHTENING;
        }
    }

    private int calculateMostSignificantDigit(int i) {
        int j;
        if (i == 0) {
            j = 0;
        } else {
            j = (int) Math.log10(i);
        }
        return j;
    }

    public float getCurrentNumberDisplayed() {
        return currentNumberDisplayed;
    }

    public int getTotalUnits() {
        return NUMBER_OF_GLYPHS * UNITS_PER_GLYPH;
    }
}