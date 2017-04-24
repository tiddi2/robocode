package robot;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import robocode.*;
import robocode.util.Utils;
import robot.Skynet2.enemies.AdvancedEnemyBot;

public class Skynet2 extends AdvancedRobot {
	private double firepower = 3;
	private byte moveDirection = 1;
	private enemies fiender;
	private RobotStatus robotStatus;
	private byte isChanged = 0;
	
	
	//Variabler for radar
	private double skannRetning;
	private Object target;
	private AdvancedEnemyBot activeTarget;

	private stats stat = new stats();
	public class stats {
		private double totalFirepower = 0;
		private double hitrate = 0;
		private double bulletsFired = 0;
		private double bullethits = 0;
		private int collision = 0;
		private int hitwall = 0;
		
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
		
		public AdvancedEnemyBot findClosest(AdvancedEnemyBot activeTarget){
			
			//Hvis den ikke har noe activeTarget, finn den nærmeste
			if(activeTarget == null){
				return findClosest();
			}
			
			//Hvis den har ett target, sjekk om det er verdt å bytte, dete forekommer om fienden er nærmere enn 50 pixler i forhold til active target
			for(Map.Entry<String, AdvancedEnemyBot> entry : fiendeHashMap.entrySet()) {
			    AdvancedEnemyBot value = entry.getValue();
			    if(value.getDistance() < activeTarget.getDistance()-50){
			    	return value;
			    }
			}
			
			return activeTarget;
		}
		
		
		//Metode som finner den nærmeste fienden
		public AdvancedEnemyBot findClosest(){ 
		      double currentClosest = Double.POSITIVE_INFINITY; 
		      AdvancedEnemyBot currentClosestEnemy = null; 
		       
		      for(Map.Entry<String, AdvancedEnemyBot> entry : fiendeHashMap.entrySet()) { 
		          AdvancedEnemyBot value = entry.getValue(); 
		          if(value.getDistance() < currentClosest){ 
		            currentClosest = value.getDistance(); 
		            currentClosestEnemy = value; 
		          } 
		      } 
		      return currentClosestEnemy; 
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
			
			@Override
			public String toString() {
				return name+  "[x=" + x + ", y=" + y + ", radarDouble=" + radarDouble + ", bearing=" + bearing
						+ ", distance=" + distance + ", energy=" + energy + ", heading=" + heading + ", velocity="
						+ velocity + "]";
			}

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
			doGun(activeTarget);
			doMove();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		
		//Sjekk om vi blir skutt mot
		//http://robowiki.net/wiki/Dodging_Bullets
		
		String name = e.getName();
		
		if(fiender.getBotByName(name) != null){
			fiender.getBotByName(name).update(e, this);
		}
		else{
			fiender.insertBot(e);
			fiender.getBotByName(name).update(e, this);
		}
				
		activeTarget = fiender.findClosest(activeTarget);
		//sjekk om det er verdt å bytte active target
		
		
		
		
	    if ((name == target || target == null) && fiender.getFiendeHashMap().size() == getOthers()) {
	    	skannRetning = Utils.normalRelativeAngle(fiender.getFiendeHashMap().values().iterator().next().getRadarDouble() - getRadarHeadingRadians());
	    	target = fiender.getFiendeHashMap().keySet().iterator().next();
	    }
	}
	public void onRoundEnded(RoundEndedEvent event) {
		stat.updateHitrate();
		out.println("Hitrate: " + stat.getHitrate());
		out.println("avg firepower: " + stat.getTotalFirepower()/stat.getBulletsFired());
		out.println("Bullets Fired: " + stat.getBulletsFired());
		out.println("Bullets hit: " + stat.getBullethits());
		out.println("Collision: " + stat.getCollision()); 
		out.println("Wall collisions: " + stat.getCollision());
		out.println("runde over");
	
	}
	public void onRobotDeath(RobotDeathEvent e) {
		
		//Fjerner den døde motstanderen fra fiendeHashMap
		fiender.getFiendeHashMap().remove(e.getName());
		target = null;
		
		//Hvis activeTarget var den som døde, sett activetarget til null
		if(e.getName() == activeTarget.getName()){
			activeTarget = null;
		}
	}
	
