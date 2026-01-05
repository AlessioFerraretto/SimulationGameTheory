import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationModel {

	// --- Simulation Constants ---
	public static final double SPLIT = 0.3; // 30% of users in the area use the net

	public static final int AREA_WIDTH = 4000;  // in meter
	public static final int AREA_HEIGHT = 4000;  // in meter
	public static final int PAINT_SCALE = 4;  // inverse of the scale
	
	public static final int km_squared = AREA_WIDTH/1000 * AREA_HEIGHT/1000;
	public static final long N_AGENTS = (long) (200 * km_squared * SPLIT);
	public static final int SEED = 0;
	public static final int NUMBER_POIS = (int) (N_AGENTS/10);

	// Time steps
	public static final int SIMULATION_DURATION_MINUTES = 1 * 365 * 60 * 24; // One year
	public static final int T_DECISION = 60 * 24; // 1 day


	private List<Integer> delays;
	private List<Agent> agents;
	private List<PointOfInterest> pois;
	private List<LostDevice> lostDevices;
	
	public SimulationModel() {
		delays = new ArrayList<>();
		agents = new ArrayList<>();
		pois = new ArrayList<>();
		lostDevices = new ArrayList<>();
		
		// 1. Generate POIs (Poisson distribution logic)
		// Simplifying Poisson generation: creating discrete centers
		for (int i = 0; i < NUMBER_POIS; i++) {
			int px = RandomSingleton.nextInt(0,  AREA_WIDTH);
			int py = RandomSingleton.nextInt(0, AREA_HEIGHT);
			int weight = getPoisson(1.0); // Lambda = 1
			getPois().add(new PointOfInterest(px, py, weight));
		}

		// 2. Populate Agents
		for (int i = 0; i < N_AGENTS; i++) {
			getAgents().add(new Agent());
		}

		System.out.println("Initialized " + N_AGENTS + " agents.");
	}


	public void step(int t) {
		for (Agent agent : getAgents()) {
			// Phase 1: Mobility
			agent.move(getPois(), t, this);

			// Phase 2: Game Theoretic Update (Every 24 hours)
			if (t % T_DECISION == 0) {
				agent.updateStrategy();
			}

			// Phase 3: Device Aging
			agent.ageDevice(1);
			
			LostDevice dev = agent.loseDevice(t);
			if(dev!=null) {
				getLostDevices().add(dev);
			}

		}

		// Optional: Log statistics every day
		if (t % T_DECISION == 0) {
			printStats(t);
		}
	}
	

	// Knuth algorithm for Poisson generation
	private int getPoisson(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
			k++;
			p *= RandomSingleton.nextDouble();
		} while (p > L);
		return k - 1;
	}

	private void printStats(int t) {
		int day = t/60/24;
		long activeCount = getAgents().stream()
				.filter(a -> !a.getHasExited()).count();
		long exitedCount = getAgents().stream().filter(a -> a.getHasExited()).count();
		long agentsWithPhone = getAgents().stream().filter(a -> !a.getLostDevice()).count();
		System.out.println("Day: " + day + " | Active Agents: " + activeCount + " | Exited: " + exitedCount + " | Agents with phone " + agentsWithPhone);
	}


	public synchronized List<Agent> getAgents() {
		return agents;
	}

	public synchronized List<PointOfInterest> getPois() {
		return pois;

	}

	public synchronized List<LostDevice> getLostDevices() {
		return lostDevices;

	}


	public List<Integer> getDelays() {
		return delays;
	}
}