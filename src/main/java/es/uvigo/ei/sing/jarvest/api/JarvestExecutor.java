package es.uvigo.ei.sing.jarvest.api;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;
import es.uvigo.ei.sing.jarvest.web.dao.HibernateUtils;
import es.uvigo.ei.sing.jarvest.web.dao.RobotDAO;
import es.uvigo.ei.sing.jarvest.web.dao.UserDAO;
import es.uvigo.ei.sing.jarvest.web.entities.Robot;
import es.uvigo.ei.sing.jarvest.web.entities.User;

@Path("/execute")
@Produces(MediaType.TEXT_PLAIN)
public class JarvestExecutor {
	@GET
	@Path("/test")
	public Response test() {
		return Response.ok("Test Ok").build();
	}
	
	private Response responseOk(String data) {
		return this.responseOk(data, MediaType.TEXT_PLAIN);
	}
	
	private Response responseOk(String data, String mediaType) {
		return Response.ok(data).type(mediaType).build();
	}
	
	private Response responseUnauthorized(String errorMessage) {
		return Response.status(Response.Status.UNAUTHORIZED)
			.entity(errorMessage)
		.build();
	}
	
	private Response responseBadRequest(String errorMessage) {
		return Response.status(Response.Status.BAD_REQUEST)
			.entity(errorMessage)
		.build();
	}
	
	private Response responseError(String errorMessage) {
		return Response.status(Response.Status.fromStatusCode(500))
			.entity(errorMessage)
		.build();
	}
	
	
	@GET
	@Path("/help") 
	public Response help() {
		final StringBuilder sb = new StringBuilder();
		sb.append("GET /help => This help\n");
		sb.append("GET /robot/inline => Inline Robots\n");
		sb.append("\trobot => Robot code\n");
		sb.append("\tinput => Robot input (multiple allowed)\n");
		sb.append("\tctype => Response content type (optional)\n");
		sb.append("GET /robot/code/{id} => Show public robot source code\n");
		sb.append("GET /robot/{id} => Execute public robot\n");
		sb.append("\tinput => Robot input (multiple allowed)\n");
		sb.append("GET /robot/{apiKey}/{id} => Execute private robot\n");
		sb.append("\tinput => Robot input (multiple allowed)\n");
		sb.append("GET /robot/{apiKey}/create/ => Create private robot\n");
		sb.append("\tname => Robot name\n");
		sb.append("\tdescription => Robot description\n");
		sb.append("\trobot => Robot code\n");
		sb.append("\tisPublic => Is public?\n");
		sb.append("GET /robot/{apiKey}/code/{id} => Show private robot source code\n");
		sb.append("GET /robot/{apiKey}/delete/{id} => Delete private robot\n");
		sb.append("GET /robot/{apiKey}/list => Lists user robots\n");
		
		return this.responseOk(sb.toString());
	}
	
	@GET
	@Path("/robot/inline")
	public Response executeInlineRobot(
		@QueryParam("robot") String robot,
		@QueryParam("input") List<String> inputs,
		@DefaultValue(MediaType.TEXT_PLAIN) @QueryParam("ctype") String ctype
	) {
		final Jarvest jarvest = new Jarvest();
		
		final StringBuilder response = new StringBuilder();
		for (String output : jarvest.exec(robot, inputs.toArray(new String[0]))) {
			response.append(output).append('\n');
		}
		
		return this.responseOk(response.toString(), ctype);
	}
	
	@GET
	@Path("/robot/code/{id}")
	public Response showSourceCode(
		@PathParam("id") String id
	) {
		final Session session = HibernateUtils.getSessionFactory().openSession();
		try {
			session.beginTransaction();
			final RobotDAO robotDAO = new RobotDAO(session);
			final Robot robot = robotDAO.get(Integer.parseInt(id));
			session.getTransaction().commit();
			
			if (robot.isPublicAccess()) {
				return this.responseOk(robot.getRobot());
			} else {
				return this.responseUnauthorized("Unauthorized access to robot " + id);
			}
		} catch (RuntimeException e) {
			if (session.getTransaction().isActive())
				session.getTransaction().rollback();
			return this.responseError(e.getMessage());
		} finally {
			session.close();
		}
	}

	@GET
	@Path("/robot/{id}")
	public Response executeRobot(
		@PathParam("id") String id, 
		@QueryParam("input") List<String> inputs
	) {
		final Session session = HibernateUtils.getSessionFactory().openSession();
		try {
			session.beginTransaction();
			final RobotDAO robotDAO = new RobotDAO(session);
			final Robot robot = robotDAO.get(Integer.parseInt(id));
			session.getTransaction().commit();
			
			final Jarvest jarvest = new Jarvest();
			
			if (robot.isPublicAccess()) {
				final StringBuilder response = new StringBuilder();
				for (String output : jarvest.exec(robot.getRobot(), inputs.toArray(new String[0]))) {
					response.append(output).append('\n');
				}
				
				return this.responseOk(response.toString(), robot.getContentType());
			} else {
				return this.responseUnauthorized("Unauthorized access to robot " + id);
			}
		} catch (RuntimeException e) {
			if (session.getTransaction().isActive())
				session.getTransaction().rollback();
			return this.responseError(e.getMessage());
		} finally {
			session.close();
		}
	}

