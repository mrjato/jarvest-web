package es.uvigo.ei.sing.jarvest.web.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity(name = "User")
public class User {
	@Id
	@Column
	private String login;
	
	@Column
	private String password;
	
	@Column
	private String apiKey;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "userId")
	private Set<Robot> robots;
	
	public User() {}
	
	public User(String login, String password, String apiKey) {
		super();
		this.login = login;
		this.password = password;
		this.apiKey = apiKey;
		this.robots = new HashSet<Robot>();
	}
	
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
	
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Set<Robot> getRobots() {
		return Collections.unmodifiableSet(robots);
	}
	
	public boolean addRobot(Robot robot) {
		if (this.robots.add(robot)) {
			robot.packageSetUser(this);
			return true;
		} else {
			return false;
		}
	}

	public boolean removeRobot(Robot robot) {
		if (this.robots.remove(robot)) {
			robot.packageSetUser(null);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean containsRobot(Robot robot) {
		return this.getRobots().contains(robot);
	}
}
