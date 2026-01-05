import java.awt.Rectangle;

import javax.swing.JFrame;

public class Frame extends JFrame {

	public Frame() {
		super();
		add(new Panel());
		
		setBounds(new Rectangle(600,600));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	
}
