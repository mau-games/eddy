package util.eventrouting.events;

import game.Game.MapMutationType;
import game.Map;
import util.eventrouting.PCGEvent;

public class StartMapMutate extends PCGEvent {
	private MapMutationType mutationType;
	private int mutations;
	private boolean randomiseConfig;
	
	public StartMapMutate(Map map){
		setPayload(map);
		mutationType = MapMutationType.OriginalConfig;
		mutations = 1;
		randomiseConfig = false;
	}
	
	public StartMapMutate(Map map, MapMutationType mutationType, int mutations, boolean randomiseConfig) {
		setPayload(map);
		this.mutationType = mutationType;
		this.mutations = mutations;
		this.randomiseConfig = randomiseConfig;
	}

	public MapMutationType getMutationType(){
		return mutationType;
	}
	
	public int getMutations(){
		return mutations;
	}
	
	public boolean getRandomiseConfig(){
		return randomiseConfig;
	}
}
