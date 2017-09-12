package com.intricatech.slingball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.text.DecimalFormat;

/**
 * Created by Bolgbolg on 19/01/2016.
 */
public class ScoreDisplayer {

    private final String TAG;

    ScrollingDigitDisplay2 sdd;
    PlayAreaInfo playAreaInfo;
    int[] glyphYCoordinates;
    int[] scaledGlyphYCoordinates;

    Bitmap source;
    Bitmap digitImages;
    Bitmap[] displayedGlyphs;
    Bitmap highScoreBitmap;
    Bitmap highScoreDigitImages;
    Bitmap starOnBitmap, starOffBitmap;
    Bitmap star5On, star5Off;
    Canvas[] displayedGlyphCanvases;

    Rect sourceRect;
    RectF targetRect;

    Paint readoutBoxPaint;
    int outerCircleColor;

    static final int NUMBER_OF_DIGITS = IntRepConsts.SCORE_NUMBER_OF_DIGITS;
    static final int NUMBER_OF_GLYPHS_IN_BASE_IMAGE = 13;
    static int UNIT_RANGE_OF_SDD;

    private float glyphWidth;
    private float glyphHeight;
    private float xPos;
    private float yPos;
    private float xSize;
    private float ySize;
    private float highScoreXPos, highScoreYPos;
    private float highScoreXSize, highScoreYSize;
    private float heartYPos, starYPos;
    private float highScoreRelativeSize;
    private float blurThickness;

    // fields for stars and hearts displays
    static final float HEART_REL_XPOS = 0.15f;
    static final float STAR_REL_XPOS = 0.85f;
    private Bitmap scaledHeartIcon, scaledStarIcon;
    private Bitmap heartsNumberBitmap, starsNumberBitmap;
    private Bitmap heartTempBitMap, starTempBitmap;
    private float heartXPos, heartAndStarYPos, starXPos;
    private float heartAndStarNumberHeight;
    private float heartAndStarHeight;
    private float scoreXCenter;
    private Rect heartNumberBounds, starNumberBounds;
    private Paint heartNumberPaint, starNumberPaint;
    Canvas utilityCanvas;
    static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.0");
    int heartsCurrentlyShown;
    float starsCurrentlyShown;

    boolean redrawOnBB1, redrawOnBB2;

    private float divider;
    private int heightOfScaledDigitImages;
    private int heightOfScaledHighScoreDigitImages;

    int highScore;

    Paint blackPaint, blankTransparentPaint;

    public ScoreDisplayer (Bitmap digitSource, Resources resources, int highScore) {

        TAG = getClass().getSimpleName();

        this.source = digitSource;
        this.highScore = highScore;
        outerCircleColor = resources.getColor(R.color.outer_circle);

        sdd = new ScrollingDigitDisplay2(NUMBER_OF_DIGITS, 100);
        UNIT_RANGE_OF_SDD = sdd.getTotalUnits();
        displayedGlyphs = new Bitmap[NUMBER_OF_DIGITS];
        displayedGlyphCanvases = new Canvas[NUMBER_OF_DIGITS];
        glyphYCoordinates = new int[NUMBER_OF_DIGITS];
        scaledGlyphYCoordinates = new int[NUMBER_OF_DIGITS];
        glyphYCoordinates = new int[NUMBER_OF_DIGITS];
        scaledGlyphYCoordinates = new int[NUMBER_OF_DIGITS];
        calculateScaledGlyphYCoors();
        sourceRect = new Rect();
        targetRect = new RectF();
        highScoreRelativeSize = IntRepConsts.HIGHSCORE_RELATIVE_SIZE;

        readoutBoxPaint = new Paint();
        readoutBoxPaint.setColor(outerCircleColor);
        /*readoutBoxPaint.setColor(Color.BLACK);*/
        readoutBoxPaint.setStyle(Paint.Style.FILL);

        scaledStarIcon = BitmapFactory.decodeResource(resources, R.drawable.bonus_star_on);
        scaledHeartIcon = BitmapFactory.decodeResource(resources, R.drawable.heart_icon_ec0000);

        heartNumberPaint = new Paint();
        heartNumberPaint.setARGB(255, 235, 0, 0);
        heartNumberPaint.setTextSize(40);
        heartNumberPaint.setFakeBoldText(true);
        heartNumberPaint.setAntiAlias(true);

        starNumberPaint = new Paint();
        starNumberPaint.setARGB(255, 255, 213, 0);
        starNumberPaint.setTextSize(40);
        starNumberPaint.setFakeBoldText(true);
        starNumberPaint.setAntiAlias(true);

        heartNumberBounds = new Rect();
        starNumberBounds = new Rect();

        heartsCurrentlyShown = -1; // -1 ensures they are drawn the first time.
        starsCurrentlyShown = -1;

        blackPaint = new Paint();
        blackPaint.setARGB(255, 0, 0, 0);
        blackPaint.setStyle(Paint.Style.FILL);

        blankTransparentPaint = new Paint();
        blankTransparentPaint.setColor(Color.TRANSPARENT);
        blankTransparentPaint.setStyle(Paint.Style.FILL);
        blankTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

    }

