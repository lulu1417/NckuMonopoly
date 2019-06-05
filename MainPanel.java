import java.awt.Dimension;

import javax.swing.JPanel;

import ingame.Game;
public class MainPanel extends JPanel{
	public MainPanel(Dimension dim) {
		
		super();
		System.out.println("dim = "+dim);
		double sc_w = dim.getWidth()/Game.Width;
		double sc_h = dim.getHeight()/Game.Height;
		System.out.println("sc_w sc_h = "+sc_w+" "+sc_h);
		if(sc_w < sc_h) 
			this.setBounds(0,(int) ((dim.getHeight()-Game.Height*sc_w)/2),
				(int) dim.getWidth(), (int) (Game.Height*sc_w));
		else
			this.setBounds((int) ((dim.getWidth()-Game.Width*sc_h)/1), 0,
				(int) (Game.Width*sc_h), (int) dim.getHeight());
		this.setLayout(null);
	}

}
