package es.uvigo.ei.sing.jarvest.web.zk.composers;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.GlobalCommandEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.ui.select.annotation.Subscribe;
import org.zkoss.zul.Textbox;

import es.uvigo.ei.sing.jarvest.web.zk.vm.MainViewModel;

public class SourceCodeComposer extends SelectorComposer<Textbox> {
	private static final long serialVersionUID = 1L;

	private final static Map<String, String> ROBOT_CODE = new HashMap<String, String>();
	private final static Map<String, Integer> ROBOT_OFFSET = new HashMap<String, Integer>();
	
	static {
		ROBOT_CODE.put("wget", "wget(:userAgent => '', :ajax => 'false', :headers => '')");
		ROBOT_OFFSET.put("wget", 20);
	}
	
	@Wire
	private Textbox txtSourceCode;
	
	private int position;
	
	@Override
	public void doAfterCompose(Textbox comp) throws Exception {
		super.doAfterCompose(comp);

		this.txtSourceCode.setWidgetListener(
			"onClick", 
			"zAu.send(new zk.Event(this, \"onMyClick\", zk(this.$n()).getSelectionRange()[1]));"
		);
		
		this.txtSourceCode.setWidgetListener(
			"onKeyUp",
			"zAu.send(new zk.Event(this, \"onMyKey\", zk(this.$n()).getSelectionRange()[1]));"
		);
		
		final EventListener<Event> updatePositionEL = new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				position = Integer.parseInt(event.getData().toString());
			}
		};
		
		this.txtSourceCode.addEventListener("onMyClick", updatePositionEL);
		this.txtSourceCode.addEventListener("onMyKey", updatePositionEL);
		
//		this.txtSourceCode.addEventListener(Events.ON_CHANGING, new EventListener<InputEvent>() {
//			@Override
//			public void onEvent(InputEvent event) throws Exception {
//				System.out.println("onChanging " + event.getStart());
//				position = event.getStart();
//			}
//		});
	}
	
	@Subscribe(MainViewModel.QUEUE_NAME)
	public void updateSourceCode(Event event) {
		if (event instanceof GlobalCommandEvent) {
			final GlobalCommandEvent gcEvent = (GlobalCommandEvent) event;
			
			if ("addRobotCode".equals(gcEvent.getCommand())) {
				final String robot = (String) gcEvent.getArgs().get("robot");
				
				final StringBuilder sb = new StringBuilder(this.txtSourceCode.getText());
				sb.insert(this.position, ROBOT_CODE.get(robot));
				this.txtSourceCode.setText(sb.toString());
				
				this.position += ROBOT_OFFSET.get(robot);
				this.txtSourceCode.setSelectionRange(this.position, this.position);
			}
		}
	}
}
