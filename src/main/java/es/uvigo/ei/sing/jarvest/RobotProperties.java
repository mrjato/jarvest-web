package es.uvigo.ei.sing.jarvest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;

public final class RobotProperties {
	private final static Map<String, RobotInfo> ROBOTS = 
		new LinkedHashMap<String, RobotInfo>();
	private final static Map<String, BranchInfo> BRANCHES =
		new LinkedHashMap<String, BranchInfo>();
	
	static {
		try {
			final Properties props = new Properties();
			props.load(Jarvest.class.getResourceAsStream("transformer.properties"));
			
			final String[] robotNames = props.getProperty("transfomer.names").split(",");
			
			for (String robotName : robotNames) {
				RobotProperties.ROBOTS.put(robotName, new RobotProperties.RobotInfo(
					robotName, 
					props.getProperty(robotName + ".description"), 
					props.getProperty(robotName + ".template") 
				));
			}
		} catch (Exception e) {
			System.err.println("Unable to load robot properties");
		}
		
		BRANCHES.put("Duplicated -> Scattered", 
			new BranchInfo(
				"Duplicated -> Scattered", 
				"branch(:BRANCH_DUPLICATED, :SCATTERED) {\n\t\n}"
			)
		);
		BRANCHES.put("Scattered -> Scattered",  
			new BranchInfo(
				"Scattered -> Scattered", 
				"branch(:BRANCH_SCATTERED, :SCATTERED) {\n\t\n}"
			)
		);
		BRANCHES.put("Duplicated -> Collapsed", 
			new BranchInfo(
				"Duplicated -> Collapsed", 
				"branch(:BRANCH_DUPLICATED, :COLLAPSED) {\n\t\n}"
			)
		);
		BRANCHES.put("Scattered -> Collapsed", 
			new BranchInfo(
				"Scattered -> Collapsed", 
				"branch(:BRANCH_SCATTERED, :COLLAPSED) {\n\t\n}"
			)
		);
		BRANCHES.put("Duplicated -> Ordered", 
			new BranchInfo(
				"Duplicated -> Ordered", 
				"branch(:BRANCH_DUPLICATED, :ORDERED) {\n\t\n}"
			)
		);
		BRANCHES.put("Scattered -> Ordered", 
			new BranchInfo(
				"Scattered -> Ordered", 
				"branch(:BRANCH_SCATTERED, :ORDERED) {\n\t\n}"
			)
		);
	}
	
	public final static class BranchInfo {
		private final String name;
		private final String template;
		
		public BranchInfo(String name, String template) {
			this.name = name;
			this.template = template;
		}

		public String getName() {
			return name;
		}

		public String getTemplate() {
			return template;
		}
		
		public int getOffset() {
			return this.template.indexOf('\t') + 1;
		}
	}
	
	public final static class RobotInfo {
		private final String name;
		private final String description;
		private final String template;
		
		public RobotInfo(String name, String description, String template) {
			super();
			this.name = name;
			this.description = description;
			this.template = template;
		}
		
		public int getOffset() {
			return this.template.indexOf('\'') + 1;
		}
		
		public String getName() {
			return name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getTemplate() {
			return template;
		}
	}
	
	public static List<String> getBranchNames() {
		return new ArrayList<String>(BRANCHES.keySet());
	}
	
	public static BranchInfo getBranchInfo(String branchInfo) {
		return BRANCHES.get(branchInfo);
	}
	
	public static List<String> getRobotNames() {
		return new ArrayList<String>(ROBOTS.keySet());
	}
	
	public static RobotInfo getRobotInfo(String robotName) {
		return ROBOTS.get(robotName);
	}

	private RobotProperties() {}
}
