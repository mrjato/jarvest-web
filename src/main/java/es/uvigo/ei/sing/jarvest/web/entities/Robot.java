package es.uvigo.ei.sing.jarvest.web.entities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity(name = "Robot")
public class Robot {
	public static final String DEFAULT_CONTENT_TYPE = "text/plain";

	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private String name;
	
	@Basic
	private String description;
	
	@Lob
	@Basic
	private String robot;
	
	@Basic
	private String contentType = Robot.DEFAULT_CONTENT_TYPE;
	
	@Basic
	private boolean publicAccess;
	
	@ManyToOne(fetch = FetchType.EAGER)
	public User user;
	
	public Robot() {
	}
	
	public Robot(User user, String name, String description, String robot, boolean publicAccess) {
		this(user, name, description, robot, Robot.DEFAULT_CONTENT_TYPE, publicAccess);
	}
	
	public Robot(User user, String name, String description, String robot, String contentType, boolean publicAccess) {
		this.user = user;
		this.name = name;
		this.description = description;
		this.robot = robot;
		this.contentType = contentType;
		this.publicAccess = publicAccess;
	}

	public Integer getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setRobot(String robot) {
		this.robot = robot;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getRobot() {
		return robot;
	}

	public boolean isPublicAccess() {
		return publicAccess;
	}

	public void setPublicAccess(boolean isPublicAccess) {
		this.publicAccess = isPublicAccess;
	}
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		if (this.user != null) {
			this.user.removeRobot(this);
		}
		if (user != null) {
			user.addRobot(this);
		}
	}
	
	void packageSetUser(User user) {
		this.user = user;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
