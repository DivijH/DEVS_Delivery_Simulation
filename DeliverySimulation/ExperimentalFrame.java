package Component.DeliverySimulation;

import java.awt.Dimension;
import java.awt.Point;

import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

/**
 * Class representing the experimental frame containing the Generator and Transducer
 * 
 * @author Divij and Arit
 */
public class ExperimentalFrame extends ViewableDigraph {
	
	// Ports
	private static final String START = "start";
	private static final String STOP = "stop";
	private static final String IN = "in";
	private static final String G_OUT = "generator_out";
	private static final String T_OUT = "transducer_out";
	
	/**
	 * Default Constructor for the Experimental Frame
	 */
	public ExperimentalFrame() {
		super("Experimental Frame");
		
		ViewableAtomic generator = new PackageGenerator();
		ViewableAtomic transducer = new Transducer();
		
		add(generator);
		add(transducer);
		
		// Adding ports of Coupled Model
		addInport(START);
		addInport(STOP);
		addInport(IN);
		addOutport(G_OUT);
		addOutport(T_OUT);
		
		// Connection of Coupled model to Generator
		addCoupling(this, START, generator, START);
		addCoupling(this, STOP, generator, STOP);
		
		// Connection of Generator to Coupled Model
		addCoupling(generator, "out", this, G_OUT);
		
		// Connection of Coupled model to Transducer
		addCoupling(this, IN, transducer, IN);
		
		// Connection of Transducer to Coupled model
		//addCoupling(transducer, "out", this, T_OUT);
		addCoupling(transducer, "avgTime", this, T_OUT);
		addCoupling(transducer, "cost", this, T_OUT);
		addCoupling(transducer, "out", generator, START);
		
		// Connection of Generator to Transducer
		addCoupling(generator, "out", transducer, IN);
		
	}
	
    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimView() {
        preferredSize = new Dimension(560, 109);
        ((ViewableComponent)withName("Transducer")).setPreferredLocation(new Point(214, 35));
        ((ViewableComponent)withName("Generator")).setPreferredLocation(new Point(9, 35));
    }
}
