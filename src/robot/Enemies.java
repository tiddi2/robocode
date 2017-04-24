package robot;

import java.util.LinkedHashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Enemies {

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

	public Enemies(){
		fiendeHashMap = new LinkedHashMap<String, AdvancedEnemyBot>(5, 2, true);
	}

	public AdvancedEnemyBot findClosest(AdvancedEnemyBot activeTarget){

		//Hvis den ikke har noe activeTarget, finn den n�rmeste
		if(activeTarget == null){
			return findClosest();
		}

		//Hvis den har ett target, sjekk om det er verdt � bytte, dete forekommer om fienden er n�rmere enn 50 pixler i forhold til active target
		for(Map.Entry<String, AdvancedEnemyBot> entry : fiendeHashMap.entrySet()) {
		    AdvancedEnemyBot value = entry.getValue();
		    if(value.getDistance() < activeTarget.getDistance()-50){
		    	return value;
		    }
		}

		return activeTarget;
	}	
	
	//Metode som finner den n�rmeste fienden
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
	

}
