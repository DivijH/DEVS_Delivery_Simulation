package Component.DeliverySimulation;

import GenCol.entity;

/**
 * This class encapsulates the message to be sent to the Model by Generator
 * 
 * @author Divij and Arit
 */
public class deliveryInput extends entity {
	
	// Variables containing the information of the message to be sent
	private int no_of_truck;
	private int cost_of_truck;
	private int[][] items_in_trucks;
	
	/**
	 * Default constructor of the class
	 */
	public deliveryInput() {
		this(0, 0, null);
	}
	
	/**
	 * Parameterized Constructor of the class
	 * @param no_of_trucks Contains the number of trucks that are available that day
	 * @param cost_of_truck Contains the cost of each truck
	 * @param items_in_trucks Contains the array of items for each truck
	 * @param sigma 
	 */
	public deliveryInput(int no_of_truck, int cost_of_truck, int[][] items_in_trucks) {
		this.no_of_truck = no_of_truck;
		this.cost_of_truck = cost_of_truck;
		this.items_in_trucks = items_in_trucks;
	}
	
	/**
	 * Function to convert the input to String
	 * @return The string containing the information for the input
	 */
	private String convert_to_string() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<items_in_trucks.length; i++) {
			for(int j=0; j<items_in_trucks[i].length; j++) {
				str.append(items_in_trucks[i][j]);
				if(j!=items_in_trucks[i].length-1) {
					str.append(",");
				}
			}
			if(i!=items_in_trucks.length-1) {
				str.append(";");
			}
		}
		return str.toString();
	}
	
	/**
	 * Overrides the default toString() method to display the relevant information
	 * @return String containing the information to be displayed
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(no_of_truck);
		str.append(":");
		str.append(cost_of_truck);
		str.append(":");
		str.append(convert_to_string());
		return str.toString();
	}
	
	/**
	 * Getter function for no_of_truck
	 * @return The number of trucks available
	 */
	public int get_no_of_truck() {
		return this.no_of_truck;
	}
	
	/**
	 * Getter function for cost_of_truck
	 * @return The cost of each truck
	 */
	public int get_cost_of_truck() {
		return this.cost_of_truck;
	}
	
	/**
	 * Getter function for items_in_trucks
	 * @return The array containing the items to be delivered by each truck
	 */
	public int[][] get_items_in_trucks() {
		return this.items_in_trucks;
	}
}
