package machineLearning.neuralnetwork.activationFunction;

import java.util.List;

import machineLearning.neuralnetwork.Neuron;

public class ActivationFunction 
{
	public ActivationFunction()
	{
		
	}
	
	public double applyActivationFunction(double value)
	{
		return value;
	}
	
	public double derivateActivationFunction(double input, double output)
	{
		return output;
	}
	
	public double postFeedForward(double output, List<Neuron> others, Neuron owner)
	{
		return output;
	}
}
