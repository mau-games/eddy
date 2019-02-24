package util.eventrouting.events;

import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import util.eventrouting.PCGEvent;

public class MAPEGridUpdate extends PCGEvent{

	//Array of dimensions in case there is more than 2 (this is for future work!!!)
	private MAPEDimensionFXML[] dimensions;
	
	public MAPEGridUpdate(MAPEDimensionFXML[] dimensions) 
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
