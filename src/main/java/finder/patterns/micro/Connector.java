package finder.patterns.micro;

import finder.geometry.Geometry;
import finder.patterns.Pattern;

public class Connector extends Pattern {

	
	public double getQuality(){
		return 1.0;
	}
	
	public Connector(Geometry geometry){
		boundaries = geometry;
	}
	
	
}
