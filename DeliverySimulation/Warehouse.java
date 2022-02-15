package Component.DeliverySimulation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;

import GenCol.Queue;
import GenCol.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/**
 * Class representing the model of Warehouse
 * 
 * @author Divij and Arit
 */
public class Warehouse extends ViewableAtomic{
	
	// Ports
	protected static final String PASSIVE_PHASE = "passive";
	protected static final String WAREHOUSE_INPUT = "in";
	protected static final String WAREHOUSE_RED_INPUT = "r_in";
	protected static final String WAREHOUSE_GREEN_INPUT = "g_in";
	protected static final String WAREHOUSE_YELLOW_INPUT = "y_in";
	protected static final String WAREHOUSE_BLUE_INPUT = "b_in";
	protected static final String WAREHOUSE_OUTPUT = "out";
	protected static final String WAREHOUSE_RED_OUTPUT = "r_out";
	protected static final String WAREHOUSE_GREEN_OUTPUT = "g_out";
	protected static final String WAREHOUSE_YELLOW_OUTPUT = "y_out";
	protected static final String WAREHOUSE_BLUE_OUTPUT = "b_out";
	
	// Variables containing information about the Warehouse
	protected HashMap<Truck, Double> output;
	protected double minTime = Double.POSITIVE_INFINITY;
	protected Truck minTruck;
	private double travel_cost;
	private boolean sent = true;
	
	/**
	 * Default Generator of the Warehouse
	 */
	public Warehouse() {
		this("Warehouse");
	}
	
	/**
	 * Parameterized constructor of the Warehouse
	 * @param name String containing the name of the Warehouse
	 */
	public Warehouse(String name) {
		super(name);
		
		// Adding the ports
		addInport(WAREHOUSE_INPUT);
		addInport(WAREHOUSE_RED_INPUT);
		addInport(WAREHOUSE_GREEN_INPUT);
		addInport(WAREHOUSE_YELLOW_INPUT);
		addInport(WAREHOUSE_BLUE_INPUT);
		addOutport(WAREHOUSE_OUTPUT);
		addOutport(WAREHOUSE_RED_OUTPUT);
		addOutport(WAREHOUSE_BLUE_OUTPUT);
		addOutport(WAREHOUSE_GREEN_OUTPUT);
		addOutport(WAREHOUSE_YELLOW_OUTPUT);
		
		// Setting the background color during the simulation
		setBackgroundColor(Color.GRAY);
	}
	
	/**
	 * Function responsible for initializing the model of Warehouse
	 */
	public void initialize() {
		
		super.initialize();
		sigma = INFINITY;
		phase = PASSIVE_PHASE;
		output = new HashMap<Truck,Double>();
		sent = true;
	}
	
	/**
	 * Function responsible for creating the trucks for the starting the delivery
	 * @param itemsInTruck Array containing the items in the truck
	 * @param j The ID of the truck
	 */
	public void createTrucks(int[] itemsInTruck, int j) {
		HashMap<Integer, Integer> itemsCount = new HashMap<>();
		for(int item: itemsInTruck) {
			if(itemsCount.containsKey(item)) {
				itemsCount.put(item, itemsCount.get(item)+1);
			} else {
				itemsCount.put(item, 1);
			}
		}
			
		Set<Integer> zones = itemsCount.keySet();
		Iterator<Integer> iter = zones.iterator();
		
		double[][] newDistanceMap = new double[zones.size()+1][zones.size()+1];
		newDistanceMap[0][0] = AreaMap.distanceMatrix[0][0];
		int k = 1;
		while(iter.hasNext()) {
			int z = iter.next();
			newDistanceMap[0][k] = AreaMap.distanceMatrix[0][z];
			newDistanceMap[k][0] = AreaMap.distanceMatrix[z][0];
			
			Iterator<Integer> iter1 = zones.iterator();
			int idx = 1;
			while(iter1.hasNext()) {
				int z1 = iter1.next();
				newDistanceMap[idx][k] = AreaMap.distanceMatrix[z][z1];
				newDistanceMap[k][idx] = AreaMap.distanceMatrix[z1][z];
				idx++;
			}
			k++;
		}

		TSPdp solver = new TSPdp(0, newDistanceMap);
		travel_cost = solver.getTourCost();
		List<Integer> path = solver.getTour();
		Queue<Integer> updatedPath = new Queue<>();
		
		Object[] zoneArray =zones.toArray();
		
		for(int z: path) {
			if(z==0) {
				updatedPath.add(0);
			} else {
				updatedPath.add((int)zoneArray[z-1]);
			}
		}
		
		//Removing 1st Element as it will be the Warehouse
		updatedPath.remove(0);
		Truck t = new Truck("Truck "+j , updatedPath, itemsCount);
		output.put(t, AreaMap.distanceMatrix[0][updatedPath.get(0)]);
	}
	
	/**
	 * Delta External for the Warehouse
	 */
	public void deltext(double e, message x) {
		Continue(e);
		for(int i=0; i<x.getLength(); i++) {
			if(messageOnPort(x, WAREHOUSE_INPUT, i)) {
				initialize();
				deliveryInput input = (deliveryInput)x.getValOnPort(WAREHOUSE_INPUT,i);
				int[][] itemsInTrucks = input.get_items_in_trucks();
				int j = 0;
				for(int[] itemsInTruck : itemsInTrucks) {
					createTrucks(itemsInTruck, j);
					j++;	
				}
				for(Map.Entry<Truck, Double> map : output.entrySet()){
					Truck t = map.getKey();
					double time = map.getValue();
					if(minTime >= time) {
						minTime = time;
						minTruck = t;
					}
				}
				holdIn("active",minTime);
			}
		}
	}
	
	/**
	 * Delta Internal for the Warehouse
	 */
	public void deltint() {
		if(phaseIs("active")) {	
			output.remove(minTruck);
			if(!output.isEmpty()) {
				for(Map.Entry<Truck, Double> map : output.entrySet()) {
					Truck t = map.getKey();
					double time = map.getValue();
					output.put(t, time-minTime);
				}
				minTime = Double.POSITIVE_INFINITY;
				for(Map.Entry<Truck, Double> map : output.entrySet()) {
					Truck t = map.getKey();
					double time = map.getValue();
					if(minTime >= time) {
						minTime = time;
						minTruck = t;
					}
				}				
				holdIn("active",minTime);
			} else {
				holdIn("active",INFINITY);
			}
		}		
	}
	
	/**
	 * Function responsible for sending out the message
	 */
	public message out() {
		message m = new message();
		if(phaseIs("active")) {
			Truck t = minTruck;
			int portVal = t.getTruckPath().first();
			content con = null;

			//Mapping : Red:1, Green:2, Blue: 3, Yellow: 4
			switch(portVal) {
			case 1:
				con = makeContent(WAREHOUSE_RED_OUTPUT,t);
				System.out.println("WRed Out:" + t.getName());
				break;
			case 2:
				con = makeContent(WAREHOUSE_GREEN_OUTPUT,t);
				System.out.println("WG Out:" + t.getName());
				break;
			case 3:
				con = makeContent(WAREHOUSE_BLUE_OUTPUT,t);
				System.out.println("WB Out:" + t.getName());
				break;
			case 4:
				con = makeContent(WAREHOUSE_YELLOW_OUTPUT,t);
				System.out.println("WY Out:" + t.getName());
				break;
            }
            m.add(con);
		}
		if(sent) {
			content con = makeContent(WAREHOUSE_OUTPUT, new entity("C"+Double.toString(travel_cost)));
			m.add(con);
			sent = false;
		}
		return m;
	}	
}