import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import ingame.Cell;
import ingame.CellType;
import ingame.Game;
import ingame.GameState;
import ingame.GraphicImgItem;
import ingame.GraphicItem;
import ingame.GraphicTextItem;
import ingame.Player;

public class NckuMonopoly {

	//main
	public static void main(String[] args) {
		NckuMonopoly game = new NckuMonopoly();
	}

	//ctor
	public NckuMonopoly() {
		mainW = new MainWindow();
		this.setGameState(GameState.START);
		this.steppedScore = 0;
		//thread
		while(true) {
			//timer
			for(int i=0; i<Game.graphicItems.size(); ++i) Game.graphicItems.get(i).timerRun();
			//receive signals
			for(String signal: Game.signals) {
				System.out.println("Got signal: " + signal);
				switch (Game.gamestate) {
				case START:
					if(signal.equals("Button clicked: Start")){
						this.start();
					}
					break;
				case ROLLING:
					if(signal.equals("Button clicked: Roll")) {
						mainW.getPlayingPanel().deleteRollingButton();
						this.tickStart();
					}
					break;
				case EVENT:
					if(signal.startsWith("Button clicked: Select")) {
						mainW.getPlayingPanel().deleteSelections();
						Random rng = new Random();
						if(signal.endsWith("lesson")) {
							currentPlayer.addLesson(this.steppedScore);
						} else if(signal.endsWith("club")) {
							currentPlayer.addClub(this.steppedScore);
						} else {
							currentPlayer.addLove(this.steppedScore);
						}
						this.tickStart(21);
					}
					break;
				default:
				}
			}
			Game.signals.clear();

			//mouse
			if(Game.gamestate != GameState.START && Game.gamestate != GameState.END) {
				Point mouse_pos = mainW.getPlayingPanel().getMouseLocation();
				for(Cell cell_pos: Game.cells) {
					if((Math.abs(mouse_pos.getX() - cell_pos.getX()) < 50) && (Math.abs(mouse_pos.getY() - cell_pos.getY()) < 50)) {
						mainW.getPlayingPanel().showCellName(cell_pos, cell_pos.getName());
						break;
					}
				}
			}
			
			//tick execution
			switch (Game.gamestate) {
			case ROLLING:
				for(Player player: Game.players) {
					if(player == currentPlayer) player.select();
					else player.unselect();
				}
				if(ticking) {
					++tick;
					final int times = 26;
					int standardTick = 0;
					int standardTickAdd = 1;
					for(int i=0; i<times; ++i) {
						standardTick += standardTickAdd;
						if(i<times/2) standardTickAdd = 1;
						else standardTickAdd = i-times/2;
						if(tick == standardTick) {
							//random choose new die number
							Random rng = new Random();
							int newNum;
							if(this.rollingNum==0) newNum = rng.nextInt(6)+1;
							else {
								newNum = rng.nextInt(5)+1;
								if(newNum>=this.rollingNum) ++newNum;
							}
							this.rollingNum = newNum; //debug
							//die img
							String dieImg = "/die" + newNum + ".png";
							GraphicItem die = new GraphicImgItem((Game.Width-250)/2+130, Game.Height/2, 100, 100, dieImg, Game.graphicItems);
							die.setLifeTime(i==times-1 ? 50 : standardTickAdd);
							if(i == times-1) { //last time
								//die number hint
								String hintString = "擲出點數：" + newNum;
								GraphicItem hint = new GraphicTextItem((Game.Width-250)/2+140, Game.Height/2-100, 30, hintString, Game.graphicItems);
								hint.setLifeTime(50);
								//change state
								this.setGameState(GameState.MOVING);
								this.tickStart(-30);
							}
						} else if(tick < standardTick) break;
					}
				}
				break;
			case MOVING:
				if(ticking) {
					if(++tick>=20) {
						this.currentPlayer.moveToNext();
						if(--this.rollingNum<=0) this.setGameState(GameState.EVENT);
						this.tickStart();
					}
				}
				break;
			case EVENT:
				if(ticking) {
					if(++tick==20) {
						tickPause();
						Cell steppedCell = currentPlayer.getCurrentCell();
						switch(steppedCell.getCellType()) {
							case NOTHING: //no event
								mainW.getPlayingPanel().showEventName("　　　這裡什麼都沒有...", (Game.Width-250)/2+100, Game.Height/2-100, 90);
								tickStart(21);
								break;
							case SELECT: //select event
								String selections[];
								switch(steppedCell.getSelectPolicy()) {
									case THREE: {
										String selections_temp[] = {"課業", "社團", "愛情"};
										selections = selections_temp;
										} break;
									case LESSON: {
										String selections_temp[] = {"課業","",""}; 
										selections = selections_temp;
										} break;
									case CLUB: {
										String selections_temp[] = {"","社團",""}; 
										selections = selections_temp;
										} break;
									default: {
										String selections_temp[] = {"","","愛情"}; 
										selections = selections_temp;
										} break;
								}
								int score = steppedCell.getScore();
								this.steppedScore = score;
								for(int i=0;i<selections.length;++i)
									if(!selections[i].equals(""))
										selections[i] = selections[i] + (score>0?"+":"") + score;
								mainW.getPlayingPanel().createSelections(steppedCell.getMessage(),selections[0],selections[1],selections[2]);
								break;
							case START: //start event
								mainW.getPlayingPanel().showEventName("回到起點，獲得一百元", (Game.Width-250)/2+100, Game.Height/2-100, 90);
								currentPlayer.addMoney(100);
								tickStart(21);
								break;
							case CHANCE: //chance
								int chanceCount = 5;
								Random rng = new Random();
								int chanceNum = rng.nextInt(chanceCount);
								switch (chanceNum) {
								case 0: {
									mainW.getPlayingPanel().showEventName("　　Java翹課，課業減半", (Game.Width-250)/2+100, Game.Height/2-100, 90);
									currentPlayer.addLesson(-currentPlayer.getLesson()/2);
									tickStart(21);
								} break;
								case 1: {
									mainW.getPlayingPanel().showEventName("　　對中發票，獲得200元", (Game.Width-250)/2+100, Game.Height/2-100, 90);
									currentPlayer.addMoney(200);
									tickStart(21);
								} break;
								case 2: {
									mainW.getPlayingPanel().showEventName("　撿到50元", (Game.Width-250)/2+100, Game.Height/2-100, 90);
									currentPlayer.addMoney(50);
									tickStart(21);
								} break;
								case 3: {
									String chanceSelections[] = {"課業","社團","愛情"};
									int chanceScore = -10;
									this.steppedScore = chanceScore;
									for(int i=0;i<chanceSelections.length;++i)
										if(!chanceSelections[i].equals(""))
											chanceSelections[i] = chanceSelections[i] + chanceScore;
									mainW.getPlayingPanel().createSelections("衣服穿反",chanceSelections[0],chanceSelections[1],chanceSelections[2]);
								} break;
								case 4: {
									mainW.getPlayingPanel().showEventName("　　考上研究所，學業+50", (Game.Width-250)/2+100, Game.Height/2-100, 90);
									currentPlayer.addLesson(50);
									tickStart(21);
								} break;
								default:
									break;
								}
								break;
							default: //fate event, shop event
								mainW.getPlayingPanel().showEventName("此格尚未完成", (Game.Width-250)/2+100, Game.Height/2-100, 90);
								tickStart(21);
								break;
						}
					} else if(tick>=50) {
						int id = this.currentPlayer.getID();
						if(++id>=Game.playerCount) id=0;
						this.currentPlayer = Game.players.get(id);
						this.setGameState(GameState.ROLLING);
						this.tickPause();
						mainW.getPlayingPanel().createRollingButton();
					}
				}
				break;
			default:
				break;
			}

			//dead
			Iterator<GraphicItem> it = Game.graphicItems.iterator();
			while (it.hasNext()) {
			    GraphicItem graphicItem = it.next();
			    if(graphicItem.isDead()) it.remove();
			}
			
			//repaint
			mainW.repaint();
			
			//time out
			try {
				Thread.sleep(33); //30 fps
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//method
	public void start() {
		this.setGameState(GameState.ROLLING); //page
		//bg
		GraphicImgItem bg = new GraphicImgItem(Game.Width/2, Game.Height/2, Game.Width, Game.Height, "/bg.png", Game.graphicItems);
		//players
		for(int i=0; i<Game.playerCount; ++i) {
			String playerImg = "/player" + (i+1) + ".png";
			Player player = new Player(77, 120, playerImg, 100, 100, 100, 50, 0, i, Game.graphicItems);
			if(i<2)
				player.createScoreBoard(20, 30+i*600, "/scoreboard"+ (i+1) +".png", Game.graphicItems);
			else 
				player.createScoreBoard(1280, 30+(3-i)*600, "/scoreboard"+ (i+1) +".png", Game.graphicItems);
			if(i==0) this.currentPlayer = player;
			Game.players.add(player);
			if(i==Game.playerCount-1) {
				try {
					player.moveTo(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		mainW.getPlayingPanel().createRollingButton();
		//tick
		this.tickPause();
		//roll
		this.rollingNum = 0;
	}
	public void tickStart() {
		this.tickStart(0);
	}
	public void tickStart(int init_tick) {
		this.ticking = true;
		this.tick = init_tick;
	}
	public void tickPause() {
		this.ticking = false;
	}
	public void tickContinue() {
		this.ticking = true;
	}
	
	//get-set
	public void setGameState(GameState gamestate) {
		//debug
		System.out.println("Gamestate changed: "
						+ Game.gamestate.toString()
						+ " to " +
						gamestate.toString());
		//main window
		mainW.changePanel(gamestate);
		Game.gamestate = gamestate;
	}
	//var
	private MainWindow mainW;
	private long tick;
	private boolean ticking;
	private Player currentPlayer;
	private int rollingNum, steppedScore;
}