package util.eventrouting.events.intraview;

import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import util.eventrouting.IntraViewEvent;

public class RestartDimensionsExperiment extends IntraViewEvent
{
	//Array of dimensions in case there is more than 2 (this is for future work!!!)
	private MAPEDimensionFXML[] dimensions;
	
	public RestartDimensionsExperiment(MAPEDimensionFXML[] dimensions) 
	{
		// TODO Auto-generated constructor stub
		this.dimensions = dimensions;
	}
	
	public MAPEDimensionFXML[] getDimensions()
	{
		return dimensions;
	}
	
	public MAPEDimensionFXML getDimension(int index)
	{
		if(this.dimensions.length < index)
		{
			return dimensions[index];
		}
		
		return null;
	}
}
