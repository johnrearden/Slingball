package com.intricatech.slingball;

public interface LevelChangeDirector {
	
	public void register(LevelChangeObserver levelChangeObserver);
	
	public void unregister(LevelChangeObserver levelChangeObserver);
	
	public void updateObservers();
	
}
