package es.uvigo.ei.sing.jarvest.web.zk.composers;

import org.zkoss.bind.GlobalCommandEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Textbox;

import es.uvigo.ei.sing.jarvest.RobotProperties;
import es.uvigo.ei.sing.jarvest.web.zk.vm.MainViewModel;

public class SourceCodeComposer extends SelectorComposer<Textbox> {
	private static final long serialVersionUID = 1L;
	
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
		
		EventQueues.lookup(MainViewModel.QUEUE_NAME).subscribe(new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				SourceCodeComposer.this.updateSourceCode(event);
			}
		});
	}
	
//	@Subscribe(MainViewModel.QUEUE_NAME)
	public void updateSourceCode(Event event) {
		if (event instanceof GlobalCommandEvent) {
			final GlobalCommandEvent gcEvent = (GlobalCommandEvent) event;
			
			if ("addRobotCode".equals(gcEvent.getCommand())) {
				final String robot = (String) gcEvent.getArgs().get("robot");
				final RobotProperties.RobotInfo props = RobotProperties.getRobotInfo(robot);
				
				final StringBuilder sb = new StringBuilder(this.txtSourceCode.getText());
				sb.insert(this.position, props.getTemplate());
				this.txtSourceCode.setText(sb.toString());
				
				this.position += props.getOffset();
				this.txtSourceCode.setSelectionRange(this.position, this.position);
			} else if ("addBranchCode".equals(gcEvent.getCommand())) {
				final String branch = (String) gcEvent.getArgs().get("branch");
				final RobotProperties.BranchInfo props = RobotProperties.getBranchInfo(branch);
				
				final StringBuilder sb = new StringBuilder(this.txtSourceCode.getText());
				sb.insert(this.position, props.getTemplate());
				this.txtSourceCode.setText(sb.toString());
				
				this.position += props.getOffset();
				this.txtSourceCode.setSelectionRange(this.position, this.position);
			}
		}
	}
}
