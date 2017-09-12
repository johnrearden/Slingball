package com.intricatech.slingball;

public enum TargetColor {
	RED(245, 72, 49),
	GRAY(100, 100, 100),
	GREEN(42, 230, 200),
	BLUE(9,9,230),
	PURPLE(173, 10, 163),
	YELLOW(237, 186, 33),
	/*YELLOW(100, 100, 100),*/
	OFF_WHITE(180, 180, 180),
	DECOY_RED(180, 0, 0),
	GOLD(250, 185, 5),
	SILVER(169, 169, 169);

	
	private int redComponent, greenComponent, blueComponent;
	
	TargetColor(int r, int g, int b) {
		this.redComponent = r;
		this.greenComponent = g;
		this.blueComponent = b;
	}

	public int getRedComponent() {
		return redComponent;
	}

	public int getGreenComponent() {
		return greenComponent;
	}

	public int getBlueComponent() {
		return blueComponent;
	}
}
