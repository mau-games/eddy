package generator.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import finder.geometry.Point;
import game.Game;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.ZoneNode;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.config.GeneratorConfig;
import util.Util;

/**
 * Represents a member of Eddy's dungeon level population
 * TODO: Not so sold on how mutationProbability is passed around here
 * 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class ZoneIndividual {
	private double fitness;
	private double novelty;
	
	protected HashMap<DimensionTypes, Double> dimensionValues;
	
	//TODO: Reconsider these...
	private double treasureAndEnemyFitness;
    private double roomFitness;
	private double corridorFitness;
	private double roomArea;
	private double corridorArea;
	
	private ZoneGenotype genotype;
	private ZonePhenotype phenotype;
	private boolean evaluate;
	private float mutationProbability;
	private GeneratorConfig config;
	
	private boolean childOfInfeasibles = false;


	//Newly measure metrics!
	//These are all helper metrics to be used.
	//style is in the room not the individual.
	public int style;
	public double style_fitness;
	public double no_style_fitness;
	public double style_weight;
	public double elite = -1.0;

	public double style_selection_pressure = 0.0;
	
	public void setTreasureAndEnemyFitness(double treasureAndEnemyFitness){
		this.treasureAndEnemyFitness = treasureAndEnemyFitness;
	}
	public void setRoomFitness(double roomFitness){
		this.roomFitness = roomFitness;
	}
	public void setCorridorFitness(double corridorFitness){
		this.corridorFitness = corridorFitness;
	}

	/**
	 * Room area NOT weighted by quality
	 * @param roomArea
	 */
	public void setRoomArea(double roomArea){
		this.roomArea = roomArea;
	}
	
	/**
	 * Corridor area NOT weighted by quality
	 * @param corridorArea
	 */
	public void setCorridorArea(double corridorArea){
		this.corridorArea = corridorArea;
	}
	
	public double getTreasureAndEnemyFitness(){
		return treasureAndEnemyFitness;
	}
	public double getRoomFitness(){
		return roomFitness;
	}
	public double getCorridorFitness(){
		return corridorFitness;
	}
	
	public boolean isChildOfInfeasibles(){
		return childOfInfeasibles;
	}
	
	public void setChildOfInfeasibles(boolean cOI){
		childOfInfeasibles = cOI;
	}
	
	/**
	 * Room area NOT weighted by quality
	 * @param roomArea
	 * @return
	 */
	public double getRoomArea(){
		return roomArea;
	}
	
	/**
	 * Corridor area NOT weighted by quality
	 * @param corridorArea
	 * @return
	 */
	public double getCorridorArea(){
		return corridorArea;
	}
	
	public ZoneIndividual(GeneratorConfig config, int size, float mutationProbability) {
		this(config, new ZoneGenotype(config,size), mutationProbability);
	}
	
	public ZoneIndividual(GeneratorConfig config, ZoneGenotype genotype, float mutationProbability){
		this.config = config;
		this.genotype = genotype;
		this.phenotype = null;
		this.fitness = 0.0;
		this.evaluate = false;
		this.mutationProbability = mutationProbability;
	}
	
	public ZoneIndividual(Room room, float mutationProbability)
	{
		config = room.getConfig();
		genotype = new ZoneGenotype(config, room.getColCount() * room.getRowCount());
		phenotype = null;
		fitness = 0.0;
		evaluate = false;
		this.mutationProbability = mutationProbability;
		
		genotype.ProduceGenotype(room);
	}
	
	/**
	 * Generate a genotype
	 * 
	 */
	public void initialize() {
		genotype.randomSupervisedChromosome();
	}

	public void SetDimensionValues(ArrayList<GADimension> dimensions, Room original)
	{
		dimensionValues = new HashMap<DimensionTypes, Double>();
		
		for(GADimension dimension : dimensions)
		{
			dimensionValues.put(dimension.GetType(), dimension.CalculateValue(this, original));
		}
		
		this.getPhenotype().getMap(-1, 1, null, null, null).SetDimensionValues(dimensionValues);
	}
	
	public double getDimensionValue(DimensionTypes currentDimension)
	{
		return dimensionValues.get(currentDimension);
	}
	
	public void BroadcastIndividualDimensions()
	{
		System.out.print("Room fitness: " + getFitness());
		
		for (Entry<DimensionTypes, Double> entry : dimensionValues.entrySet())
		{
		    System.out.print(", " + entry.getKey().toString() + ": " + entry.getValue());
		}
				
		System.out.println();
	}
	/**
	 * Two point crossover between two individuals.
	 * 
	 * @param other An Individual to reproduce with.
	 * @return An array of offspring resulting from the crossover.
	 */
	public ZoneIndividual[] twoPointCrossover(ZoneIndividual other, int width, int height){
		ZoneIndividual[] children = new ZoneIndividual[2];
		children[0] = new ZoneIndividual(config, new ZoneGenotype(config, genotype.getChromosome().clone(), genotype.GetRootChromosome()), mutationProbability);
		children[1] = new ZoneIndividual(config, new ZoneGenotype(config, other.getGenotype().getChromosome().clone(), other.genotype.GetRootChromosome()), mutationProbability);
		
		ArrayList<ZoneNode> myZones = children[0].genotype.GetRootChromosome().GetAllValidZones();
		ArrayList<ZoneNode> otherZones = children[1].genotype.GetRootChromosome().GetAllValidZones();
		
		int rndZone = Util.getNextInt(0, myZones.size());

		int lowerBound = Util.getNextInt(0, myZones.get(rndZone).GetSection().getNumberOfPoints());
		int upperBound = Util.getNextInt(lowerBound,myZones.get(rndZone).GetSection().getNumberOfPoints());
		
		for(int i = lowerBound; i <= upperBound; i++)
		{
			Point p = myZones.get(rndZone).GetSection().getPoint(i);
			//exchange
			if(children[0].getGenotype().getChromosome()[p.getY() * width + p.getX()] < 4 && children[1].getGenotype().getChromosome()[p.getY() * width + p.getX()] < 4)
			{
				children[0].getGenotype().getChromosome()[p.getY() * width + p.getX()] = other.getGenotype().getChromosome()[p.getY() * width + p.getX()];
				children[1].getGenotype().getChromosome()[p.getY() * width + p.getX()] = genotype.getChromosome()[p.getY() * width + p.getX()];
			}

		}
		
		//mutate
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability)
			{
				//This need to be specifc to each child
				rndZone = Util.getNextInt(0, myZones.size());
				float rand = Util.getNextFloat(0, 1);
				if(rand <= 0.8f)
					children[i].mutate(myZones.get(rndZone));
//				else if  (rand <= 0.8f)
//					children[i].squareMutation();
				else
					children[i].mutateRotate180(myZones.get(rndZone));
			}
		}
		
		children[0].genotype.GetRootChromosome().UpdateRefMap(children[0].genotype.getChromosome());
		children[1].genotype.GetRootChromosome().UpdateRefMap(children[1].genotype.getChromosome());
		return children;
	}
	
	public ZoneIndividual[] rectangularCrossover(ZoneIndividual other, int width, int height){
		ZoneIndividual[] children = new ZoneIndividual[2];
		children[0] = new ZoneIndividual(config, new ZoneGenotype(config, genotype.getChromosome().clone(), genotype.GetRootChromosome()), mutationProbability);
		children[1] = new ZoneIndividual(config, new ZoneGenotype(config, other.getGenotype().getChromosome().clone(), genotype.GetRootChromosome()), mutationProbability);

		int lowerBoundM = Util.getNextInt(0, width);
		int upperBoundM = Util.getNextInt(lowerBoundM, width);
		int lowerBoundN = Util.getNextInt(0, height);
		int upperBoundN = Util.getNextInt(lowerBoundN, height);
		
		for(int i = lowerBoundM; i <= upperBoundM; i++){
			for(int j = lowerBoundN; j <= upperBoundN; j++){
				//exchange
				children[0].getGenotype().getChromosome()[j * width+ i] = other.getGenotype().getChromosome()[j * width + i];
				children[1].getGenotype().getChromosome()[j * width + i] = genotype.getChromosome()[j * width + i];
			}
		}
		
		//mutate
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability){
				float rand = Util.getNextFloat(0, 1);
				if(rand <= 0.6f)
					children[i].mutateAll(0.2, width, height);
				else if  (rand <= 0.8f)
					children[i].squareMutation(width, height);
				else
					children[i].mutateRotate180(width, height);
			}
				
		}
		
		return children;
		
	}
	
	
	/**
	 * Mutate ONE bit of the chromosome
	 */
	public void mutate() {
		int indexToMutate = Util.getNextInt(0,genotype.getSizeChromosome());
		if(genotype.getChromosome()[indexToMutate] < 4)
		{
			//genotype.getChromosome()[indexToMutate] = (genotype.getChromosome()[indexToMutate] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
			int v = Util.getNextInt(0, TileTypes.DOOR.getValue());
			genotype.getChromosome()[indexToMutate] =v;
		}
			
	}
	
	/**
	 * Mutate ONE bit of the chromosome
	 */
	public void mutate(ZoneNode validZone) 
	{
		int indexToMutate = validZone.GetOrderedKeys().get(Util.getNextInt(0,validZone.GetOrderedKeys().size()));
		
		if(validZone.GetMap().getTileBasedMap()[indexToMutate].GetImmutable())
		{
			System.out.println("THIS CANNOT HAPPEN, MUTATE");
		}
		
		if(genotype.getChromosome()[indexToMutate] < 4)
		{
//			genotype.getChromosome()[indexToMutate] = (genotype.getChromosome()[indexToMutate] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
			int v = Util.getNextInt(0, TileTypes.DOOR.getValue());
			genotype.getChromosome()[indexToMutate] =v;
//			genotype.getChromosome()[indexToMutate] = Util.getNextInt(0, TileTypes.DOOR.getValue());
		}
			
	}
	
	public void squareMutation(int width, int height){
		System.out.println("DOES THIS EVEN HAPPENS`??");
		double wallChance = 0.1;
		int size = Util.getNextInt(3, 5);
		int startX = Util.getNextInt(0, width- size);
		int startY = Util.getNextInt(0, height- size);
		if(Util.getNextFloat(0, 1) <= wallChance){
			for(int i = startX; i < startX + size; i++)
				for(int j = startY; j < startY + size; j++){
					if(genotype.getChromosome()[j*width + i] < 4) genotype.getChromosome()[j*width + i] = 1;
				}
		} else {
			for(int i = startX; i < startX + size; i++)
				for(int j = startY; j < startY + size; j++){
					if(genotype.getChromosome()[j*width + i] < 4) genotype.getChromosome()[j*width + i] = randomFloorTile();
				}
		}
	}
	
	public int randomFloorTile(){
		float rand = Util.getNextFloat(0, 1);
		if(rand < 0.1f)
			return 2;
		if(rand < 0.2f)
			return 3;
		return 0;
	}
	
	/**
	 * Mutate each bit of the chromosome with a small probability
	 */
	public void mutateAll(double probability, int width, int height)
	{
		ArrayList<ZoneNode> validZones = genotype.GetRootChromosome().GetAllValidZones();
		
		for(ZoneNode validZone : validZones)
		{
			for(Point p : validZone.GetSection().getPoints())
			{
				if(genotype.GetRootChromosome().GetMap().getTile(p.getX(), p.getY()).GetImmutable())
				{
					System.out.println("THIS CANNOT HAPPEN, MUTATE ALL");
				}
				
				if(Math.random() < probability)
				{
					if(genotype.getChromosome()[p.getY() * width + p.getX()] < 4) //TODO: REMOVE THIS FOUR AND CHECK FOR THE ACTUAL DOOR
					{
//						genotype.getChromosome()[p.getY() * Game.sizeWidth + p.getX()] = (genotype.getChromosome()[p.getY() * Game.sizeWidth + p.getX()] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
//						genotype.getChromosome()[indexToMutate] = Util.getNextInt(0, TileTypes.DOOR.getValue());
						genotype.getChromosome()[p.getY() * width + p.getX()] = Util.getNextInt(0, TileTypes.DOOR.getValue());
					}
						
				}
			}
		}
		
		genotype.GetRootChromosome().UpdateRefMap(genotype.getChromosome());
//		
//		for(int i = 0; i < genotype.getSizeChromosome(); i++){
//			if(Math.random() < probability){
//				genotype.getChromosome()[i] = (genotype.getChromosome()[i] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
//			}
//		}
	}
	
	private void mutateRotate180(int width, int height){
		int[] chromosomeCopy = genotype.getChromosome().clone();
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				genotype.getChromosome()[j*width + i] = chromosomeCopy[(height - 1 - j)*width + width - 1 - i];
	}
	
	private void mutateRotate180(ZoneNode validZone)
	{
//		HashMap<Integer, Point> sec = validZone.GetS();
		List<Integer> sortedKeys = validZone.GetOrderedKeys();
		int[] chromosomeCopy = genotype.getChromosome().clone();
		
		for(int ord = 0, rev = sortedKeys.size() - 1; ord < sortedKeys.size(); ord++, rev--)
		{
			if(validZone.GetMap().getTileBasedMap()[sortedKeys.get(ord)].GetImmutable())
			{
				System.out.println("THIS CANNOT HAPPEN, MUTATEROTATE180");
			}
			
			if(genotype.getChromosome()[sortedKeys.get(ord)] < TileTypes.DOOR.getValue() && chromosomeCopy[sortedKeys.get(rev)] < TileTypes.DOOR.getValue())
			{
				genotype.getChromosome()[sortedKeys.get(ord)] = chromosomeCopy[sortedKeys.get(rev)];
			}

		}
		
//		for(int i = 0; i < Game.sizeWidth; i++)
//		{
//			for(int j = 0; j < Game.sizeHeight; j++)
//			{
//				genotype.getChromosome()[j*Game.sizeWidth + i] = chromosomeCopy[(Game.sizeHeight - 1 - j)*Game.sizeWidth + Game.sizeWidth - 1 - i];
//			}
//
//		}
	}
	
	private void mutateRotate90(int width, int height){
		int[] chromosomeCopy = genotype.getChromosome().clone();
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				genotype.getChromosome()[j*width+ i] = chromosomeCopy[(height - 1 - i)* width + width - 1 - j];
	}
	
	

	
	public double getDistance(ZoneIndividual other){
		int[] thisChromosome = this.getGenotype().getChromosome();
		int[] otherChromosome = other.getGenotype().getChromosome();
		
		int match = 0;
		for(int i = 0; i < thisChromosome.length;i++)
			if(thisChromosome[i] == otherChromosome[i]) match++;
		
		return (double)(thisChromosome.length-match)/thisChromosome.length;
	}
	
	/**
	 * Get this individual's calculated fitness
	 * 
	 * @return Fitness
	 */
	public double getFitness(){
		return fitness;
	}
	
	/**
	 * Set this individual's fitness
	 * 
	 * @param fitness Fitness
	 */
	public void setFitness(double fitness){
		this.fitness = fitness;
	}


	public double getNovelty() {
		return novelty;
	}

	public void setNovelty(double novelty) {
		this.novelty = novelty;
	}
	
	/**
	 * Has the fitness of this Individual been evaluated yet?
	 * 
	 * @return true if the fitness of this individual has already been evaluated
	 */
	public boolean isEvaluated() {
		return evaluate;
	}
	
	/**
	 * Set that this Individual has been evaluated.
	 * 
	 * @param evaluate true if the fitness of this Individual has been evaluated
	 */
	public void setEvaluate(boolean evaluate){
		this.evaluate = evaluate;
	}
	
	/**
	 * Get genotype
	 * 
	 * @return Genotype
	 */
	public ZoneGenotype getGenotype(){
		return genotype;
	}
	
	/**
	 * Get phenotype
	 * 
	 * @return Phenotype
	 */
	public ZonePhenotype getPhenotype(){
		if(phenotype == null){
			phenotype = new ZonePhenotype(config, genotype);
		}
		return phenotype;
	}
	
	/*
	 * Update the config file and create once again the phenotype
	 * This operation can be very costly!
	 */
	public void ResetPhenotype(GeneratorConfig config)
	{
		this.config = config;
		phenotype = null;
	}
}
