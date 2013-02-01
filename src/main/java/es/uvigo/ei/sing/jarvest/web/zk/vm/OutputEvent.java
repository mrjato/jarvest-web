package es.uvigo.ei.sing.jarvest.web.zk.vm;

import org.zkoss.zk.ui.event.Event;

public class OutputEvent extends Event {
	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_ON_OUTPUT = "onOutputEvent";

	private final String output;
	
	public OutputEvent(String output) {
		super(EVENT_ON_OUTPUT);
		
		this.output = output;
	}
	
	public String getOutput() {
		return output;
	}
}
