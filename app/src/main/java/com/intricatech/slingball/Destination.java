package com.intricatech.slingball;

import java.io.Serializable;

public class Destination implements Serializable {
	
	float xDest, yDest;
	
	public Destination(float xDest, float yDest) {
		this.xDest = xDest;
		this.yDest = yDest;
	}
	
	
}
