package finder.patterns.micro;

import finder.geometry.Geometry;
import finder.patterns.SpacialPattern;
import generator.config.GeneratorConfig;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

public class Connector extends SpacialPattern {

	private double turnQuality = 0.0;
	private double intersectionQuality = 0.0;
	
	public enum ConnectorType {
		TURN,
		INTERSECTION		
	}
	
	private ConnectorType type;
	
	public double getQuality(){
		switch(type){
			case TURN:
				return turnQuality;
			case INTERSECTION:
				return intersectionQuality;
		}
		return 1.0;
		
	}
	
	public Connector(GeneratorConfig config, Geometry geometry, ConnectorType type){
		boundaries = geometry;
		this.type = type;

		turnQuality = config.getTurnQuality();
		intersectionQuality = config.getIntersectionQuality();

	}
	
	
}
