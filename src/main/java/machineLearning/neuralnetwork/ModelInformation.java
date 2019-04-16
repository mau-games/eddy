package machineLearning.neuralnetwork;

import java.util.ArrayList;

public class ModelInformation {
	
	protected double[] accuracy;
	protected double[] loss;
	public double inGameAccuracy;
	public double learningRate;
	public int epoch;
	public int iteration;
	
	//extra utils
	public double batchSize;
	public int maxIterations;
	public int maxEpochs;
	
	public ModelInformation()
	{
		accuracy = new double[] {0.0, 0.0, 0.0};
		loss = new double[] {0.0, 0.0, 0.0};
	}
	
	//Create the model with all the min. info
	public ModelInformation(double learningRate, int epoch, int iteration, double batchSize, int maxIterations, int maxEpochs)
	{
		accuracy = new double[] {0.0, 0.0, 0.0};
		loss = new double[] {0.0, 0.0, 0.0};
		
		this.learningRate = learningRate;
		this.epoch = epoch;
		this.iteration = iteration;
		this.batchSize = batchSize;
		this.maxIterations = maxIterations;
		this.maxEpochs = maxEpochs;
	}

	public double getAccuracy(DatasetUses index)
	{
		return accuracy[index.ordinal()];
	}
	
	public double getLoss(DatasetUses index)
	{
		return loss[index.ordinal()];
	}
	
	public void setAccuracy(DatasetUses index, double value)
	{
		accuracy[index.ordinal()] = value;
	}
	
	public void setLoss(DatasetUses index, double value)
	{
		loss[index.ordinal()] = value;
	}
	
	public double[] getAccuracies()
	{
		return accuracy;
	}
	
	public double[] getLosses()
	{
		return loss;
	}
	
}
