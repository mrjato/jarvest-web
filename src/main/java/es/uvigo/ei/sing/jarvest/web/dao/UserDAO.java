package es.uvigo.ei.sing.jarvest.web.dao;

import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import es.uvigo.ei.sing.jarvest.web.entities.User;

public class UserDAO extends HibernateDAO<String, User> {
	public UserDAO() {
		super();
	}

	public UserDAO(Session session) {
		super(session);
	}

	public User createUser(
		String login,
		String password,
		String robot,
		boolean isPublic
	) throws DAOException {
		final String apiKey = UUID.randomUUID().toString();
		final User robotEntity = new User(
			login, password, apiKey
		);
		this.create(robotEntity);
		this.getSession().flush();
		
		return robotEntity;
	}
	
	public boolean checkLogin(String login, String password) {
		User user = this.get(login);
		
		return user != null && user.getPassword().equals(password); 
	}
	
	public User getUserByApiKey(String apiKey) {
		final Session session = this.getSession();
		
		return (User) session.createCriteria(User.class)
			.add(Restrictions.eq("apiKey", apiKey))
		.uniqueResult();
	}
}
