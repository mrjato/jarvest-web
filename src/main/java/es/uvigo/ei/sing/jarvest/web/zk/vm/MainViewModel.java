package es.uvigo.ei.sing.jarvest.web.zk.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import es.uvigo.ei.sing.jarvest.RobotProperties;
import es.uvigo.ei.sing.jarvest.dsl.Jarvest;
import es.uvigo.ei.sing.jarvest.web.dao.HibernateUtils;
import es.uvigo.ei.sing.jarvest.web.dao.RobotDAO;
import es.uvigo.ei.sing.jarvest.web.entities.Robot;
import es.uvigo.ei.sing.jarvest.web.entities.User;
import es.uvigo.ei.sing.jarvest.web.zk.initiators.ExecutorServiceManager;

public class MainViewModel {
	public static final String QUEUE_NAME = "jarvestweb";
	
	private Robot robot = new Robot();
	private boolean isRunning = false;
	private String inputs = "";
	private StringBuffer outputSB = new StringBuffer();

	public Robot getRobot() {
		return robot;
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
	}
	
	public String getInput() {
		return inputs;
	}

	public void setInput(String input) {
		this.inputs = input;
	}
	
	public String getOutput() {
		return this.outputSB.toString();
	}
	
	public List<String> getRobotNames() {
		return RobotProperties.getRobotNames();
	}

	public List<Robot> getRobots() {
		final User user = UserViewModel.getCurrentUser();
		final List<Robot> robots = new ArrayList<Robot>(user.getRobots());
		
		Collections.sort(robots, new Comparator<Robot>() {
			public int compare(Robot o1, Robot o2) {
				final int cmpName = o1.getName().compareTo(o2.getName());
				
				return cmpName == 0 ? o1.getId().compareTo(o2.getId()) : cmpName;
			}
		});
		
		return robots;
	}
	
	public List<String> getBranchNames() {
		return RobotProperties.getBranchNames();
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	@Command
	public void closeSession() {
		UserViewModel.closeSession();
	}
	
	@DependsOn("robot")
	public String getRobotURL() {
		final Robot robot = this.getRobot();
		
		if (robot.getId() == null) {
			return "";
		} else if (robot.isPublicAccess()) {
			final Execution execution = Executions.getCurrent();
			
			return String.format("%s://%s:%d/JarvestWeb/api/execute/robot/%d",
				execution.getScheme(),
				execution.getServerName(),
				execution.getServerPort(),
				robot.getId()
			);
		} else {
			final Execution execution = Executions.getCurrent();
			
			return String.format("%s://%s:%d/JarvestWeb/api/execute/robot/%s/%d",
				execution.getScheme(),
				execution.getServerName(),
				execution.getServerPort(),
				robot.getUser().getApiKey(),
				robot.getId()
			);
		}
	}
	
	@DependsOn("robot")
	public boolean isValid() {
		final String robot = this.getRobot().getRobot();
		final Jarvest jarvest = new Jarvest();
		
		if (robot != null && !robot.trim().isEmpty()) {
			try {
				return jarvest.eval(robot) != null;
			} catch (Exception e) {}
		}
		
		return false;
	}
	
	@Command
	@NotifyChange({"robot", "valid"})
	public void validateRobot() {
		this.isValid();
	}
	
	@Command
	@NotifyChange("robot")
	public void newRobot() {
		this.setRobot(new Robot());
	}
	
	@Command
	@NotifyChange({"robots", "robotURL"})
	public void persistRobot() {
		final RobotDAO robotDAO = 
			new RobotDAO(HibernateUtils.currentSession());
		final Robot robot = this.getRobot();
		
		if (robot.getId() == null) {
			final User user = UserViewModel.getCurrentUser();
			
			robot.setUser(user);
			robotDAO.create(robot);
		} else {
			robotDAO.update(robot);
		}
	}
	
	@Command
	@NotifyChange({"robot"})
	public void refreshRobot() {
		if (this.getRobot().getId() == null) {
			this.setRobot(new Robot());
		} else {
			final RobotDAO robotDAO =
				new RobotDAO(HibernateUtils.currentSession());
			
			this.setRobot(robotDAO.get(this.getRobot().getId()));
		}
	}
	
	@Command
	@NotifyChange("output")
	public void clearOutput() {
		this.outputSB = new StringBuffer();
	}
	
	@Command
	@NotifyChange("running")
	public void launchRobot() {
		this.clearOutput();
		
		final String[] inputs =	(this.getInput() == null || this.getInput().trim().isEmpty())?
			new String[0] : this.inputs.split("\n");
		
		final Desktop desktop = Executions.getCurrent().getDesktop();
		desktop.enableServerPush(true);
		final ExecutionTask task = 
			new ExecutionTask(this.getRobot(), inputs, desktop, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getName().equals(OutputEvent.EVENT_ON_OUTPUT)) {
						MainViewModel.this.outputSB.append(((OutputEvent) event).getOutput());
						
						BindUtils.postNotifyChange(MainViewModel.QUEUE_NAME, null, MainViewModel.this, "output");
					} else if (event.getName().equals(FinishEvent.EVENT_ON_FINISH)) {
						MainViewModel.this.isRunning = false;
						
						BindUtils.postNotifyChange(MainViewModel.QUEUE_NAME, null, MainViewModel.this, "running");
					}
				}
			});
		
		ExecutorServiceManager.getExecutor().execute(task);
	}
	
	private class ExecutionTask implements Runnable {
		private final Robot robot;
		private final String[] inputs;
		private final Desktop desktop;
		private final EventListener<Event> eventListener;
		
		public ExecutionTask(
			Robot robot, 
			String[] inputs,
			Desktop desktop,
			EventListener<Event> eventListener
		) {
			this.robot = robot;
			this.inputs = inputs;
			this.desktop = desktop;
			this.eventListener = eventListener;
		}

		@Override
		public void run() {
			try {
				final Jarvest jarvest = new Jarvest();
			
				for (String result : jarvest.exec(this.robot.getRobot(), this.inputs)) {
					Executions.schedule(
						this.desktop, 
						this.eventListener, 
						new OutputEvent(result)
					);
				}
			} finally {
				Executions.schedule(
					this.desktop, 
					this.eventListener, 
					new FinishEvent()
				);
			}
		}
	}
}
