import java.util.Random;

public class RandomSingleton {

	private final static long SEED = 0;
	private RandomSingleton() {}
	
	private static Random r;
	
	static {
		setSeed(SEED);
	}
	
	public static void setSeed(long seed) {
		 r = new Random();
		 r.setSeed(seed);
	}
	
	public static double nextDouble() {
		return r.nextDouble();
	}
	
	public static int nextInt(int a) {
		return r.nextInt(a);
	}
	
	public static double randDouble(float from, float to) {
		return r.nextDouble() * (to - from) + from;
	}

	public static float randFloat(float from, float to) {
		return r.nextFloat() * (to - from) + from;

	}

	public static boolean randBool() {
		return r.nextDouble()>0.5;
	}

	public static int nextInt(int from, int to) {
		return r.nextInt(from, to);
	}

}