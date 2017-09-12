package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 04/12/2015.
 */
public interface DifficultyLevelObserver {
    public void registerWithDifficultyLevelDirector();
    public void unregisterWithDifficultyLevelDirector();
    public void updateDifficultyDependents(DifficultyLevel level);

}
