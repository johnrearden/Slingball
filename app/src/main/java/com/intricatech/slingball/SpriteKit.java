package com.intricatech.slingball;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

/**
 * Class contains all information necessary to draw a target in each orbit. All coordinates are
 * relative to the outermostTargetRect variable held in the PlayAreaInfo object.
 *
 * Created by Bolgbolg on 27/11/2015.
 */
public class SpriteKit {
    Map<TargetSize, Sprite[]> spriteMap;
    int numberOfPossibleOrbits;
    TargetType type;
    SpriteType spriteType;

    class Sprite {
        float xPos, yPos;  // the position of the sprite relative to the outermostTargetRect.
        Bitmap spriteImage;

        Sprite (float xPos, float yPos, Bitmap spriteImage) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.spriteImage = spriteImage;
        }
    }

    public SpriteKit (
            PlayAreaInfo playAreaInfo,
            SpriteType sType,
            TargetType targetType) {
        numberOfPossibleOrbits = IntRepConsts.MAX_NUMBER_OF_ORBITS;
        spriteMap = new HashMap<TargetSize, Sprite[]>();
        for (TargetSize size : TargetSize.values()) {
            spriteMap.put(size, new Sprite[numberOfPossibleOrbits]);
        }
        type = targetType;
        spriteType = sType;
    }

    public Sprite createSprite(float xPos, float yPos, Bitmap spriteImage){
        Sprite returnSprite = new Sprite(xPos, yPos, spriteImage);
        return returnSprite;
    }

    float getxCoor(int orbitIndex, TargetSize size) {
        return spriteMap.get(size)[orbitIndex].xPos;
    }
    float getyCoor(int orbitIndex, TargetSize size) {

        return spriteMap.get(size)[orbitIndex].yPos;
    }
    Bitmap getSpriteImage(int orbitIndex, TargetSize size){
        return spriteMap.get(size)[orbitIndex].spriteImage;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("SpriteKit for " + type + "\n");
        for (TargetSize size : TargetSize.values()) {
            for (int i = 0; i < numberOfPossibleOrbits; i ++) {
                sb.append(size + ", " + i + " : " + spriteMap.get(size)[i] + ", width == " + spriteMap.get(size)[i].spriteImage.getWidth() + "\n");
            }
        }

        return sb.toString();
    }
}
