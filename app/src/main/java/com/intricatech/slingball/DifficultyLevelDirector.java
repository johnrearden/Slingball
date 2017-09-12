package com.intricatech.slingball;

import java.util.ArrayList;

/**
 * Created by Bolgbolg on 04/12/2015.
 */
public class DifficultyLevelDirector {

    private static final String TAG = "DifficultyLevelDirector";
    private DifficultyLevel level;
    private ArrayList<DifficultyLevelObserver> observerList;

    public DifficultyLevelDirector() {
        level = null;
        observerList = new ArrayList<DifficultyLevelObserver>();
    }

    public void register(DifficultyLevelObserver ob) {
        observerList.add(ob);
        ob.updateDifficultyDependents(level);
    }

    public boolean unregister(DifficultyLevelObserver ob) {
        if (!observerList.contains(ob)) {
            return false;
        }
        observerList.remove(ob);
        return true;
    }

    public void updateDifficultyLevel(DifficultyLevel level) {
        this.level = level;
        updateObservers();
    }

    public void updateObservers() {
        for (DifficultyLevelObserver ob : observerList) {
            ob.updateDifficultyDependents(level);
        }
    }

    public DifficultyLevel getDiffLev() {
        return level;
    }

    public int getLastLevel() {
        int returnValue;
        switch (level) {
            case HARD: {
                returnValue = IntRepConsts.HIGHEST_DEFINED_LEVEL_HARD;
                break;
            }
            case NORMAL: {
                returnValue = IntRepConsts.HIGHEST_DEFINED_LEVEL_NORMAL;
                break;
            }
            case EASY: {
                returnValue = IntRepConsts.HIGHEST_DEFINED_LEVEL_EASY;
                break;
            }
            default : {
                returnValue = 9;
            }
        }
        return returnValue;
    }
}
