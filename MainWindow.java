import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ingame.Game;
import ingame.GameState;

public class MainWindow extends JFrame {
	//ctor
	public MainWindow() {
		//mainwindow
		super();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); //size
		this.setBounds(0, 0, (int) dim.getWidth(), (int) dim.getHeight());
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(null);
		//this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		//this.setUndecorated(true);
		//show
		this.setVisible(true);
	}
	//get-set
	//method
	public void changePanel(GameState newGamestate) {
		Dimension dim = this.getSize();
		//panel
		if(newGamestate == GameState.START) {
			if(Game.gamestate != GameState.START) {
				if(this.playingPanel != null) this.remove(this.playingPanel);
				this.playingPanel = null;
				this.startPanel = new StartPanel(dim);
				this.add(this.startPanel);
			}
		} else {
			if(Game.gamestate == GameState.START || Game.gamestate == GameState.INIT) {
				if(this.startPanel != null) this.remove(this.startPanel);
				this.startPanel = null;
				this.playingPanel = new PlayingPanel(dim);
				this.add(this.playingPanel);
			}
		}
	}
	//get-set
	public PlayingPanel getPlayingPanel() {
		return playingPanel;
	}
	//var
	private StartPanel startPanel;
	private PlayingPanel playingPanel;
}