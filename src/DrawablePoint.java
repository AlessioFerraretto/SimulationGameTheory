import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class DrawablePoint extends Point {

	Color color;
	
	public DrawablePoint(int x, int y, Color color) {
		super(x,y);
		this.color = color;
	}

	public void draw(Graphics g) {
		g.setColor(color);
		g.fillOval(x / SimulationModel.PAINT_SCALE, y / SimulationModel.PAINT_SCALE, 5, 5);
	}
}
