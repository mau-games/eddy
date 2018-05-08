package util.eventrouting.events;

import game.Game.MapMutationType;
import game.Map;
import generator.algorithm.Algorithm.AlgorithmTypes;
import util.Point;
import util.eventrouting.PCGEvent;

public class StartMapMutate extends PCGEvent {
	private MapMutationType mutationType;
	private int mutations;
	private boolean randomiseConfig;
	private AlgorithmTypes algorithmTypes;
	static int count = 0;
	
	
	public StartMapMutate(Map map){
		setPayload(map);
		mutationType = MapMutationType.OriginalConfig;
		mutations = 1;
		randomiseConfig = false;
		algorithmTypes = algorithmTypes.Native;
	}

	public StartMapMutate(Map map, MapMutationType mutationType, AlgorithmTypes algorithmTypes, int mutations, boolean randomiseConfig) {
		setPayload(map);
		this.mutationType = mutationType;
		this.mutations = mutations;
		this.randomiseConfig = randomiseConfig;
		this.algorithmTypes = algorithmTypes;
		System.out.println("here");
	}

	public MapMutationType getMutationType(){
		return mutationType;
	}
	
	public int getMutations(){
		return mutations;
	}
	public AlgorithmTypes getAlgorithmTypes()
	{
		return algorithmTypes;
	}
	
	public boolean getRandomiseConfig(){
		return randomiseConfig;
	}
}
