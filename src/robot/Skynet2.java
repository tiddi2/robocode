package robot;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;

public class Skynet2 extends AdvancedRobot {
	private double firepower = 3;
	private byte moveDirection = 1;
	private Enemies fiender;
	//private RobotStatus robotStatus;
	private byte isChanged = 0;
	//Variabler for radar
	private double skannRetning;
	private Object target;
	private AdvancedEnemyBot activeTarget;
	private int wallMargin = 100;
	private Stats stat = new Stats();
	

	public void run() {

		//VI mï¿½ velge farger gutter
		setColors(Color.red,Color.blue,Color.white); // body,gun,radar

		fiender = new Enemies();

		//Radar setup
		skannRetning = 1;

		while(true) {
			doRadar();
			doGun(activeTarget);
			doMove();
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {


		//Sjekk om vi blir skutt mot
		//http://robowiki.net/wiki/Dodging_Bullets
		if(e.getName() != null && activeTarget != null && e.getName() == activeTarget.getName() && e.getEnergy() < activeTarget.getEnergy()) { 
		      //TODO: Lag funksjonalitet som gjør at roboten unnviker skudde
			moveDirection *= -1;
			out.println("active Target skyter");
		 }
		 
		String name = e.getName();

		if(fiender.getBotByName(name) != null){
			fiender.getBotByName(name).update(e, this);
		}
		else{
			fiender.insertBot(e);
			fiender.getBotByName(name).update(e, this);
		}

		activeTarget = fiender.findClosest(activeTarget);
		//sjekk om det er verdt ï¿½ bytte active target


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

		//Fjerner den dï¿½de motstanderen fra fiendeHashMap
		fiender.getFiendeHashMap().remove(e.getName());
		target = null;
		
		//Hvis activeTarget var den som dï¿½de, sett activetarget til null
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
		//Står på tvers av target sånn at det er enklest å dodge
		setTurnRight(activeTarget.getBearing() + 90 + (30 * -moveDirection));
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
			setFire(firepower);
		}
	}	
	
	public void doMove() {	
		double x = getX(); 
		double y= getY(); 
		
		
		if (y <= wallMargin || y >= getBattleFieldHeight() - wallMargin || x <= wallMargin || x >= getBattleFieldWidth() - wallMargin) { 
			if (isChanged == 0) { 
				moveDirection *= -1;
				isChanged = 1; 
			}
		} 
		else { 
			isChanged = 0; 
		} 
		setAhead(50 * moveDirection); 
	}

	
	public void moveToPoint(int x, int y){
		if(Math.floor(getX()) == x && Math.floor(getY()) == y) {
	    	out.println("fremme");
	    	return;
	    }
		 double a;
		    setTurnRightRadians(Math.tan(
		        a = Math.atan2(x -= (int) getX(), y -= (int) getY()) 
		              - getHeadingRadians()));
		    setAhead(Math.hypot(x, y) * Math.cos(a));
		    out.println((int)Math.floor(getX()));
		    
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
		double power = e.getPower();
		stat.addDamageTaken(power * 4 + (power > 1? 2* (power -1): 0));
		
		//Velger å ikke snu, basert på statistikk
		//moveDirection *= -1;
	}

	public void onHitWall(HitWallEvent e) {
		//stat.addDamageTaken(power * 4 + (power > 1? 2* (power -1): 0));
		//oppdaterer statistikk
		stat.addCollision();
		out.println("au");
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
		//robotStatus = e.getStatus();
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
