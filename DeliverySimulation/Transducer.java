package Component.DeliverySimulation;

import java.util.HashMap;
import java.util.Map;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/**
 * Class representing the Transducer for the model
 * 
 * @author Divij and Arit
 */
public class Transducer extends ViewableAtomic {
	
	// TruckId : {PackageId : Quantity}
	private Map<Integer,Map<Integer,Integer>> packages;
	private double no_of_trucks, cost_of_truck, travel_cost, avg_time, total_packages=0;
	
	// Ports
	private final static String AVG_TIME = "avgTime";
	private final static String COST = "cost";
	private final static String OUT = "out";
	private final static String IN = "in";
	
	// Variables containing the information about the Transducer
	private double clock;
	private double observation_time;
	
	/**
	 * Default Constructor for the Transducer
	 */
	public Transducer() {
		this("Transducer", 200);
	}
	
	/**
	 * Parameterized Constructor for the Transducer
	 * @param name String containing the name of the Transducer
	 * @param observation_time Time to which to observe the input
	 */
	public Transducer(String name, double observation_time) {
		super(name);
		this.observation_time = observation_time;
		packages = new HashMap<>();
		
		addOutport(AVG_TIME);
		addOutport(COST);
		addOutport(OUT);
		addInport(IN);
		
		addTestInput(IN, new entity(""));
		addTestInput(OUT, new entity(""));
		
		initialize();
	}
	
	/**
	 * Function to initialize the Transducer
	 */
	public void initialize() {
		phase = "active";
		clock = 0;
		sigma = observation_time;
		super.initialize();
	}
	
	/**
	 * Delta External for the Transducer
	 * @param e Elapsed Time for the message
	 * @param msg The message to be sent
	 */
	public void deltext(double e, message msg) {
		clock+=e;
		Continue(e);
		String val = null;
		for(int i=0; i<msg.size(); i++) {
			val = msg.getValOnPort(IN, i).toString();
		}
		
		// Input for Travel cost
		if(val.charAt(0) == 'C') {
			travel_cost = Double.parseDouble(val.substring(1));
		}
		// Input for Package Delivery
		else if(val.charAt(0) == 'F') {
			String[] str = val.split(":");
			int truck_name = Integer.parseInt(str[0].substring(7))+1;
			int zone = Integer.parseInt(str[1].substring(1));
			double avg_cost = Double.parseDouble(str[2].substring(1));
			avg_time = avg_time + packages.get(truck_name).get(zone) * (2*clock - (packages.get(truck_name).get(zone)-1)*avg_cost)/2;
		}
		// Input from Generator
		else {
			String[] str = val.split(":");
			no_of_trucks = Integer.parseInt(str[0]);
			cost_of_truck = Integer.parseInt(str[1]);
			String[] all_packages = str[2].split(";");
			for(int i=0; i<no_of_trucks; i++) {
				Map<Integer, Integer> temp = new HashMap<>();
				String[] truck_packages = all_packages[i].split(",");
				for(int j=0; j<truck_packages.length; j++) {
					total_packages++;
					if(temp.containsKey(Integer.parseInt(truck_packages[j]))) {
						temp.put(Integer.parseInt(truck_packages[j]), temp.get(Integer.parseInt(truck_packages[j]))+1);
					} else {
						temp.put(Integer.parseInt(truck_packages[j]), 1);
					}
				}
				packages.put(i+1, temp);
			}
		}
	}
	
	/**
	 * Delta Internal for the Transducer
	 */
	public void deltint() {
		clock+=sigma;
		initialize();
	}
	
	/**
	 * Function responsible for generating the message
	 */
	public message out() {
		message m = new message();
		content nextGenerator = makeContent(OUT, new entity("next"));
		content avgPackageTime = makeContent(AVG_TIME, new entity("Average Delivery Time : " + Double.toString(avg_time/total_packages)));
		content totalCost = makeContent(COST, new entity("Cost : " + Double.toString(no_of_trucks*cost_of_truck + travel_cost)));
		m.add(nextGenerator);
		m.add(avgPackageTime);
		m.add(totalCost);
		return m;
	}
	
	/**
	 * Function which displays information of transducer during simulation
	 */
	public String getTooltipText() {
		String text = "";
		text+= "Average Time: " + Double.toString(avg_time/total_packages) + "\nCost: " + Double.toString(no_of_trucks*cost_of_truck + travel_cost);
		return text;
	}
}
