package com.intricatech.slingball;

import java.util.ArrayList;
import java.util.List;

public class LevelManager implements LevelChangeDirector {

	private int level;
	
	private List<LevelChangeObserver> observerList;
	

	

	
	LevelManager(int level) {
		// gameController will increment the level the first time the run() method executes, because targetManager.levelPhase
		// is initialized to READY_FOR_NEW_LEVEL.
		this.level = level - 1;
		//level = IntRepConsts.STARTING_LEVEL - 1;
		observerList = new ArrayList<LevelChangeObserver>();
	}

	@Override
	public void updateObservers() {
		if (level >= 0) {
			for (LevelChangeObserver lco : observerList) {
				lco.updateConstants(level);
			}
		}
	}

	@Override
	public void register(LevelChangeObserver levelChangeObserver) {
		observerList.add(levelChangeObserver);
	}

	@Override
	public void unregister(LevelChangeObserver levelChangeObserver) {
		observerList.remove(levelChangeObserver);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
		updateObservers();
	}
	
}
