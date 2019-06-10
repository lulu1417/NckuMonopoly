import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import ingame.Cell;
import ingame.Game;
import ingame.GraphicImgItem;
import ingame.GraphicItem;
import ingame.GraphicTextItem;

public class PlayingPanel extends MainPanel{
	//ctor
	public PlayingPanel(Dimension dim) {
		super(dim);
		this.selections = new ClickButton[3];
		this.dieSelections = new ClickButton[6];
		this.mouse_pos = new Point(0,0);
	}
	//method
	public void paint(Graphics g) {
		double sc = (double)this.getWidth() / Game.Width;
		//music button
		image = new ImageIcon("music.png");
		MusicButton = new ClickButton(1150, 580, 100, 100, sc, image,"Music");
		MusicButton.setOpaque(false);
		MusicButton.setContentAreaFilled(false);  
		MusicButton.setFocusPainted(false);
		MusicButton.setBorder(null); 
		this.add(MusicButton);
		//mouse
		Point mouse_pos_temp = MouseInfo.getPointerInfo().getLocation();
		this.mouse_pos.setLocation((mouse_pos_temp.getX()-this.getLocationOnScreen().getX())/sc, (mouse_pos_temp.getY()-this.getLocationOnScreen().getY())/sc);
		//sort
		Collections.sort(Game.graphicItems, new Comparator<GraphicItem>() {
			public int compare(GraphicItem a, GraphicItem b) 
			{
				if(a.getZ() > b.getZ()) return 1;
				if(a.getZ() == b.getZ()) return 0;
				return -1;
			}
		});
		//paint
		for(GraphicItem graphicItem: Game.graphicItems) graphicItem.draw(g,sc);
		Graphics2D g2 = (Graphics2D)g;
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0);
		g2.setComposite(ac);
		super.paintComponents(g);
	}
	public void showCellName(Cell cell, String cellName) {
		int x = (int) cell.getX(), y = (int) (cell.getY());
		this.cellNameBg = new GraphicImgItem(x, y, 164, 40, "/cellNameBg.png", Game.graphicItems);
		this.cellNameBg.setZ(1000);
		this.cellNameBg.setLifeTime(1);
		this.cellNameBg.setOpacity(0.8);
		this.cellName = new GraphicTextItem(x, y, 25, cellName, Game.graphicItems);
		this.cellName.setZ(1000);
		this.cellName.setLifeTime(1);
		this.cellName.setOpacity(0.8);
	}
	public void showEventName(String eventName, int x, int y, int lifetime) {
		if(this.eventName != null) this.eventName.kill();
		this.eventName = new GraphicTextItem(x, y, 50, eventName, Game.graphicItems);
		this.eventName.setLifeTime(lifetime);
	}
	public void createRollingButton() {
		if(this.rollingButton != null) return;
		int w = 250, h = 100;
		double sc = (double)this.getWidth() / Game.Width;
		this.rollingButton = new ClickButton((Game.Width-250)/2+120, Game.Height/2, w, h, sc, "擲骰子", "Roll");
		this.add(rollingButton);
	}
	public void deleteRollingButton() {
		if(this.rollingButton == null) return;
		this.remove(rollingButton);
		this.rollingButton = null;
	}
	public void createDieSelections(String eventName) {
		int w = 100, h = 100;
		double sc = (double)this.getWidth() / Game.Width;
		this.showEventName(eventName, (Game.Width-250)/2+250-50,(int) (Game.Height/2-2*(h+25)+10+(0.5*h)), -1);
		for(int i=0; i<6; ++i) {
			String signal;
			signal = "Select die point: " + (i+1);
			try {
				URL url = this.getClass().getResource("/die"+ (i+1) +".png");
				ImageIcon img = new ImageIcon(ImageIO.read(url));
				this.dieSelections[i] = new ClickButton((int) ((Game.Width-250)/2+250 + (i/3-0.5)*120), Game.Height/2+(i%3-1)*(h+25)+50, w, h, sc, img, signal);
				this.add(dieSelections[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void deleteDieSelections() {
		for(int i=0; i<6; ++i) {
			if(dieSelections[i] == null) continue;
			this.remove(dieSelections[i]);
			dieSelections[i] = null;
		}
		if(eventName == null) return;
		eventName.kill();
		eventName = null;
	}
	public void createSelections(String eventName, String selection1, String selection2, String selection3) {
		int w = 500, h = 100;
		double sc = (double)this.getWidth() / Game.Width;
		this.showEventName(eventName, (Game.Width-250)/2+250-50,(int) (Game.Height/2-2*(h+25)+10+(0.5*h)), -1);
		for(int i=0; i<3; ++i) {
			String text, signal;
			switch (i) {
			case 0:
				text = selection1;
				signal = "Select score: lesson";
				break;
			case 1:
				text = selection2;
				signal = "Select score: club";
				break;
			default:
				text = selection3;
				signal = "Select score: love";
				break;
			}
			if(text.equals("")) {
				if(this.selections[i] != null) this.selections[i] = null;
				continue;
			}
			this.selections[i] = new ClickButton((Game.Width-250)/2+250, Game.Height/2+(i-1)*(h+25)+50, w, h, sc, text, signal);
			this.add(selections[i]);
		}
	}
	public void deleteSelections() {
		for(int i=0; i<3; ++i) {
			if(selections[i] == null) continue;
			this.remove(selections[i]);
			selections[i] = null;
		}
		if(eventName == null) return;
		eventName.kill();
		eventName = null;
	}
	//set-get
	public Point getMouseLocation() {
		return this.mouse_pos;
	}
	//var
	private GraphicItem cellNameBg, eventName, cellName;
	private ClickButton rollingButton;
	private ClickButton[] selections;
	private ClickButton[] dieSelections;
	private Point mouse_pos;
	private JButton MusicButton;
	private ImageIcon image;
}
