    
package ingame;

import java.awt.Point;

public class Cell extends Point{
	//ctor
	public Cell(String name, int x, int y, CellType cellType) {
		super(x,y);
		this.name = name;
		this.cellType = cellType;
		this.score = 0;
		this.message = "";
		this.selectPolicy = PlayerScoreType.THREE;
	}
	//get-set
	public CellType getCellType() {
		return this.cellType;
	}
	public Cell setScore(int score) {
		this.score = score;
		return this;
	}
	public Cell setMessage(String message) {
		this.message = message;
		return this;
	}
	public Cell setSelectPolicy(PlayerScoreType selectPolicy) {
		this.selectPolicy = selectPolicy;
		return this;
	}
	public int getScore() {
		return this.score;
	}
	public String getName() {
		return this.name;
	}
	public String getMessage() {
		return this.message;
	}
	public PlayerScoreType getSelectPolicy() {
		return this.selectPolicy;
	}
	//var
	private String name, message;
	private CellType cellType;
	private int score;
	private PlayerScoreType selectPolicy;
}
