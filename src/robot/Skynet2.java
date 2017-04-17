package robot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import robocode.*;
import robocode.util.Utils;

public class Skynet2 extends AdvancedRobot implements Serializable {
	private double firepower = 3;
	private byte scanDirection = 1;
	private byte moveDirection = 1;
	private enemies fiender;
	
	
	//Variabler for radar
	private double skannRetning;
	private Object target;
	 

	private stats stat = new stats();
	private static ArrayList<stats> stats = new ArrayList<stats>();

	public class stats {
		private double firepower = 3;
		private double totalFirepower = 0;
		private double hitrate = 0;
		private double bulletsFired = 0;
		private double bullethits = 0;
		private int collision = 0;
		private int hitwall = 0;
		
		public stats() {
			stats.add(this);
		}
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

	}
	public class fileWriter {
		private stats stats;

		public fileWriter(stats stats) {
			try {
				FileOutputStream outStream = new FileOutputStream(getClass().getResource("stats.ser").getPath());
				ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
				objOutStream.writeObject(this.stats);
				objOutStream.close();
				outStream.close();
			} catch (FileNotFoundException e) {
				out.println("Filen finnes ikke");
			} catch (IOException e) {
				out.println("Kan ikke åpne objektstrøm til fil");
				
			}
		}

	}
	
	public class enemies {
		
		private LinkedHashMap<String, AdvancedEnemyBot> fiendeHashMap;
		
		public LinkedHashMap<String, AdvancedEnemyBot> getFiendeHashMap(){
			return fiendeHashMap;
		}
		
		public AdvancedEnemyBot getBotByName(String name){
			return fiendeHashMap.get(name);
		}
		
		public void insertBot(ScannedRobotEvent e){
			fiendeHashMap.put(e.getName(), new AdvancedEnemyBot());
		}
		
		public enemies(){
			fiendeHashMap = new LinkedHashMap<String, AdvancedEnemyBot>(5, 2, true);
		}
		
		public class EnemyBot {
			double bearing;
			double distance;
			double energy;
			double heading;
			double velocity;
			String name;
					
			public double getBearing(){
				return bearing;		
			}
			public double getDistance(){
				return distance;
			}
			public double getEnergy(){
				return energy;
			}
			public double getHeading(){
				return heading;
			}
			public double getVelocity(){
				return velocity;
			}
			public String getName(){
				return name;
			}
			public void update(ScannedRobotEvent bot){
				bearing = bot.getBearing();
				distance = bot.getDistance();
				energy = bot.getEnergy();
				heading = bot.getHeading();
				velocity = bot.getVelocity();
				name = bot.getName();
			}
			public void reset(){
				bearing = 0.0;
				distance =0.0;
				energy= 0.0;
				heading =0.0;
				velocity = 0.0;
				name = null;
			}	
			public Boolean none(){
				if (name == null || name == "")
					return true;
				else
					return false;
			}	
			public EnemyBot(){
				reset();
			}
		}
		public class AdvancedEnemyBot extends EnemyBot{

			private double x, y, radarDouble;
			
			public double getX(){
				return x;
			}
			
			public double getY(){
				return y;
			}
			
			public double getRadarDouble(){
				return radarDouble;
			}
			
			public void setRadarDouble(double _radarDouble){
				radarDouble = _radarDouble;
			}
			
			public void reset(){
				super.reset();
				x = 0;
				y = 0;
			}
			
			public AdvancedEnemyBot(){
				reset();
			}
			
			public void update(ScannedRobotEvent e, AdvancedRobot robot){
				super.update(e);
				double absBearingDeg= (robot.getHeading() + e.getBearing());
				radarDouble = robot.getHeadingRadians() + e.getBearingRadians();
				if (absBearingDeg <0) absBearingDeg +=360;				
							
				x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
				y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
				
			}
			
			public double getFutureX(long when){
				return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
			}
			
			public double getFutureY(long when ){
				return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
			}
		}

	}
	
	public void run() {

		//VI må velge farger gutter
		setColors(Color.red,Color.blue,Color.white); // body,gun,radar
		
		fiender = new enemies();
		
		//Radar setup
		skannRetning = 1;
		
		while(true) {
			doRadar();
			//doGun();
			doMove();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		//Radar greier som jeg må lære meg
		String name = e.getName();
		
		if(fiender.getBotByName(name) != null){
			fiender.getBotByName(name).update(e, this);
		}
		else{
			fiender.insertBot(e);
			fiender.getBotByName(name).update(e, this);
		}
		
		
	 
	    if ((name == target || target == null) && fiender.getFiendeHashMap().size() == getOthers()) {
	    	skannRetning = Utils.normalRelativeAngle(fiender.getFiendeHashMap().values().iterator().next().getRadarDouble() - getRadarHeadingRadians());
	    	target = fiender.getFiendeHashMap().keySet().iterator().next();
	    }
	}
	public void onRoundEnded(RoundEndedEvent event) {
		out.println("runde over");
	}
	public void onRobotDeath(RobotDeathEvent e) {
		
		//Fjerner den døde motstanderen fra fiendeHashMap
		fiender.getFiendeHashMap().remove(e.getName());
		target = null;
	}
	
	/*public void doGun() {
		if (enemy.none())
			return;
		
		// calculate firepower based on distance
		firepower = Math.min(800 / enemy.getDistance(), 3);
		// calculate speed of bullet
		double bulletSpeed = 20 - firepower * 3;
		// distance = rate * time, solved for time
		long time = (long)(enemy.getDistance() / bulletSpeed);

		// calculate gun turn to predicted x,y location
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		// turn the gun to the predicted x,y location
		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
		//Stå på tvers av target sånn at det er enklest å dodge
		setTurnRight(enemy.getBearing() + 90);
		
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
			setFire(firepower);
		}
	}*/
	
	public void doMove() {			

	}
	
	public void doRadar(){
		//Beveger radaren maksimalt i retningen bestemt av skannRetning
		setTurnRadarRightRadians(skannRetning * Double.POSITIVE_INFINITY);
        scan();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		moveDirection *= -1;
	}
	
	public void onHitWall(HitWallEvent e) {
		stat.addCollision();
		moveDirection *= -1;
	}
	
	public void onBulletHit(BulletHitEvent event) {
		stat.addBullethits();
		stat.addBulletsFired();
		stat.addTotalFirepower(firepower);

	}

	public void onBulletHitBullet(BulletHitBulletEvent event) {
		stat.addBulletsFired();
		stat.addTotalFirepower(firepower);
	}
	public void onBulletMissed(BulletMissedEvent event) {
		stat.addBulletsFired();
		stat.addTotalFirepower(firepower);
	}
	
	public double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;
	
		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}
	
		return bearing;
	}
	
	public void onBattleEnded(BattleEndedEvent event) {
		stat.updateHitrate();
		out.println("Hitrate: " + stat.getHitrate());
		out.println("avg firepower: " + stat.getTotalFirepower()/stat.getBulletsFired());
		out.println("Bullets Fired: " + stat.getBulletsFired());
		out.println("Bullets hit: " + stat.getBullethits());
		out.println("Collision: " + stat.getCollision()); 
		out.println("Wall collisions: " + stat.getCollision()
);
	}
	
	public void onHitRobot(HitRobotEvent e) {
		stat.addCollision();
	}

}
