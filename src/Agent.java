import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import java.util.Random;

public class Agent extends Point {

	// Probabilities
	public static final double P_LOSS_PER_MINUTE = 9.7e-8; // Probability of losing your device
	public static final double WORK_PROBABILITY = 0.9; // Probability of losing your device
	public static final int MAX_SPEED = 120; // Speed of the Agent in meter/minute
	public static final int BASE_COST = 1; // C_0 The base cost of being active is 1


    private static final double INITIAL_HEALTH = 100.0; // H0
    private static final double BETA = 0.0000005; // Beta: Degradation Coefficient (tuned for minutes)
    private static final double  DELTA = 0.000001; // Delta: 
    
	public static final int DETECTION_RANGE = 10; // 10 meters

	// Agent params
	private Point home, work;
	private int speed;
	private Point currentDestination;
	private boolean hasExited; // If Cost > Value
	private boolean lostDevice;
	private int stationary; // number of minutes it stays in the same place

	// Device params
	private int deviceAgeMinutes; 
	private int value;    // V(t)
	private int costScan; // C_scan(t)
	private int initialValue; //V_0


	public Agent() {
		super();
		lostDevice = false;

		this.speed = RandomSingleton.nextInt(1, MAX_SPEED);

		// Uniform Home Location in Area A
		this.home = new Point(
				RandomSingleton.nextInt(0, SimulationModel.AREA_WIDTH),
				RandomSingleton.nextInt(0, SimulationModel.AREA_HEIGHT));

		x = home.x;
		y = home.y;

		// Device Age U[0, 3] years
		this.deviceAgeMinutes = RandomSingleton.nextInt(0, 3 * 365 * 24 * 60);

		this.initialValue = RandomSingleton.nextInt(800,1600);

		// Strategy Initialization
		this.hasExited = false;
		

		// Initialize Value and Cost based on age (Placeholder functions)
		updateEconomicParams();
	}

	/**
	 * 1. Mobility Phase
	 * Agents move toward a POI or stay put based on P_stay
	 * @param time 
	 */
	public void move(List<PointOfInterest> pois, int time, SimulationModel sim) {
		if(work == null) {
			work = pois.get(RandomSingleton.nextInt(pois.size()));
		}

		if (hasExited) return;

		// Check if staying stationary
		if (stationary>0) {
			stationary--;
			return;
		}
		synchronized(sim) {
			if (!lostDevice) {
				for (int i=0;i<sim.getLostDevices().size();i++) {
					LostDevice d = sim.getLostDevices().get(i);
					double dist = d.distance(this);
					if (dist <= DETECTION_RANGE) {
						d.getAgent().deviceFound();
						sim.getDelays().add(time - d.getTime());
						sim.getLostDevices().remove(d);
						i--;
					}
				}
			}
		}

		// If no destination, pick one based on weights (Simplified probabilistic model)
		if (currentDestination == null) {
			currentDestination = pickDestination(pois);
		}
		if (hasReachedDestination() && currentDestination == home) {
			if(RandomSingleton.nextDouble() < WORK_PROBABILITY) {
				currentDestination = work;
			} else {
				currentDestination = pickDestination(pois);
			}
		}
		if (hasReachedDestination() && currentDestination != home) {
			currentDestination = home;
		}

		// Move towards destination (Simple vector movement)
		int dx = currentDestination.x - x;
		int dy = currentDestination.y - y;
		double dist = Math.sqrt(dx*dx + dy*dy);

		if (dist <= speed) {
			x = currentDestination.x;
			y = currentDestination.y;

			stationary = RandomSingleton.nextInt(1*60,12*60);


		} else {
			x += (dx / dist) * speed;
			y += (dy / dist) * speed;
		}
	}

	/**
	 * 2. Game Theoretic Update
	 * Switch strategy if E[U_alt] > U_current
	 */
	public void updateStrategy() {
		if (hasExited) return;

		double utilityActive = calculateExpectedUtility(Strategy.ACTIVE);
		double utilityPassive = calculateExpectedUtility(Strategy.PASSIVE);

//		// Rational switch logic
		if (utilityPassive > utilityActive) {
			hasExited = true;
		}
	}

