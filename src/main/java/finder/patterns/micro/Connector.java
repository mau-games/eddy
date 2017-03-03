package finder.patterns.micro;

import finder.geometry.Geometry;
import finder.patterns.Pattern;

public class Connector extends Pattern {

	public enum ConnectorType {
		TURN,
		INTERSECTION		
	}
	
	private ConnectorType type;
	
	public double getQuality(){
		switch(type){
			case TURN:
				return 0.2;
			case INTERSECTION:
				return 0.8;
		}
		return 1.0;
		
	}
	
	public Connector(Geometry geometry, ConnectorType type){
		boundaries = geometry;
		this.type = type;
	}
	
	
}
