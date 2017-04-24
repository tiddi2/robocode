package robot;

public class Stats {
	private double totalFirepower = 0;
	private double hitrate = 0;
	private double bulletsFired = 0;
	private double bullethits = 0;
	private int collision = 0;
	private int hitwall = 0;
	private double damageDone = 0;
	private double damageTaken = 0;

	public double getTotalFirepower() {
		return totalFirepower;
	}
	public void addTotalFirepower(double firepower) {
		this.totalFirepower += firepower;
	}
	public double getHitrate() {
		return hitrate;
	}
	public void updateHitrate() {
		this.hitrate = this.bullethits/this.bulletsFired;
	}
	public double getBulletsFired() {
		return bulletsFired;
	}
	public void addBulletsFired() {
		this.bulletsFired = this.bulletsFired+1;
	}
	public double getBullethits() {
		return bullethits;
	}
	public void addBullethits() {
		this.bullethits = this.bullethits+1;
	}
	public int getCollision() {
		return collision;
	}
	public void addCollision() {
		this.collision = this.collision+1;
	}
	public int getHitwall() {
		return hitwall;
	}
	public void setHitwall() {
		this.hitwall = this.hitwall+1;
	}
	public double getDamageTaken() {
		return damageTaken;
	}
	public void addDamageTaken(double damageTaken) {
		this.damageTaken += damageTaken;
	}
	public double getDamageDone() {
		return damageDone;
	}
	public void addDamageDone(double damageDone) {
		this.damageDone += damageDone;
	}

}
