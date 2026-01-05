import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SimulationThread extends Thread {

	private Panel p;
	private SimulationModel model;

	public SimulationThread(Panel p) {
		super();
		this.p = p;
		this.model = new SimulationModel();

	}
	
	public void run() {

		for (int t = 0; t < SimulationModel.SIMULATION_DURATION_MINUTES; t++) {
			model.step(t);
			p.setTime(t);
			p.refresh();
		}

		Map<Integer, Integer> counts = new HashMap<>();

		long avg = 0;
        for (int num : model.getDelays()) {
            counts.put(num, counts.getOrDefault(num, 0) + 1);
            avg += num;
        }
        avg /= model.getDelays().size();
        

        // Print the results
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"))) {
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                writer.write(entry.getKey() + ";" + entry.getValue());
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        System.out.println("avg " + avg);
        
        double lostDevices =  model.getDelays().size() + model.getLostDevices().size();
        double retrievedDevices = model.getDelays().size();
        System.out.println("percent retrivial : " + retrievedDevices/lostDevices);
        System.out.println("lost total: " + lostDevices);
        
		
	}

	public synchronized SimulationModel getSimulationModel() {
		return model;
	}
}
