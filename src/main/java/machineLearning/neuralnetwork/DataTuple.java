package machineLearning.neuralnetwork;

import java.util.ArrayList;

public class DataTuple 
{
	
	public ArrayList<Double> numericalData;
	public boolean label; //NEED TO FULLY CHANGE THIS!
	
	public DataTuple()
	{

	}

	//LOAD
	public DataTuple(String data)
	{
		String[] dataSplit = data.split(";");
//		
//		this.DirectionChosen = MOVE.valueOf(dataSplit[0]); //class
//		
//		this.mazeIndex = Integer.parseInt(dataSplit[1]);
//		this.currentLevel = Integer.parseInt(dataSplit[2]);
//		this.pacmanPosition = Integer.parseInt(dataSplit[3]);
//		this.pacmanLivesLeft = Integer.parseInt(dataSplit[4]);
//		this.currentScore = Integer.parseInt(dataSplit[5]);
//		this.totalGameTime = Integer.parseInt(dataSplit[6]);
//		this.currentLevelTime = Integer.parseInt(dataSplit[7]);
//		this.numOfPillsLeft = Integer.parseInt(dataSplit[8]);
//		this.numOfPowerPillsLeft = Integer.parseInt(dataSplit[9]);
//		this.isBlinkyEdible = Boolean.parseBoolean(dataSplit[10]);
//		this.isInkyEdible = Boolean.parseBoolean(dataSplit[11]);
//		this.isPinkyEdible = Boolean.parseBoolean(dataSplit[12]);
//		this.isSueEdible = Boolean.parseBoolean(dataSplit[13]);
//		this.blinkyDist = Integer.parseInt(dataSplit[14]);
//		this.inkyDist = Integer.parseInt(dataSplit[15]);
//		this.pinkyDist = Integer.parseInt(dataSplit[16]);
//		this.sueDist = Integer.parseInt(dataSplit[17]);
//		this.blinkyDir = MOVE.valueOf(dataSplit[18]);
//		this.inkyDir = MOVE.valueOf(dataSplit[19]);
//		this.pinkyDir = MOVE.valueOf(dataSplit[20]);
//		this.sueDir = MOVE.valueOf(dataSplit[21]);
//		this.numberOfNodesInLevel = Integer.parseInt(dataSplit[22]);
//		this.numberOfTotalPillsInLevel = Integer.parseInt(dataSplit[23]);
//		this.numberOfTotalPowerPillsInLevel = Integer.parseInt(dataSplit[24]);
//		this.normalizedPosition = normalizePosition(Integer.parseInt(dataSplit[3]));
//		
//		//my extra values
//		this.n_neighbor = Double.parseDouble(dataSplit[25]);
//		this.e_neighbor = Double.parseDouble(dataSplit[26]);
//		this.s_neighbor = Double.parseDouble(dataSplit[27]);
//		this.w_neighbor = Double.parseDouble(dataSplit[28]);
//		this.num_pill_left_norm = Double.valueOf(dataSplit[29]);
//		this.num_pp_left_norm = Double.valueOf(dataSplit[30]);
//		this.closest_ghost_dist = Double.valueOf(dataSplit[31]);
//		this.closest_ghost_dir = MOVE.valueOf(dataSplit[32]);
//		this.closest_pill_dist = Double.valueOf(dataSplit[33]);
//		this.closest_pp_dist = Double.valueOf(dataSplit[34]);
	}
	
	public String getSaveString()
	{
		StringBuilder stringbuilder = new StringBuilder();
		
//		stringbuilder.append(this.DirectionChosen+";");
//		stringbuilder.append(this.mazeIndex+";");
//		stringbuilder.append(this.currentLevel+";");
//		stringbuilder.append(this.pacmanPosition+";");
//		stringbuilder.append(this.pacmanLivesLeft+";");
//		stringbuilder.append(this.currentScore+";");
//		stringbuilder.append(this.totalGameTime+";");
//		stringbuilder.append(this.currentLevelTime+";");
//		stringbuilder.append(this.numOfPillsLeft+";");
//		stringbuilder.append(this.numOfPowerPillsLeft+";");
//		stringbuilder.append(this.isBlinkyEdible+";");
//		stringbuilder.append(this.isInkyEdible+";");
//		stringbuilder.append(this.isPinkyEdible+";");
//		stringbuilder.append(this.isSueEdible+";");
//		stringbuilder.append(this.blinkyDist+";");
//		stringbuilder.append(this.inkyDist+";");
//		stringbuilder.append(this.pinkyDist+";");
//		stringbuilder.append(this.sueDist+";");
//		stringbuilder.append(this.blinkyDir+";");
//		stringbuilder.append(this.inkyDir+";");
//		stringbuilder.append(this.pinkyDir+";");
//		stringbuilder.append(this.sueDir+";");
//		stringbuilder.append(this.numberOfNodesInLevel+";");
//		stringbuilder.append(this.numberOfTotalPillsInLevel+";");
//		stringbuilder.append(this.numberOfTotalPowerPillsInLevel+";");
//		stringbuilder.append(this.n_neighbor+";");
//		stringbuilder.append(this.e_neighbor+";");
//		stringbuilder.append(this.s_neighbor+";");
//		stringbuilder.append(this.w_neighbor+";");
//		stringbuilder.append(this.num_pill_left_norm +";");
//		stringbuilder.append(this.num_pp_left_norm +";");
//		stringbuilder.append(this.closest_ghost_dist+";");
//		stringbuilder.append(this.closest_ghost_dir+";");
//		stringbuilder.append(this.closest_pill_dist+";");
//		stringbuilder.append(this.closest_pp_dist+";");
		return stringbuilder.toString();
	}

	public String ToString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getHeader() {return null;}
	
}