    public void setScoreWithoutScrolling(int score) {
        glyphYCoordinates = sdd.setScoreWithoutScrolling(score);
    }
    public Bitmap[] update(int score) {
        glyphYCoordinates = sdd.update(score);
        calculateScaledGlyphYCoors();
        for (int i = 0; i < NUMBER_OF_DIGITS; i++) {
            drawGlyph(i, scaledGlyphYCoordinates[i]);
        }
        return displayedGlyphs;
    }

    public void drawGlyph(int index, int yCoordinate) {
        sourceRect.set(0, (int)yCoordinate, (int)glyphWidth, (int)(yCoordinate + glyphHeight));
        targetRect.set(0, 0, (int) glyphWidth, (int) glyphHeight);
        displayedGlyphCanvases[index].drawBitmap(digitImages, sourceRect, targetRect, null);
    }

    public void onSurfaceChanged(PlayAreaInfo playAreaInfo) {

        this.playAreaInfo = playAreaInfo;

        // Scale the positions.
        divider = IntRepConsts.SCORE_DIVIDER_THICKNESS * playAreaInfo.screenWidth;
        xPos = IntRepConsts.SCORE_XPOS * playAreaInfo.screenWidth;
        yPos = IntRepConsts.SCORE_YPOS * playAreaInfo.topPanelHeight;
        xSize = IntRepConsts.SCORE_XSIZE * playAreaInfo.screenWidth;
        ySize = IntRepConsts.SCORE_YSIZE * playAreaInfo.topPanelHeight;
        glyphWidth = (xSize - (NUMBER_OF_DIGITS - 1) * divider) / NUMBER_OF_DIGITS;
        glyphHeight = ySize;

        // scale digitImages appropriately.
        float newWidth = xSize / NUMBER_OF_DIGITS;
        float newHeight = ySize * NUMBER_OF_GLYPHS_IN_BASE_IMAGE;
        Bitmap temp = Bitmap.createScaledBitmap(source, (int) newWidth, (int) newHeight, false);
        digitImages = Bitmap.createBitmap((int) newWidth, (int) newHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(digitImages);
        c.drawARGB(255, 0,0,0);
        c.drawBitmap(temp, 0, 0, null);
        heightOfScaledDigitImages = (int)newHeight;

        // Do the same for the highScore images.
        /*highScoreXPos = IntRepConsts.HIGHSCORE_XPOS * playAreaInfo.screenWidth;*/
        highScoreYPos = IntRepConsts.HIGHSCORE_YPOS * playAreaInfo.topPanelHeight;
        highScoreXSize = xSize * IntRepConsts.HIGHSCORE_RELATIVE_SIZE;
        highScoreXPos = xPos + xSize - highScoreXSize;
        highScoreYSize = ySize * IntRepConsts.HIGHSCORE_RELATIVE_SIZE;
        newWidth = highScoreXSize / NUMBER_OF_DIGITS;
        newHeight = highScoreYSize * NUMBER_OF_GLYPHS_IN_BASE_IMAGE;
        temp = Bitmap.createScaledBitmap(source, (int) newWidth, (int) newHeight, false);
        highScoreDigitImages = Bitmap.createBitmap((int) newWidth, (int) newHeight, Bitmap.Config.ARGB_8888);
        c = new Canvas(highScoreDigitImages);
        c.drawARGB(255, 0,0,0);
        c.drawBitmap(temp, 0, 0, null);
        heightOfScaledHighScoreDigitImages = (int) newHeight;

        // create the displayedGlyphs bitmaps and canvases.
        for (int i = 0; i < NUMBER_OF_DIGITS; i++) {
            displayedGlyphs[i] = Bitmap.createBitmap((int) glyphWidth, (int) glyphHeight, Bitmap.Config.ARGB_8888);
            displayedGlyphCanvases[i] = new Canvas(displayedGlyphs[i]);
        }

        // create the highScore bitmap.
        highScoreBitmap = Bitmap.createBitmap(
                (int) highScoreXSize,
                (int) highScoreYSize,
                Bitmap.Config.ARGB_8888);

        drawHighScoreOnBitmap();

        // scale the star bitmaps.
        scoreXCenter = xPos + (xSize * 0.5f);
        heartAndStarHeight = IntRepConsts.HEART_STAR_HEIGHT_RATIO * ySize;

        heartAndStarNumberHeight = heartAndStarHeight * IntRepConsts.HEART_AND_STAR_NUMBER_REL_SIZE;
        heartAndStarYPos = IntRepConsts.HEART_AND_STAR_DISPLAY_YPOS * playAreaInfo.topPanelHeight;

        starYPos = heartAndStarYPos;
        heartYPos = heartAndStarYPos + heartAndStarHeight + (heartAndStarHeight * IntRepConsts.HEART_AND_STAR_REL_Y_GAP);
        scaledStarIcon = Bitmap.createScaledBitmap(
                scaledStarIcon,
                (int) ((float) scaledStarIcon.getWidth() * (heartAndStarHeight / scaledStarIcon.getHeight())),
                (int) (heartAndStarHeight),
                false
        );
        scaledHeartIcon = Bitmap.createScaledBitmap(
                scaledHeartIcon,
                (int) ((float) scaledHeartIcon.getWidth() * (heartAndStarHeight / scaledHeartIcon.getHeight())),
                (int) (heartAndStarHeight),
                false
        );
        heartsNumberBitmap = Bitmap.createBitmap(
                (int) (xSize * 0.5f),
                (int) heartAndStarHeight,
                Bitmap.Config.ARGB_8888
        );
        starsNumberBitmap = Bitmap.createBitmap(
                (int) (xSize * 0.5f),
                (int) heartAndStarHeight,
                Bitmap.Config.ARGB_8888
        );
        createHeartNumberBitmap(0);
        createStarNumberBitmap(0.0f);

        blurThickness = IntRepConsts.COUNTDOWN_HALO_BLUR_THICKNESS * playAreaInfo.screenWidth;
        readoutBoxPaint.setMaskFilter(new BlurMaskFilter(blurThickness, BlurMaskFilter.Blur.NORMAL));
    }
    void createHeartNumberBitmap(int numberOfHearts) {
        String number = "  " + String.valueOf(numberOfHearts) + " ";
        float width, height;
        heartNumberPaint.getTextBounds(number, 0, number.length(), heartNumberBounds);
        width = heartNumberPaint.measureText(number);
        height = heartNumberBounds.height();
        heartTempBitMap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        utilityCanvas = new Canvas(heartTempBitMap);
        utilityCanvas.drawText(number, 0, height, heartNumberPaint);

        float ratio = height / heartAndStarNumberHeight;
        width = width / ratio * IntRepConsts.HEART_AND_STAR_HOR_SQUEEZE;
        height = heartAndStarNumberHeight;
        heartTempBitMap = Bitmap.createScaledBitmap(heartTempBitMap, (int) width, (int) height, false);

        heartsNumberBitmap = Bitmap.createBitmap(
                (int) width + scaledHeartIcon.getWidth(),
                (int) heartAndStarHeight,
                Bitmap.Config.ARGB_8888);
        utilityCanvas = new Canvas(heartsNumberBitmap);
        utilityCanvas.drawRect(0, 0, heartsNumberBitmap.getWidth(), heartsNumberBitmap.getHeight(), blankTransparentPaint);
        utilityCanvas.drawBitmap(heartTempBitMap, 0, 0, null);
        utilityCanvas.drawBitmap(scaledHeartIcon, width, 0, null);
        /*heartXPos = scoreXCenter - (xSize * 0.5f) + (xSize * HEART_REL_XPOS) - width;*/
        heartXPos = xPos + xSize - (width + scaledHeartIcon.getWidth());

    }

    void createStarNumberBitmap(float averageStars) {
        if (averageStars < 0.0f || averageStars > 5.0f || Float.isNaN(averageStars)) {
            averageStars = 0.0f;
        }
        String number = FLOAT_FORMAT.format(averageStars) + " ";
        float width, height;
        starNumberPaint.getTextBounds(number, 0, number.length(), starNumberBounds);
        width = starNumberPaint.measureText(number);
        height = starNumberBounds.height();
        starTempBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        utilityCanvas = new Canvas(starTempBitmap);
        utilityCanvas.drawText(number, 0, height, starNumberPaint);

        float ratio = height / heartAndStarNumberHeight;
        width = width / ratio * IntRepConsts.HEART_AND_STAR_HOR_SQUEEZE;
        height = heartAndStarNumberHeight;
        starTempBitmap = Bitmap.createScaledBitmap(starTempBitmap, (int) width, (int) height, false);

        starsNumberBitmap = Bitmap.createBitmap(
                (int) width + scaledStarIcon.getWidth(),
                (int) heartAndStarHeight,
                Bitmap.Config.ARGB_8888);
        utilityCanvas = new Canvas(starsNumberBitmap);
        utilityCanvas.drawRect(0, 0, starsNumberBitmap.getWidth(), starsNumberBitmap.getHeight(), blankTransparentPaint);
        utilityCanvas.drawBitmap(starTempBitmap, 0, 0, null);
        utilityCanvas.drawBitmap(scaledStarIcon, width, 0, null);
        /*starXPos = scoreXCenter - (xSize * 0.5f) + (xSize * STAR_REL_XPOS) - width;*/
        starXPos = xPos + xSize - (width + scaledStarIcon.getWidth());
    }

    public void updateAndDrawHeartAndStarDisplay(Canvas canvas, float averageNumberOfStars, int livesRemaining, boolean usingBB1) {
        if (averageNumberOfStars != starsCurrentlyShown && !Float.isNaN(averageNumberOfStars) ) {
            starsCurrentlyShown = averageNumberOfStars;
            redrawOnBB1 = true;
            redrawOnBB2 = true;
            createStarNumberBitmap(averageNumberOfStars);
        }
        if (livesRemaining != heartsCurrentlyShown) {
            heartsCurrentlyShown = livesRemaining;
            redrawOnBB1 = true;
            redrawOnBB2 = true;
            createHeartNumberBitmap(livesRemaining);
        }
        if (redrawOnBB1 && usingBB1) {
            drawHeartAndStarBackground(canvas);
            canvas.drawBitmap(starsNumberBitmap, starXPos, starYPos, null);
            canvas.drawBitmap(heartsNumberBitmap, heartXPos, heartYPos, null);
            redrawOnBB1 = false;
        }
        if (redrawOnBB2 && !usingBB1) {
            drawHeartAndStarBackground(canvas);
            canvas.drawBitmap(starsNumberBitmap, starXPos, starYPos, null);
            canvas.drawBitmap(heartsNumberBitmap, heartXPos, heartYPos, null);
            redrawOnBB2 = false;
        }
    }

    private void drawHeartAndStarBackground(Canvas c) {
        c.drawRect(
                starXPos,
                starYPos,
                starXPos + starsNumberBitmap.getWidth(),
                starYPos + starsNumberBitmap.getHeight(),
                blackPaint
        );
        c.drawRect(
                heartXPos,
                heartYPos,
                heartXPos + heartsNumberBitmap.getWidth(),
                heartYPos + heartsNumberBitmap.getHeight(),
                blackPaint
        );
    }

    public void drawHighScore(Canvas canvas) {
        canvas.drawBitmap(highScoreBitmap, highScoreXPos, highScoreYPos, null);
    }

    private void drawHighScoreOnBitmap() {
        float xCoor;
        float highScoreGlyphHeight = (float) highScoreDigitImages.getHeight() / NUMBER_OF_GLYPHS_IN_BASE_IMAGE;
        float highScoreGlyphWidth = (float) highScoreDigitImages.getWidth();
        Rect source = new Rect();
        RectF dest = new RectF();
        Canvas can = new Canvas(highScoreBitmap);
        can.drawARGB(255, 0, 0, 0);
        if (highScore > Math.pow(10, NUMBER_OF_DIGITS) - 1) {
            highScore = (int) Math.pow(10, NUMBER_OF_DIGITS) - 1;
        }
        int log = (int) Math.log10(highScore) + 1;
        int temp = highScore;
        for (int i = 0; i < log; i++) {
            float digit = temp % 10;
            temp = temp / 10;
            xCoor = (highScoreXSize - (glyphWidth * highScoreRelativeSize) * (1 + i));
            source.set(
                    0,
                    (int)(digit * highScoreGlyphHeight),
                    (int) highScoreGlyphWidth,
                    (int) (highScoreGlyphHeight * (digit + 1))
            );
            dest.set(xCoor, 0, xCoor + highScoreGlyphWidth, highScoreGlyphHeight);
            can.drawBitmap(highScoreDigitImages, source, dest, null);
        }
    }

    private void calculateScaledGlyphYCoors() {
        for (int i = 0; i < NUMBER_OF_DIGITS; i++) {
            scaledGlyphYCoordinates[i] = glyphYCoordinates[i] * heightOfScaledDigitImages / UNIT_RANGE_OF_SDD;
        }
    }

    public void drawScoreBoxBackgrounds(Canvas canvas) {
        int w = playAreaInfo.screenWidth;
        int h = playAreaInfo.topPanelHeight;
        float border = IntRepConsts.SCORE_BORDER_THICKNESS * w;
        float radius = IntRepConsts.ROUNDRECT_RADIUS * w;
        float left = IntRepConsts.SCORE_XPOS * w - border;
        float top = IntRepConsts.SCORE_YPOS * h - border;
        float right = IntRepConsts.SCORE_XPOS * w +
                IntRepConsts.SCORE_XSIZE * w + border;
        float bottom = IntRepConsts.SCORE_YPOS * h +
                IntRepConsts.SCORE_YSIZE * h + border;
        canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, readoutBoxPaint);
        canvas.drawRect(new RectF(
                        IntRepConsts.SCORE_XPOS * w,
                        IntRepConsts.SCORE_YPOS * h,
                        IntRepConsts.SCORE_XPOS * w +
                                IntRepConsts.SCORE_XSIZE * w,
                        IntRepConsts.SCORE_YPOS * h +
                                IntRepConsts.SCORE_YSIZE * h),
                blackPaint);

        radius = radius * highScoreRelativeSize;
        border = border * highScoreRelativeSize;
        left = highScoreXPos - border;
        right = highScoreXPos + highScoreXSize + border;
        top = highScoreYPos - border;
        bottom = highScoreYPos + highScoreYSize + border;
        canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, readoutBoxPaint);
        canvas.drawRect(new RectF(
                        highScoreXPos,
                        highScoreYPos,
                        highScoreXPos + highScoreXSize,
                        highScoreYPos + highScoreYSize),
                blackPaint);

