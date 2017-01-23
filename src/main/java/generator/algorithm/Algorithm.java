package generator.algorithm;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import generator.config.Config;

public class Algorithm {
	public static int POPULATION_SIZE = 100;
	public static float MUTATION_PROB = 0.9f;
	public static float SON_SIZE = 0.7f;
	
	private List<Individual> populationValid;
	private List<Individual> populationInvalid;
	private Individual best;
	private Config mConfig;
	private int indexWorst = 0;
	private List<Individual> readyToValid;
	private List<Individual> readyToInvalid;
	
	public Algorithm(int size, Config config){
		mConfig = config;
		readyToValid = new ArrayList<Individual>();
		readyToInvalid = new ArrayList<Individual>();
		
		try{
			populationValid = new ArrayList<Individual>();
			populationInvalid = new ArrayList<Individual>();
			int i = 0;
			int j = 0;
			while((i + j) < size){
				Individual ind = new Individual(Game.sizeN * Game.sizeM);
				ind.initialize();
			}
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
	}
	
}
