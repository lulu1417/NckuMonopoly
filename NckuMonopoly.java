import java.awt.Point;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ingame.Cell;
import ingame.Game;
import ingame.GameState;
import ingame.GraphicImgItem;
import ingame.GraphicItem;
import ingame.GraphicTextItem;
import ingame.Player;
import ingame.Sound;

public class NckuMonopoly {

	//main
	public static void main(String[] args) {
		NckuMonopoly game = new NckuMonopoly();
	}

	//ctor
	public NckuMonopoly() {
		mainW = new MainWindow();
		music = new Music();
		sound = new Sound();
		this.setGameState(GameState.START);
		this.steppedScore = 0;
		this.nextMoveNoEvent = false;
		music.playMusic("bgm.wav");
		music.check(musicOff);
		//thread
		while(true) {
			
			//timer
			for(int i=0; i<Game.graphicItems.size(); ++i) Game.graphicItems.get(i).timerRun();
			
			//receive signals
			for(String signal: Game.signals) {
				if(signal.equals("Button clicked: Music")){
					if(!musicOff) musicOff = true;
					else musicOff = false;
					music.check(musicOff);
				}
				System.out.println("Got signal: "+signal);
				switch (Game.gamestate) {
				case START:
					if(signal.equals("Button clicked: Start")) this.start();
					break;
				case ROLLING:
					if(signal.equals("Button clicked: Roll")) {
						mainW.getPlayingPanel().deleteRollingButton();
						this.tickStart();
					}
					break;
				case EVENT:
					if(signal.startsWith("Button clicked: Select score:")) {
						mainW.getPlayingPanel().deleteSelections();
						Random rng = new Random();
						if(this.steppedScore>0)
							sound.playSound("addPoints.wav", soundOff);
						else sound.playSound("deduct.wav", soundOff);
						if(signal.endsWith("lesson")) {
							currentPlayer.addLesson(this.steppedScore);
						} else if(signal.endsWith("club")) {
							currentPlayer.addClub(this.steppedScore);
						} else {
							currentPlayer.addLove(this.steppedScore);
						}
						this.tickStart(21);
					} else if(signal.startsWith("Button clicked: Select die point:")) {
						mainW.getPlayingPanel().deleteDieSelections();
						String diePointStr = signal.substring("Button clicked: Select die point: ".length());
						int fateDiePoint = Integer.valueOf(diePointStr);
						this.rollingNum = fateDiePoint;
						this.tickStart(111);
					}
					break;
				case FATE:
					try {
						Scanner scanner = new Scanner(signal);
						String fateIdentifier = scanner.next() + " " + scanner.next();
						if(fateIdentifier.equals("Fate ended:")) {
							int fateType = scanner.nextInt();
							int fatePoint = scanner.nextInt();
							if(fatePoint>0)
								sound.playSound("addPoints.wav", soundOff);
							else sound.playSound("deduct.wav", soundOff);
							switch (fateType) {
							case 1:
								currentPlayer.addLesson(fatePoint);
								break;
							case 2:
								currentPlayer.addClub(fatePoint);
								break;
							default:
								currentPlayer.addLove(fatePoint);
								break;
							}
							this.setGameState(GameState.EVENT);
							this.tickStart(21);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
				}
			}
			Game.signals.clear();

			//mouse
			if(Game.gamestate != GameState.START && Game.gamestate != GameState.END && Game.gamestate != GameState.FATE) {
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
							GraphicItem die = new GraphicImgItem((Game.Width-250)/2+120, Game.Height/2, 100, 100, dieImg, Game.graphicItems);
							sound.playSound("roll.wav", soundOff);
							die.setLifeTime(i==times-1 ? 50 : standardTickAdd);
							if(i == times-1) { //last time
								//die number hint
								String hintString = "擲出點數：" + newNum;
								GraphicItem hint = new GraphicTextItem((Game.Width-250)/2+120, Game.Height/2-80, 30, hintString, Game.graphicItems);
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
						sound.playSound("move.wav", soundOff);
						if(this.rollingNum>0) this.currentPlayer.moveToNext();
						if(--this.rollingNum<=0) this.setGameState(GameState.EVENT);
						this.tickStart();
					}
				}
				break;
			case EVENT:
				if(this.nextMoveNoEvent) { //event won't be triggered in this move
					this.nextMoveNoEvent = false;
					tickStart(21);
				}
				if(ticking) {
					if(++tick==20) {
						tickPause();
						Cell steppedCell = currentPlayer.getCurrentCell();
						switch(steppedCell.getCellType()) {
							case NOTHING: //no event
								sound.playSound("nothing.wav", soundOff);
								mainW.getPlayingPanel().showEventName("這裡什麼都沒有...", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
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
								if(score>0)
									sound.playSound("goodluck.wav", soundOff);
								else sound.playSound("badluck.wav", soundOff);
								for(int i=0;i<selections.length;++i)
									if(!selections[i].equals(""))
										selections[i] = selections[i] + (score>0?"+":"") + score;
								mainW.getPlayingPanel().createSelections(steppedCell.getMessage(),selections[0],selections[1],selections[2]);
								break;
							case START: //start event
								sound.playSound("goodluck.wav", soundOff);
								mainW.getPlayingPanel().showEventName("回到起點，獲得一百元", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
								currentPlayer.addMoney(100);
								sound.playSound("addMoney.wav", soundOff);
								tickStart(21);
								break;
							case CHANCE: //chance
								int chanceCount = 7;
								Random rng = new Random();
								int chanceNum = rng.nextInt(chanceCount);
								switch (chanceNum) {
								case 0: {
									sound.playSound("badluck.wav", soundOff);
									mainW.getPlayingPanel().showEventName("機會：Java翹課，課業減半", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
									currentPlayer.addLesson(-currentPlayer.getLesson()/2);
									sound.playSound("deduct.wav", soundOff);
									tickStart(21);
								} break;
								case 1: {
									sound.playSound("goodluck.wav", soundOff);
									mainW.getPlayingPanel().showEventName("機會：對中發票，獲得200元", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
									currentPlayer.addMoney(200);
									sound.playSound("addMoney.wav", soundOff);
									tickStart(21);
								} break;
								case 2: {
									sound.playSound("goodluck.wav", soundOff);
									mainW.getPlayingPanel().showEventName("　機會：撿到50元", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
									currentPlayer.addMoney(50);
									sound.playSound("addMoney.wav", soundOff);
									tickStart(21);
								} break;
								case 3: {
									String chanceSelections[] = {"課業","社團","愛情"};
									sound.playSound("badluck.wav", soundOff);
									int chanceScore = -10;
									this.steppedScore = chanceScore;
									for(int i=0;i<chanceSelections.length;++i)
										if(!chanceSelections[i].equals(""))
											chanceSelections[i] = chanceSelections[i] + chanceScore;
									mainW.getPlayingPanel().createSelections("機會：衣服穿反",chanceSelections[0],chanceSelections[1],chanceSelections[2]);
									sound.playSound("deduct.wav", soundOff);
								} break;
								case 4: {
									sound.playSound("goodluck.wav", soundOff);
									mainW.getPlayingPanel().showEventName("機會：考上研究所，學業+50", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
									currentPlayer.addLesson(50);
									sound.playSound("addPoints.wav", soundOff);
									tickStart(21);
								} break;
								case 5: {
									sound.playSound("goodluck.wav", soundOff);
									mainW.getPlayingPanel().createDieSelections("機會：搭乘台南Uber，自由選擇步數前進");
								} break;
								case 6: {
									mainW.getPlayingPanel().showEventName("機會：沒注意行人號誌，發生車禍", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
									sound.playSound("badluck.wav", soundOff);
									tickStart(131);
								} break;
								default:
									break;
								}
								break;
							case FATE: //fate event
								mainW.getPlayingPanel().showEventName("命運：" + steppedCell.getMessage(), (Game.Width-250)/2+250-50, Game.Height/2-100, 60);
								tickStart(51);
								break;
							default: //shop event
								mainW.getPlayingPanel().showEventName("掛急診住院", (Game.Width-250)/2+250-50, Game.Height/2-100, 90);
								sound.playSound("badluck.wav", soundOff);
								tickStart(21);
								break;
						}
					} else if(tick==50) { //next player's rolling state
						int id = this.currentPlayer.getID();
						if(++id>=Game.playerCount) id=0;
						this.currentPlayer = Game.players.get(id);
						this.setGameState(GameState.ROLLING);
						this.tickPause();
						mainW.getPlayingPanel().createRollingButton();
					} else if(tick==110) { //fate
						Cell steppedCell = currentPlayer.getCurrentCell();
						//this.setGameState(GameState.FATE, steppedCell.getScore());
					} else if(tick==112) { //chance: choose die
						//die img
						String dieImg = "/die" + this.rollingNum + ".png";
						GraphicItem die = new GraphicImgItem((Game.Width-250)/2+250, Game.Height/2, 100, 100, dieImg, Game.graphicItems);
						die.setLifeTime(50);
						//die number hint
						String hintString = "選擇點數：" + this.rollingNum;
						GraphicItem hint = new GraphicTextItem((Game.Width-250)/2+250, Game.Height/2-80, 30, hintString, Game.graphicItems);
						hint.setLifeTime(50);
						//change state
						this.nextMoveNoEvent = true;
						this.setGameState(GameState.MOVING);
						this.tickStart(-30);
					} else if(tick==160) { //fate
						currentPlayer.moveTo(21);
						this.rollingNum = 0;
						this.setGameState(GameState.MOVING);
						this.tickStart(-30);
					}
				}
				break;
			case FATE:
				/*FatePanel fatePanel = mainW.getFatePanel();
				fatePanel.doTick();
				fatePanel.repaint();*/
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
				player.createScoreBoard(1300, 30+(3-i)*600, "/scoreboard"+ (i+1) +".png", Game.graphicItems);
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
		//roll button
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
		this.setGameState(gamestate, 0);
	}
	public void setGameState(GameState gamestate, int fateType) {
		//debug
		System.out.println("Gamestate changed: "
						+ Game.gamestate.toString()
						+ " to " +
						gamestate.toString());
		//main window
		try {
			mainW.changePanel(gamestate, fateType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Game.gamestate = gamestate;
	}
	//var
	private MainWindow mainW;
	private Music music;
	private Sound sound;
	private long tick;
	private boolean ticking, nextMoveNoEvent, musicOff, soundOff;
	private Player currentPlayer;
	private int rollingNum, steppedScore;
}
