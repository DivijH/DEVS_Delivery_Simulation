package Component.DeliverySimulation;

/**
 * Class containing the matrix of cost connecting each Zone and Warehouse
 * 
 * @author Divij and Arit
 */
public class AreaMap {
	
	// Matrix containing the data
	public static final double[][] distanceMatrix = {
		{0,1,20,12,5},
		{1,0,10,32,15},
		{20,10,0,20,24},
		{12,32,20,0,1},
		{5,15,24,1,0}
	};
}
