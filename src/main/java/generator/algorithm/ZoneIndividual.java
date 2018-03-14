package generator.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import finder.geometry.Point;
import game.Game;
import game.Map;
import game.Tile;
import game.TileTypes;
import game.ZoneNode;
import generator.config.GeneratorConfig;
import util.Util;

/**
 * Represents a member of Eddy's dungeon level population
 * TODO: Not so sold on how mutationProbability is passed around here
 * 
 * @author Alexander Baldwin, Malmö Högskola
 *
 */
public class ZoneIndividual {
	private double fitness;
	
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
	
	/**
	 * Generate a genotype
	 * 
	 */
	public void initialize() {
		genotype.randomSupervisedChromosome();
	}
	
	public ZoneIndividual(Map map, float mutationProbability){
		config = map.getConfig();
		genotype = new ZoneGenotype(config,Game.sizeHeight * Game.sizeWidth);
		phenotype = null;
		fitness = 0.0;
		evaluate = false;
		this.mutationProbability = mutationProbability;
		
		genotype.ProduceGenotype(map);
	}
	
	/**
	 * Two point crossover between two individuals.
	 * 
	 * @param other An Individual to reproduce with.
	 * @return An array of offspring resulting from the crossover.
	 */
	public ZoneIndividual[] twoPointCrossover(ZoneIndividual other){
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
			children[0].getGenotype().getChromosome()[p.getY() * Game.sizeWidth + p.getX()] = other.getGenotype().getChromosome()[p.getY() * Game.sizeWidth + p.getX()];
			children[1].getGenotype().getChromosome()[p.getY() * Game.sizeWidth + p.getX()] = genotype.getChromosome()[p.getY() * Game.sizeWidth + p.getX()];
		}
		
		//mutate TODO: THIS NEED TO BE ALSO DEPENDABLE ON THE ZONES here is the root of the problem!!!!
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability)
			{
				//This need to be specifc to each child
				rndZone = Util.getNextInt(0, myZones.size());
				float rand = Util.getNextFloat(0, 1);
				if(rand <= 0.8f)
					children[i].mutate(myZones.get(rndZone));
				else if  (rand <= 0.8f)
					children[i].squareMutation();
				else
					children[i].mutateRotate180(myZones.get(rndZone));
			}
		}
		
		children[0].genotype.GetRootChromosome().UpdateRefMap(children[0].genotype.getChromosome());
		children[1].genotype.GetRootChromosome().UpdateRefMap(children[1].genotype.getChromosome());
		return children;
	}
	
	public ZoneIndividual[] rectangularCrossover(ZoneIndividual other){
		ZoneIndividual[] children = new ZoneIndividual[2];
		children[0] = new ZoneIndividual(config, new ZoneGenotype(config, genotype.getChromosome().clone(), genotype.GetRootChromosome()), mutationProbability);
		children[1] = new ZoneIndividual(config, new ZoneGenotype(config, other.getGenotype().getChromosome().clone(), genotype.GetRootChromosome()), mutationProbability);

		int lowerBoundM = Util.getNextInt(0, Game.sizeWidth);
		int upperBoundM = Util.getNextInt(lowerBoundM, Game.sizeWidth);
		int lowerBoundN = Util.getNextInt(0, Game.sizeHeight);
		int upperBoundN = Util.getNextInt(lowerBoundN, Game.sizeHeight);
		
		for(int i = lowerBoundM; i <= upperBoundM; i++){
			for(int j = lowerBoundN; j <= upperBoundN; j++){
				//exchange
				children[0].getGenotype().getChromosome()[j*Game.sizeWidth + i] = other.getGenotype().getChromosome()[j*Game.sizeWidth + i];
				children[1].getGenotype().getChromosome()[j*Game.sizeWidth + i] = genotype.getChromosome()[j*Game.sizeWidth + i];
			}
		}
		
		//mutate
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability){
				float rand = Util.getNextFloat(0, 1);
				if(rand <= 0.6f)
					children[i].mutateAll(0.2);
				else if  (rand <= 0.8f)
					children[i].squareMutation();
				else
					children[i].mutateRotate180();
			}
				
		}
		
		return children;
		
	}
	
	
	/**
	 * Mutate ONE bit of the chromosome
	 */
	public void mutate() {
		int indexToMutate = Util.getNextInt(0,genotype.getSizeChromosome());
		genotype.getChromosome()[indexToMutate] = (genotype.getChromosome()[indexToMutate] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
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
		
		genotype.getChromosome()[indexToMutate] = (genotype.getChromosome()[indexToMutate] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
	}
	
	public void squareMutation(){
		double wallChance = 0.1;
		int size = Util.getNextInt(3, 5);
		int startX = Util.getNextInt(0, Game.sizeWidth - size);
		int startY = Util.getNextInt(0, Game.sizeHeight - size);
		if(Util.getNextFloat(0, 1) <= wallChance){
			for(int i = startX; i < startX + size; i++)
				for(int j = startY; j < startY + size; j++){
					if(genotype.getChromosome()[j*Game.sizeWidth + i] < 4) genotype.getChromosome()[j*Game.sizeWidth + i] = 1;
				}
		} else {
			for(int i = startX; i < startX + size; i++)
				for(int j = startY; j < startY + size; j++){
					if(genotype.getChromosome()[j*Game.sizeWidth + i] < 4) genotype.getChromosome()[j*Game.sizeWidth + i] = randomFloorTile();
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
	public void mutateAll(double probability)
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
				
				if(Math.random() < probability){
					genotype.getChromosome()[p.getY() * Game.sizeWidth + p.getX()] = (genotype.getChromosome()[p.getY() * Game.sizeWidth + p.getX()] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
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
	
	private void mutateRotate180(){
		int[] chromosomeCopy = genotype.getChromosome().clone();
		for(int i = 0; i < Game.sizeWidth; i++)
			for(int j = 0; j < Game.sizeHeight; j++)
				genotype.getChromosome()[j*Game.sizeWidth + i] = chromosomeCopy[(Game.sizeHeight - 1 - j)*Game.sizeWidth + Game.sizeWidth - 1 - i];
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
			genotype.getChromosome()[sortedKeys.get(ord)] = chromosomeCopy[sortedKeys.get(rev)];
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
	
	private void mutateRotate90(){
		int[] chromosomeCopy = genotype.getChromosome().clone();
		for(int i = 0; i < Game.sizeWidth; i++)
			for(int j = 0; j < Game.sizeHeight; j++)
				genotype.getChromosome()[j*Game.sizeWidth + i] = chromosomeCopy[(Game.sizeHeight - 1 - i)*Game.sizeWidth + Game.sizeWidth - 1 - j];
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
	
}
