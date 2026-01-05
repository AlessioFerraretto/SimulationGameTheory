import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import javax.swing.JPanel;

public class Panel extends JPanel {

	SimulationThread t;
	int time;

	public Panel() {
		super();
		t = new SimulationThread(this);
		t.start();

	}

	public void paint(Graphics g) {
		super.paint(g);
		synchronized(t.getSimulationModel()) {
			g.setColor(Color.white);
			g.fillRect(0, 0, 5000, 5000);

			t.getSimulationModel().getLostDevices().stream().forEach(l -> l.draw(g));
			t.getSimulationModel().getAgents().stream().filter(a-> !a.getHasExited()).forEach(a -> a.draw(g));
			t.getSimulationModel().getPois().forEach(a -> a.draw(g));
		}

	}

	public void refresh() {
		this.invalidate();
		this.revalidate();
		this.repaint();

	}

	public void setTime(int time) {
		this.time = time;		
	}

}