	@GET
	@Path("/robot/{apiKey}/{id}")
	public Response executePrivateRobot(
		@PathParam("apiKey") final String apiKey, 
		@PathParam("id") final String robotId, 
		@QueryParam("input") final List<String> inputs
	) {
		return this.processPrivateRequest(new AbstractPrivateRequest(apiKey, robotId) {
			@Override
			public Response process(Session session, User user, Robot robot) throws Exception {
				final Jarvest jarvest = new Jarvest();
				
				final StringBuilder response = new StringBuilder();
				for (String output : jarvest.exec(robot.getRobot(), inputs.toArray(new String[0]))) {
					response.append(output).append('\n');
				}
					
				return JarvestExecutor.this.responseOk(response.toString(), robot.getContentType());
			}
		});
	}

	@GET
	@Path("/robot/{apiKey}/create/")
	public Response createRobot(
		@PathParam("apiKey") final String apiKey, 
		@QueryParam("name") final String name,
		@QueryParam("description") final String description,
		@QueryParam("robot") final String robotCode,
		@QueryParam("isPublic") final String isPublic
	) {
		return this.processPrivateRequest(new PrivateRequestWithoutRobot(apiKey) {
			@Override
			public Response process(Session session, User user) throws Exception {
				final RobotDAO robotDAO = new RobotDAO(session);
				
				final Robot robotEntity = robotDAO.createRobot(
					user, name, description, robotCode, Boolean.valueOf(isPublic)
				);
				session.getTransaction().commit();
				
				return JarvestExecutor.this.responseOk(robotEntity.getId().toString());
			}
		});
	}
	
	@GET
	@Path("/robot/{apiKey}/code/{id}")
	public Response showRobotCode(
		@PathParam("apiKey") final String apiKey, 
		@PathParam("id") final String robotId
	) {
		return this.processPrivateRequest(new AbstractPrivateRequest(apiKey, robotId) {
			@Override
			public Response process(Session session, User user, Robot robot)
			throws Exception {
				return JarvestExecutor.this.responseOk(robot.getRobot());
			}
		});
	}
	
	@GET
	@Path("/robot/{apiKey}/delete/{id}")
	public Response deleteRobot(
		@PathParam("apiKey") final String apiKey, 
		@PathParam("id") final String robotId
	) {
		return this.processPrivateRequest(new AbstractPrivateRequest(apiKey, robotId) {
			@Override
			public Response process(Session session, User user, Robot robot)
			throws Exception {
				final RobotDAO robotDAO = new RobotDAO(session);
				robotDAO.delete(Integer.parseInt(robotId));
				return JarvestExecutor.this.responseOk("OK");
			}
		});
	}
	
	@GET
	@Path("/robot/{apiKey}/list")
	public Response listRobots(
		@PathParam("apiKey") final String apiKey 
	) {
		return this.processPrivateRequest(new PrivateRequestWithoutRobot(apiKey) {
			@Override
			public Response process(Session session, User user) {
				final StringBuilder sb = new StringBuilder();
				
				for (Robot userRobot : user.getRobots()) {
					sb.append(userRobot.getId()).append('\t').append(userRobot.getName());
				}
				
				return JarvestExecutor.this.responseOk(sb.toString());
			}
		});
	}
	
	private static interface PrivateRequest {
		public Response process(Session session, User user, Robot robot) throws Exception;
		public String getApiKey();
		public Integer getRobotId();
	}
	
	private static abstract class AbstractPrivateRequest implements PrivateRequest {
		private final String apiKey;
		private final Integer robotId;

		public AbstractPrivateRequest(String apiKey, String robotId) {
			this(apiKey, Integer.parseInt(robotId));
		}
		
		public AbstractPrivateRequest(String apiKey, Integer robotId) {
			this.apiKey = apiKey;
			this.robotId = robotId;
		}

		public String getApiKey() {
			return this.apiKey;
		}
		
		@Override
		public Integer getRobotId() {
			return this.robotId;
		}
	}
	
	private static abstract class PrivateRequestWithoutRobot implements PrivateRequest {
		private final String apiKey;
		
		public PrivateRequestWithoutRobot(String apiKey) {
			this.apiKey = apiKey;
		}
		
		@Override
		public Response process(Session session, User user, Robot robot)
		throws Exception {
			return this.process(session, user);
		}
		
		protected abstract Response process(Session session, User user) throws Exception;
		
		@Override
		public String getApiKey() {
			return this.apiKey;
		}
		
		@Override
		public Integer getRobotId() {
			return null;
		}
	}
	
	private Response processPrivateRequest(PrivateRequest request) {
		Session session = null;
		try {
			session = HibernateUtils.getSessionFactory().openSession();
			session.beginTransaction();

			final UserDAO userDAO = new UserDAO(session);
			final User user = userDAO.getUserByApiKey(request.getApiKey());
			
			if (user == null) {
				session.getTransaction().commit();
				return this.responseBadRequest("Invalid API key");
			} else if (request.getRobotId() != null) {
				final RobotDAO robotDAO = new RobotDAO(session);
				
				final Robot robot = robotDAO.get(request.getRobotId());
				
				if (robot == null) {
					session.getTransaction().commit();
					return this.responseBadRequest("Invalid robot id");
				} else if (robot.getUser().getApiKey().equals(request.getApiKey())) {
					final Response response = request.process(session, user, robot);
					session.getTransaction().commit();
					return response;
				} else {
					session.getTransaction().commit();
					return this.responseUnauthorized("Unauthorized access");
				}
			} else {
				final Response response = request.process(session, user, null);
				session.getTransaction().commit();
				return response;
			}
		} catch (Exception e) {
			if (session.getTransaction().isActive())
				session.getTransaction().rollback();
			return this.responseError(e.getMessage());
		} finally {
			if (session != null)
				session.close();
		}
	}
}