        String HIGH_SCORE_TAG = "HIGH";
        Rect bounds = new Rect();
        Paint highScoreTagPaint = new Paint();
        highScoreTagPaint.setARGB(255, 255, 255, 255);
        highScoreTagPaint.setTextSize(40);
        highScoreTagPaint.getTextBounds(HIGH_SCORE_TAG, 0, HIGH_SCORE_TAG.length(), bounds);
        float width = highScoreTagPaint.measureText(HIGH_SCORE_TAG);
        float height = (float) bounds.height();
        Bitmap source = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(source);
        //can.drawARGB(150, 0, 255, 0);
        can.drawText(HIGH_SCORE_TAG, 0, height, highScoreTagPaint);
        float relSize = 0.4f;
        float h1 = highScoreYSize * relSize;
        Bitmap dest = Bitmap.createScaledBitmap(
                source,
                (int) ((float)source.getWidth() * (h1 / source.getHeight())),
                (int) h1,
                false
        );
        float x = highScoreXPos + (highScoreXSize / 2) - dest.getWidth() / 2;
        float y = highScoreYPos - (border * 1.2f) - h1;
        canvas.drawBitmap(dest, x, y, highScoreTagPaint);
    }

    public float getGlyphWidth() {
        return glyphWidth;
    }

    public float getGlyphHeight() {
        return glyphHeight;
    }

    public int getNumberOfGlyphs() {
        return NUMBER_OF_DIGITS;
    }
    public float getxPos() {
        return xPos;
    }
    public float getyPos() {
        return yPos;
    }
    public float getxSize() {
        return xSize;
    }
    public float getySize() {
        return ySize;
    }
    public Bitmap getDigitImages() {
        return digitImages;
    }
    public float getDivider() {
        return divider;
    }
    public PointF getCenterOfHeartIcon() {
        return new PointF(
                heartXPos + heartAndStarHeight / 2,
                heartYPos + heartAndStarHeight / 2);
    }

}
