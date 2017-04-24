package robot;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

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
