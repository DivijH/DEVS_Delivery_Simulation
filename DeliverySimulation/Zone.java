package Component.DeliverySimulation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import GenCol.Queue;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/**
 * Class representing the Zone
 * 
 * @author Divij and Arit
 */
public class Zone extends ViewableAtomic {
	
	// Ports
	public static final String DELIVERY_PHASE = "delivery";
	public static final String LEAVING_PHASE = "leaving";
	public static final String WAITING_PHASE = "active";
	
	// Zone Ports
	public static final String ZONE_INPUT = "in";
	public static final String ZONE_OUTPUT = "out";
	public static final String ZONE_RED_INPUT = "r_in";
	public static final String ZONE_GREEN_INPUT = "g_in";
	public static final String ZONE_BLUE_INPUT = "b_in";
	public static final String ZONE_YELLOW_INPUT = "y_in";
	public static final String ZONE_WARE_INPUT = "w_in";
	public static final String ZONE_RED_OUTPUT = "r_out";
	public static final String ZONE_GREEN_OUTPUT = "g_out";
	public static final String ZONE_BLUE_OUTPUT = "b_out";
	public static final String ZONE_YELLOW_OUTPUT = "y_out";
	public static final String ZONE_WARE_OUTPUT = "w_out";
	
	// Variables containing information about the Zone
	protected double averageCost, minTime = Double.MAX_VALUE;
	protected Truck minTruck;
	protected int type;
	protected String minPhase;
	protected HashMap<Truck, Double> trucksForDelivery;
	protected HashMap<Truck, Double> trucksGoingOut;
	
	/**
	 * Default Constructor for the Zone
	 */
	public Zone() {
		this("Red Zone", 1, 2);
	}
	
	/**
	 * Parametrized Constructor for the Zone
	 * @param name Name of the Zone
	 * @param type Type of the Zone
	 * @param averageCost Average cost of the delivery in the zone
	 */
	public Zone(String name, int type, double averageCost) {
		super(name);
		
		this.type = type;
		this.averageCost = averageCost;
		
		// Adding Input Ports to the Zone
		addInport(ZONE_WARE_INPUT);
		addInport(ZONE_RED_INPUT);
		addInport(ZONE_BLUE_INPUT);
		addInport(ZONE_YELLOW_INPUT);
		addInport(ZONE_GREEN_INPUT);
		
		// Adding Output Ports to the Zone
		addOutport(ZONE_WARE_OUTPUT);
		addOutport(ZONE_OUTPUT);
		addOutport(ZONE_RED_OUTPUT);
		addOutport(ZONE_BLUE_OUTPUT);
		addOutport(ZONE_YELLOW_OUTPUT);
		addOutport(ZONE_GREEN_OUTPUT);
		
		
		// Mapping : Red:1, Green:2, Blue: 3, Yellow: 4
		switch(type) {
		case 1:
			setBackgroundColor(Color.RED);
			break;
		case 2:
			setBackgroundColor(Color.GREEN);
			break;
		case 3:
			setBackgroundColor(Color.CYAN);
			break;
		case 4:
			setBackgroundColor(Color.YELLOW);
			break;
		}
		initialize();
	}
	
	/**
	 * Function responsible for initializing the Zone
	 */
	public void initialize() {
		sigma = INFINITY;
		phase = WAITING_PHASE;
		trucksForDelivery = new HashMap<>();
		trucksGoingOut= new HashMap<>();
	}
	
	/**
	 * Delta External function for the Zone
	 */
	public void deltext(double e, message x) {
		Continue(e);
		for(int i=0;i<x.getLength(); i++) {
			Truck t ;
			if(messageOnPort(x,ZONE_WARE_INPUT, i)) {
				 t = (Truck)x.getValOnPort(ZONE_WARE_INPUT, i);
			} else if(messageOnPort(x,ZONE_RED_INPUT, i)) {
				 t = (Truck)x.getValOnPort(ZONE_RED_INPUT, i);
			} else if(messageOnPort(x,ZONE_BLUE_INPUT, i)) {
				 t = (Truck)x.getValOnPort(ZONE_BLUE_INPUT, i);
			} else if(messageOnPort(x,ZONE_YELLOW_INPUT, i)) {
				 t = (Truck)x.getValOnPort(ZONE_YELLOW_INPUT, i);
			} else{
				 t = (Truck)x.getValOnPort(ZONE_GREEN_INPUT, i);
			}
			for(Map.Entry<Truck, Double> map : trucksForDelivery.entrySet()) {
				Truck t1 = map.getKey();
				double time = map.getValue();
				trucksForDelivery.put(t1, time-e);
			}
			for(Map.Entry<Truck, Double> map : trucksGoingOut.entrySet()) {
				Truck t1 = map.getKey();
				double time = map.getValue();
				trucksForDelivery.put(t1, time-e);
			}
	        trucksForDelivery.put(t, (int)t.getPackages(type) * averageCost);
	        calcMinimum();
		}
		holdIn(minPhase, minTime);
	}
	
