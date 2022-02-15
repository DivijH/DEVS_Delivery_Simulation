package Component.DeliverySimulation;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/**
 * Generator for Delivery Simulation
 * 
 * @author Divij and Arit
 */
public class PackageGenerator extends ViewableAtomic {
	
	// Variable represents the index of the input to be sent
	private int index;
	
	// Variables containing the information about the Delivery Center
	/*
	 * Mapping : Red:1, Green:2, Blue: 3, Yellow: 4
	 */
	private final int[] no_of_trucks = {5};
	private final int[] cost_of_trucks = {100};
	private final int[][][] items_in_trucks = {
			{{3, 1}, {4, 1}, {2}, {1}, {3}}
	};
	
	// Ports
	private static final String OUT_PORT = "out";
	private static final String START_PORT = "start";
	private static final String STOP_PORT = "stop";
	
	/**
	 * Default constructor for Driving Simulation
	 */
	public PackageGenerator() {
		this("Generator");
	}
	
	/**
	 * Parameterized constructor for Driving Simulation
	 * @param name The name of the Generator
	 */
	public PackageGenerator(String name) {
		super(name);
		
		addOutport(OUT_PORT);
		addInport(STOP_PORT);
		addInport(START_PORT);
		
		addTestInput(START_PORT, new entity(""));
		addTestInput(STOP_PORT, new entity(""));
		
		index = 0;
	}
	
	/**
	 * Function to initialize the Generator
	 */
	public void initialize() {
		holdIn("active", 0);
		super.initialize();
	}
	
	/**
	 * Delta External for Generator
	 * @param e Elapsed Time for a message
	 * @param msg The message to be sent
	 */
	public void deltext(double e, message msg) {
		Continue(e);
		
		// Starting the Generator
		if(phaseIs("passive")) {
			for(int i=0; i<msg.getLength(); i++) {
				if(messageOnPort(msg, START_PORT, i)) {
					holdIn("active", INFINITY);
				}
			}
		}
		
		// Sending next value
		if(phaseIs("active")) {
			for(int i=0; i<msg.getLength(); i++) {
				if(messageOnPort(msg, START_PORT, i)) {
					String msgVal = msg.getValOnPort(START_PORT, 0).toString();
					if(msgVal.equals("next")) {
						holdIn("active", 0);
					}
				}
			}
		}		
		
		// Stopping the Generator
		if(phaseIs("active")) {
			for(int i=0; i<msg.getLength(); i++) {
				if(messageOnPort(msg, STOP_PORT, i)) {
					holdIn("passive", INFINITY);
				}
			}
		}
	}
	
	/**
	 * Delta Internal for Generator
	 */
	public void deltint() {
		if(phaseIs("active")) {
			holdIn("active", INFINITY);
			index = (index+1) % no_of_trucks.length;
		} else {
			passivate();
		}
	}
	
	/**
	 * Function responsible for generating the message
	 */
	public message out() {
		message m = new message();
		deliveryInput inp = new deliveryInput(no_of_trucks[index], cost_of_trucks[index], items_in_trucks[index]);
		content con = makeContent(OUT_PORT, inp);
		m.add(con);
		return m;
	}
}
