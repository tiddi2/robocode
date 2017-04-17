package robot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.LinkedHashMap;

import robocode.*;
import robocode.util.Utils;

public class Skynet2 extends AdvancedRobot implements Serializable {
	private double firepower = 3;
	private double totalFirepower = 0; //skal flyttes over til stats
	private double hitrate = 0; //skal flyttes over til stats 
	private double bulletsFired = 0; //skal flyttes over til stats
	private double bullethits = 0; //skal flyttes over til stats
	private int collision = 0;//skal flyttes over til stats
	private byte scanDirection = 1;
	private int hitwall = 0; //skal flyttes over til stats
	private byte moveDirection = 1;
	
	private enemies fiender;
	
	
	//Variabler for radar
	private double skannRetning;
	private Object target;
	 
	
	public static class stats {
		private double firepower = 3;
		private double totalFirepower = 0;
		private double hitrate = 0;
		private double bulletsFired = 0;
		private double bullethits = 0;
		private int collision = 0;
		private int hitwall = 0;

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

		//VI m� velge farger gutter
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
		
		//Radar greier som jeg m� l�re meg
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
	
	public void onRobotDeath(RobotDeathEvent e) {
		
		//Fjerner den d�de motstanderen fra fiendeHashMap
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
		//St� p� tvers av target s�nn at det er enklest � dodge
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
		hitwall++;
		moveDirection *= -1;
	}
	
	public void onBulletHit(BulletHitEvent event) {
		bullethits++;
		bulletsFired++;
		totalFirepower += firepower;
		hitrate = bullethits/bulletsFired;

	}

	public void onBulletHitBullet(BulletHitBulletEvent event) {
		bulletsFired++;
		totalFirepower += firepower;
		hitrate = bullethits/bulletsFired;
	}
	public void onBulletMissed(BulletMissedEvent event) {
		bulletsFired++;
		totalFirepower += firepower;
		if(bullethits != 0)
			hitrate = bulletsFired / bullethits;
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
		out.println("Hitrate: " + hitrate);
		out.println("avg firepower: " + totalFirepower/bulletsFired);
		out.println("Bullets Fired: " + bulletsFired);
		out.println("Bullets hit: " + bullethits);
		out.println("Collision: " + collision); 
		out.println("Wall collisions: " + hitwall);
	}
	
	public void onHitRobot(HitRobotEvent e) {
		collision++;
	}

}
