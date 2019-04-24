package machineLearning.neuralnetwork.activationFunction;

import java.util.List;

import machineLearning.neuralnetwork.Neuron;

//SoftMax needs the info about the other neurons in the list 
public class SoftMax extends ActivationFunction 
{
	private List<Neuron> others;
	private Neuron owner;
	
	public SoftMax()
	{
		
	}
	
	@Override
	public double derivateActivationFunction(double input, double output)
	{
		double return_value = (Math.exp(owner.input) * SUMExcluding(others))/Math.pow(SUM(others), 2.0);
		return return_value;
	}
	
	@Override
	public double postFeedForward(double output, List<Neuron> others, Neuron owner)
	{
		this.others = others;
		this.owner = owner;
		double return_value = Math.exp(owner.input)/SUM(others);
		return return_value;
	}
	
	private double SUM(List<Neuron> others)
	{
		double sum = 0.0 ;
		for(Neuron n : others)
		{
			sum += Math.exp(n.input);
		}
		
		return sum;
	}
	
	private double SUMExcluding(List<Neuron> others)
	{
		double sum = 0.0 ;
		for(Neuron n : others)
		{
			if(!n.equals(owner))
				sum += Math.exp(n.input);
		}
		
		return sum;
	}
}
