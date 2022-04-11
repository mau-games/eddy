package util.eventrouting.events;

import game.Room;
import game.Game.MapMutationType;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import util.eventrouting.PCGEvent;

public class StartGA_MAPE extends PCGEvent
{
	private MapMutationType mutationType;
	private boolean randomiseConfig;
	static int count = 0;
	//Array of dimensions in case there is more than 2 (this is for future work!!!)
	private MAPEDimensionFXML[] dimensions;

	public StartGA_MAPE(Room room, MAPEDimensionFXML[] dimensions){
		setPayload(room);
		mutationType = MapMutationType.OriginalConfig;
		randomiseConfig = false;
		this.dimensions = dimensions;
	}

	public StartGA_MAPE(Room room){
		setPayload(room);
		mutationType = MapMutationType.OriginalConfig;
		randomiseConfig = false;
	}

	public StartGA_MAPE(Room room, MapMutationType mutationType, MAPEDimensionFXML[] dimensions , boolean randomiseConfig) {
		setPayload(room);
		this.mutationType = mutationType;
		this.randomiseConfig = randomiseConfig;
		this.dimensions = dimensions;
	}

	public MapMutationType getMutationType(){
		return mutationType;
	}

	public boolean getRandomiseConfig(){
		return randomiseConfig;
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
