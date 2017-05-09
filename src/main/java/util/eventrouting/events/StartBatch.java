package util.eventrouting.events;

import util.eventrouting.events.AlgorithmEvent;

public class StartBatch extends AlgorithmEvent {

	private String config;
	private int size;
	
	public StartBatch(){
		config = "";
		size = 0;
	}
	
	
	public StartBatch(String config, int size){
		this.config = config;
		this.size = size;
	}
	
	public String getConfig(){
		return config;
	}
	
	public int getSize(){
		return size;
	}
	
}
