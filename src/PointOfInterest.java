import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class PointOfInterest extends DrawablePoint {
	private int weight;

	public PointOfInterest(int x, int y, int weight) {
		super(x,y, Color.red);
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}
	

	
}