	/**
	 * Function responsible for calculating the minimum truck in the zone
	 */
	public void calcMinimum() {
		if(!trucksGoingOut.isEmpty()) {
			for(Map.Entry<Truck, Double> map : trucksGoingOut.entrySet()) {
				Truck t = map.getKey();
				double time = map.getValue();
				if(minTime >= time) {
					minTime = time;
					minTruck = t;
					minPhase = LEAVING_PHASE;
				}
			}
		}
		if(!trucksForDelivery.isEmpty()) {
			for(Map.Entry<Truck, Double> map : trucksForDelivery.entrySet()) {
				Truck t = map.getKey();
				double time = map.getValue();
				if(minTime >= time) {
					minTime = time;
					minTruck = t;
					minPhase = DELIVERY_PHASE;
				}
			}
		}
	}
	
	/**
	 * Function responsible for updating times of the truck in the zone
	 */
	public void updateTimes() {
		for(Map.Entry<Truck, Double> map : trucksForDelivery.entrySet()) {
			Truck t1 = map.getKey();
			double time = map.getValue();
			trucksForDelivery.put(t1, time-minTime);
		}
		for(Map.Entry<Truck, Double> map : trucksGoingOut.entrySet()) {
			Truck t1 = map.getKey();
			double time = map.getValue();
			trucksGoingOut.put(t1, time-minTime);
		}
	}
	
	/**
	 * Delta Internal for the Zone
	 */
	public void deltint() {
		if(phaseIs(LEAVING_PHASE) || phaseIs(DELIVERY_PHASE)) {
			updateTimes();
			if(phaseIs(DELIVERY_PHASE)) {
				trucksForDelivery.remove(minTruck);
				minTruck.visitedZone(type);
				Queue<Integer> path = minTruck.getTruckPath();
				if(path.size()==0)
					trucksGoingOut.put(minTruck, AreaMap.distanceMatrix[type][0]);
				else
				trucksGoingOut.put(minTruck, AreaMap.distanceMatrix[type][path.first()]);
			} else {
				trucksGoingOut.remove(minTruck);
			}
			
			if(!trucksGoingOut.isEmpty() || !trucksForDelivery.isEmpty()) {
				this.minTime = Double.POSITIVE_INFINITY;
				calcMinimum();
				holdIn(minPhase,minTime);
			} else {
				holdIn(WAITING_PHASE, INFINITY);
			}
		} else {
			holdIn(WAITING_PHASE, INFINITY);
		}
	}
	
	/**
	 * Function responsible for sending the message out of the Zone
	 */
	public message out() {
		message m = new message();
		if(phaseIs(LEAVING_PHASE)) {
			Truck t = minTruck;
            int portVal = 0;
            if(t.getTruckPath().size()!= 0)
            	portVal = t.getTruckPath().first();
            content con = null;
            
            //Mapping => Warehouse:0, Red:1, Green:2, Blue: 3, Yellow: 4
            switch(portVal) {
            case 0:
            	con = makeContent(ZONE_WARE_OUTPUT,t);
            	System.out.println("Ware Out:" + t.getName());
            	break;
            case 1:
            	con = makeContent(ZONE_RED_OUTPUT,t);
            	System.out.println("Red Out:" + t.getName());
            	break;
            case 2:
            	con = makeContent(ZONE_GREEN_OUTPUT,t);
            	System.out.println("G Out:" + t.getName());
            	break;
            case 3:
            	con = makeContent(ZONE_BLUE_OUTPUT,t);
            	System.out.println("B Out:" + t.getName());
            	break;
            case 4:
            	con = makeContent(ZONE_YELLOW_OUTPUT,t);
            	System.out.println("Y Out:" + t.getName());
            	break;
            }
            m.add(con);
		} else {
			content con = makeContent(ZONE_OUTPUT, new entity("F" + minTruck.getName() + ":Z" + Integer.toString(type) + ":C" + Double.toString(averageCost)));
			m.add(con);
		}
		return m;
	}
	
	/**
	 * Function responsible for getting the information of the Zone during the simulation
	 */
	public String getTooltipText() {
		return super.getTooltipText() + "\nDelivery Trucks: " +trucksForDelivery.size() + "\n" + trucksForDelivery
		+ "\nLeaving Trucks: "+trucksGoingOut.size()+"\n"+trucksGoingOut;
	}	
}
