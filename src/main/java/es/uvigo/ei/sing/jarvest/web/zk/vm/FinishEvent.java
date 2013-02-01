package es.uvigo.ei.sing.jarvest.web.zk.vm;

import org.zkoss.zk.ui.event.Event;

public class FinishEvent extends Event {
	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_ON_FINISH = "onFinishEvent";

	
	public FinishEvent() {
		super(EVENT_ON_FINISH);
	}
}
