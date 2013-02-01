package es.uvigo.ei.sing.jarvest.web.dao;

import org.hibernate.Session;

import es.uvigo.ei.sing.jarvest.web.entities.Robot;
import es.uvigo.ei.sing.jarvest.web.entities.User;

public class RobotDAO extends HibernateDAO<Integer, Robot> {
	public RobotDAO() {
		super();
	}

	public RobotDAO(Session session) {
		super(session);
	}

	public Robot createRobot(
		User user,
		String name,
		String description,
		String robot,
		boolean isPublic
	) throws DAOException {
		final Robot robotEntity = new Robot(
			user, name, description, robot, isPublic
		);
		this.create(robotEntity);
		this.getSession().flush();
		
		return robotEntity;
	}
	
	public Robot createRobot(
		User user,
		String name,
		String description,
		String robot,
		String contentType,
		boolean isPublic
	) throws DAOException {
		final Robot robotEntity = new Robot(
			user, name, description, robot, contentType, isPublic
		);
		this.create(robotEntity);
		this.getSession().flush();
		
		return robotEntity;
	}
}
