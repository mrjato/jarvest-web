package es.uvigo.ei.sing.jarvest.web.zk.vm;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import es.uvigo.ei.sing.jarvest.web.dao.HibernateUtils;
import es.uvigo.ei.sing.jarvest.web.dao.UserDAO;
import es.uvigo.ei.sing.jarvest.web.entities.User;

public class UserViewModel {
	public static final String USER_SESSION_KEY = "user";
	
	private String login = "";
	private String password = "";
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public static User getCurrentUser() {
		final Session current = Sessions.getCurrent(false);
		
		if (current != null && current.hasAttribute(USER_SESSION_KEY)) {
			final String login = (String) current.getAttribute(USER_SESSION_KEY);
			
			final UserDAO user = new UserDAO(HibernateUtils.currentSession());
			return user.get(login);
		}
		
		return null;
	}
	
	public static void closeSession() {
		final Session current = Sessions.getCurrent(false);
		
		if (current != null && current.hasAttribute(USER_SESSION_KEY)) {
			current.removeAttribute(USER_SESSION_KEY);
			
			Executions.sendRedirect("index.zul");
		}
	}
	
	@Command
	public void checkLogin() {
		UserDAO dao = 
			new UserDAO(HibernateUtils.currentSession());
		
		if (dao.checkLogin(this.login, this.password)) {
			Executions.sendRedirect("main.zul");
			Sessions.getCurrent(true).setAttribute(
				UserViewModel.USER_SESSION_KEY, login
			);
		}
	}
}
