package ingame;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class PlayerScoreboard extends GraphicImgItem{
	//ctor
	public PlayerScoreboard(int x, int y, String img_path, int lesson, int club, int love, int money, ArrayList<GraphicItem> itemList) {
		super(x,y,280,180,img_path, itemList);
		this.text = new GraphicTextItem[4];
		this.num = new GraphicTextItem[4];
		this.additions = new GraphicTextItem[4];
		for(int i=0; i<4; ++i) {
			int number;
			String title;
			switch (i) {
			case 0:
				title = "課業：";
				number = lesson;
				break;
			case 1:
				title = "社團：";
				number = club;
				break;
			case 2:
				title = "愛情：";
				number = love;
				break;
			default:
				title = "金錢：";
				number = money;
			}
			this.text[i] = new GraphicTextItem(x+65, y+30+37*i, fontSize, title, itemList);
			this.num[i] = new GraphicTextItem(x+155, y+30+37*i, fontSize, Integer.toString(number), itemList);
		}
		for(int i=0; i<this.additions.length; ++i) this.additions[i] = null;
	}
	//method
	@Override
	protected int drawX(double sc) {
		return (int) (rect.getX() * sc);
	}
	protected int drawY(double sc) {
		return (int) (rect.getY() * sc);
	}
	private void showAddition(int type, int add) {
		String message;
		Color color;
		if(add>0) {
			message = "+" + add;
			color = new Color(0x008040);
		} else if(add<0) {
			message = "-" + (-add);
			color = new Color(0xCA0000);
		}
		else return;
		int x = (int) this.rect.getX() + 245;
		int y = (int) this.rect.getY() + 30 + 37*type;
		//addition number
		if(this.additions[type] != null) this.additions[type].kill();
		this.additions[type] = new GraphicTextItem(x, y, fontSize, message, Game.graphicItems);
		this.additions[type].setLifeTime(90);
		this.additions[type].setColor(color);
	}
	public void showLessonAddition(int add) {
		this.showAddition(0, add);
	}
	public void showClubAddition(int add) {
		this.showAddition(1, add);
	}
	public void showLoveAddition(int add) {
		this.showAddition(2, add);
	}
	public void showMoneyAddition(int add) {
		this.showAddition(3, add);
	}
	//get-set
	public void setLesson(int lesson) {
		this.num[0].setText(Integer.toString(lesson));
	}
	public void setClub(int club) {
		this.num[1].setText(Integer.toString(club));
	}
	public void setLove(int love) {
		this.num[2].setText(Integer.toString(love));
	}
	public void setMoney(int money) {
		this.num[3].setText(Integer.toString(money));
	}
	public void select() {
		this.setOpacity(1.0);
		for(int i=0;i<Game.playerCount;++i) {
			this.text[i].setOpacity(1.0);
			this.num[i].setOpacity(1.0);
		}
	}
	public void unselect() {
		this.setOpacity(0.5);
		for(int i=0;i<Game.playerCount;++i) {
			this.text[i].setOpacity(0.5);
			this.num[i].setOpacity(0.5);
		}
	}
	//var
	private GraphicTextItem[] text;
	private GraphicTextItem[] num;
	private GraphicTextItem[] additions;
	public static final int fontSize = 30;
}