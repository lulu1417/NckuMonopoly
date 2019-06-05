import java.awt.Dimension;
import ingame.GraphicItem;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ingame.Game;
import ingame.GraphicImgItem;
import ingame.GraphicItem;
public class StartPanel extends MainPanel{ 
	//ctor
		public StartPanel(Dimension dim) {
			super(dim);
			//start button
			{
				int w = 250, h = 100;
				double sc = (double)this.getWidth() / Game.Width;
				//GraphicImgItem startBackGround = new GraphicImgItem(Game.Width/2, Game.Height/2, Game.Width, Game.Height, "/startbg.jpg", Game.graphicItems);
				ClickButton startButton = new ClickButton(Game.Width/2, Game.Height/2, w, h, sc, "¶}©l¹CÀ¸","Start"); 
				this.add(startButton);
				
			}
		}
		//var
}
