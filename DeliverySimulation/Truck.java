package Component.DeliverySimulation;

import java.util.HashMap;
import java.util.Map;

import GenCol.Queue;
import GenCol.entity;

/**
 * Class representing the Truck during delivery
 * 
 * @author Divij and Arit
 */
public class Truck extends entity {
	
	// Information about the Truck
	private String truckName;
	private Queue<Integer> truckPath;
	private HashMap<Integer, Integer> packages;
	
	/**
	 * Default constructor for the truck
	 */
	public Truck() {
		this("Default", null, null);
	}
	
	/**
	 * Parameterized constructor for the Truck
	 * @param name String containing the name of the truck
	 * @param path Queue containing the path the truck has to take
	 * @param items Map containing the packages to be delivered
	 */
	public Truck(String name, Queue<Integer> path, HashMap<Integer,Integer> items) {
		truckName = name;
		truckPath = path;
		packages = items;
	}
	
	/**
	 * Getter function for the name of the truck
	 * @return String containing the name of the truck
	 */
	public String getName() {
		return truckName;
	}
	
	/**
	 * Getter function for the Truck path
	 * @return Queue containing the path of the truck
	 */
	public Queue<Integer> getTruckPath() {
		return truckPath;
	}
	
	/**
	 * Function returning the packages for the zone
	 * @param zone Value of the zone
	 * @return Integer containing the packages for the zone
	 */
	public int getPackages(int zone) {
		return packages.get(zone);
	}
	
	/**
	 * Function updating the visited zones for the truck
	 * @param zone Integer containing the zone where the truck went
	 */
	public void visitedZone(int zone) {
		truckPath.remove();
		packages.remove(zone);
	}
	
	/**
	 * Function to represent the information about the truck in the String
	 * @return String containing the information about the Truck
	 */
	public String toString() {
		String name= "Name: " + this.getName() + "\n";
		String packages = "Packages : ";
	
		for(Map.Entry<Integer, Integer> map : this.packages.entrySet()) {
			int zone = map.getKey();
			int count = this.getPackages(zone);
			packages = packages+"Zone: "+zone+" Count: "+count+"\n";
		}
		packages += "\n";
		String path = "Path: ";
		
		for(int p: truckPath) {
			path = path+"==>"+p;
		}
		path+= "\n";
		return (name + packages + path);
	}
}