	public void doGun(AdvancedEnemyBot activeTarget) {
		if(activeTarget == null){
			return;
		}
		
		// calculate firepower based on distance
		firepower = Math.min(800 / activeTarget.getDistance(), 3);
		// calculate speed of bullet
		double bulletSpeed = 20 - firepower * 3;
		// distance = rate * time, solved for time
		long time = (long)(activeTarget.getDistance() / bulletSpeed);

		// calculate gun turn to predicted x,y location
		double futureX = activeTarget.getFutureX(time);
		double futureY = activeTarget.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		// turn the gun to the predicted x,y location
		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
		//Stå på tvers av target sånn at det er enklest å dodge
		setTurnRight(activeTarget.getBearing() + 90);
		
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
			setFire(firepower);
		}
	}
	
	public void doMove() {
		double x = getX();
		double y= getY();
		
		if (y <= 70 || y >= 530 || x <= 70 || x >= 730) {

			if (isChanged == 0) {

				moveDirection *= -1;
				isChanged = 1;
			}

		} else {
			isChanged = 0;
		}
		setAhead(50 * moveDirection);
	}
	
	public void moveToPoint(double x, double y){
		//retingen man peker
		double heading = (fitInRange(robotStatus.getHeading(), 360, 0, 0, 360) + 90) % 360;
		out.println("Heading: " + heading);
		
		out.println("X: " + getX() + " Y: " + getY());
		
		//Vektoren til målet
		double dx = x - getX();
		double dy = y - getY();
		
		//vinkelen man vil peke
		//double wantedHeading = fitInRange(Math.toDegrees(Math.atan2(dx, dy)),-180,180,0,360) + 180 % 360;
		double wantedHeading = Math.toDegrees(Math.atan2(dx, dy));
		out.println("wantedheading: " + wantedHeading);
		
		
		//roter riktig vinkel 
		double movAngle = wantedHeading - heading % 180;
		if(movAngle != 0){
			out.println("movAngle: " + movAngle);
			turnLeft(movAngle);

		}
		
		//Beveg lengde
		double length = Math.sqrt(dx*dx+dy*dy);
		out.println("length: " + length);
		//ahead(length);
	}
	
	public double fitInRange(final double valueIn, final double baseMin, final double baseMax, final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }
	
	public void doRadar(){
		//Beveger radaren maksimalt i retningen bestemt av skannRetning
		setTurnRadarRightRadians(skannRetning * Double.POSITIVE_INFINITY);
        scan();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		//snur
		moveDirection *= -1;
	}
	
	public void onHitWall(HitWallEvent e) {
		//oppdaterer statistikk
		stat.addCollision();
		//snur
		moveDirection *= -1;
	}
	
	public void onBulletHit(BulletHitEvent event) {
		//oppdaterer statistikk
		stat.addBullethits();
		stat.addBulletsFired();
		stat.addTotalFirepower(firepower);

	}

	public void onBulletHitBullet(BulletHitBulletEvent event) {
		//oppdaterer statistikk
		stat.addBulletsFired();
		stat.addTotalFirepower(firepower);
	}
	public void onBulletMissed(BulletMissedEvent event) {
		//oppdaterer statistikk
		stat.addBulletsFired();
		stat.addTotalFirepower(firepower);
	}
	
	public double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	public void onStatus(StatusEvent e){
		robotStatus = e.getStatus();
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
	
	public void onBattleEnded(BattleEndedEvent e)
	{
	    
	}
	
	public void onHitRobot(HitRobotEvent e) {
		//oppdaterer statistikk
		stat.addCollision();
	}

}
