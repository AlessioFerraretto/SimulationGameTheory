
public class test {

	public static final double P = 9.7e-8;

	public static void main(String[] args) {
		int count = 0;
		
		for(int i=0;i<365 * 24 * 60 * 240;i++) {
			if(RandomSingleton.nextDouble() < P ) {
				count ++;
			}
		}
		
		System.out.println(count);
	}
}