	/**
	 * 3. Device Aging
	 * Update Value and Cost. Exit if Cost > Value.
	 */
	public void ageDevice(double deltaMinutes) {
		if (hasExited) return;

		this.deviceAgeMinutes += deltaMinutes;
		updateEconomicParams();
	}

	private void updateEconomicParams() {
        // 1. Value Decay: V(t) = V0 * e^(-delta * t)
        this.value = (int) (this.initialValue * Math.exp(-DELTA * deviceAgeMinutes));     

        // 2. Battery Health: H(t) = H0 * e^(-beta * t)
        double currentHealth = INITIAL_HEALTH * Math.exp(-BETA * deviceAgeMinutes);

        // 3. Escalating Cost: C(t) = C0 / H(t)
        // Ensure we don't divide by zero if health gets too low
        if (currentHealth > 0.01) {
            this.costScan = (int) (BASE_COST / (currentHealth / INITIAL_HEALTH)); 
            // Note: normalizing by initialHealth keeps the units consistent with BASE_COST
        } else {
            this.costScan = Integer.MAX_VALUE; // Prohibitive cost as battery dies
        }
    }

	public LostDevice loseDevice(int t) {
//		if(this.equals(home)) return null;
		if(lostDevice) return null;
		if(hasExited) return null;

		
		if (RandomSingleton.nextDouble() < P_LOSS_PER_MINUTE) {
			
			lostDevice = true;
			return new LostDevice(x, y, t, this);
		}
		return null;
	}


	private double calculateExpectedUtility(Strategy s) {
	    // 1. Define P_loss (Probability the device goes missing)
	    double pLoss = 0.05;

	    // 2. Define P_find (Probability of finding it given it is lost)
	    double pFind = (s == Strategy.ACTIVE) ? 0.9d : 0.0; 

	    // 3. Define Cost (C_scan)
	    double cost = (s == Strategy.ACTIVE) ? costScan : 0;

	    // 4. Calculate Terms
	    double expectedLoss = pLoss * this.value; 
	    
	    double expectedRecovery = expectedLoss * pFind; 

	    return -cost - expectedLoss + expectedRecovery;
	}
	
	private boolean hasReachedDestination() {
		if (currentDestination == null) return true;
		return Math.abs(x - currentDestination.x) < 1.0 && Math.abs(y - currentDestination.y) < 1.0;
	}

	private PointOfInterest pickDestination(List<PointOfInterest> pois) {
		if (pois == null || pois.isEmpty()) {
			return null; // Handle empty list case
		}

		// 1. Calculate the total weight of all POIs
		double totalWeight = 0.0;
		for (PointOfInterest poi : pois) {
			totalWeight += poi.getWeight();
		}

		// 2. Generate a random value between 0 and totalWeight
		// Assuming RandomSingleton has a nextDouble() method (returns 0.0 to 1.0)
		double randomValue = RandomSingleton.nextDouble() * totalWeight;

		// 3. Find the POI that corresponds to this random value
		double cumulativeWeight = 0.0;
		for (PointOfInterest poi : pois) {
			cumulativeWeight += poi.getWeight();
			if (cumulativeWeight >= randomValue) {
				return poi;
			}
		}

		// Fallback: This should logically not be reached unless rounding errors occur,
		// in which case returning the last element is safe.
		return pois.get(pois.size() - 1);
	}

	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillOval(x / SimulationModel.PAINT_SCALE, y / SimulationModel.PAINT_SCALE, 3, 3);

		g.setColor(Color.green);
		g.fillOval(home.x / SimulationModel.PAINT_SCALE, home.y / SimulationModel.PAINT_SCALE, 3, 3);
	}

	public void deviceFound() {
		lostDevice = false;

	}

	public boolean getLostDevice() {
		return lostDevice;
	}

	public boolean getHasExited() {
		return hasExited;
	}

}