package es.maps.scoreboard.app;

import java.io.Serializable;

public class Basket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum teams {HOME, VISITOR};
	
	private teams team;
	private int period;
	private int player;
	private int points;
	private int home;
	private int visitor;

	
	public teams getTeam() {
		return team;
	}

	public int getPeriod() {
		return period;
	}

	public int getPlayer() {
		return player;
	}

	public int getPoints() {
		return points;
	}

	public int getVisitor() {
		return visitor;
	}

	public void setVisitor(int visitor) {
		this.visitor = visitor;
	}

	public int getHome() {
		return home;
	}

	public void setHome(int home) {
		this.home = home;
	}

	
	public Basket()
	{
		this.team = teams.HOME;
		this.period = 0;
		this.player = 0;
		this.points = 0;
		this.home = 0;
		this.visitor = 0;
	}

	public Basket(teams team, int period, int player, int points, int home, int visitor)
	{
		this.team = team;
		this.period = period;
		this.player = player;
		this.points = points;
		this.home = home;
		this.visitor = visitor;
	}

}
