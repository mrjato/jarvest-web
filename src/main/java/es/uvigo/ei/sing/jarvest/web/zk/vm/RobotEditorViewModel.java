package es.uvigo.ei.sing.jarvest.web.zk.vm;

import org.zkoss.bind.annotation.Command;

import es.uvigo.ei.sing.jarvest.web.dao.HibernateUtils;
import es.uvigo.ei.sing.jarvest.web.dao.RobotDAO;
import es.uvigo.ei.sing.jarvest.web.entities.Robot;


public class RobotEditorViewModel {
	private Robot robot;

	public Robot getRobot() {
		return robot;
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
	}
	
	@Command
	public void persistRobot() {
		final RobotDAO robotDAO = 
			new RobotDAO(HibernateUtils.currentSession());
		
		if (robot.getId() == null) {
			robotDAO.create(this.getRobot());
		} else {
			robotDAO.update(this.getRobot());
		}
	}
}
