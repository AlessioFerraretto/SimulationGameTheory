import java.awt.Color;

public class LostDevice extends DrawablePoint {
  
    private int timeLost;
	private Agent agent;

    public LostDevice(int x, int y, int timeLost, Agent agent) {
    	super(x, y, Color.ORANGE);
        this.timeLost = timeLost;
        this.agent = agent;
    }

	public Agent getAgent() {
		return agent;
	}

	public Integer getTime() {
		return timeLost;
	}

   
	
}