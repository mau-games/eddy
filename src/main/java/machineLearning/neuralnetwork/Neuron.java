package machineLearning.neuralnetwork;

import java.util.ArrayList;
import java.util.Random;

public class Neuron 
{
	public ArrayList<Connection> connections = new ArrayList<Connection>();
	
	public double output = 0.0;
	//public double desired_output;
	public double error = 0.0;
	
	public enum NeuronTypes
	{
		INPUT,
		HIDDEN,
		OUTPUT,
		BIAS
	};
	
	public NeuronTypes neuron_type;
	
	public Neuron(NeuronTypes nt)
	{
		neuron_type = nt;
		
		
//		if(neuron_type != NeuronTypes.INPUT)
//		{
//			AddConnection(new Neuron(NeuronTypes.BIAS, 1.0));
//		}
	}
	
	public Neuron(NeuronTypes nt, double value)
	{
		neuron_type = nt;
		output = value;
	}
	
	public void AddConnection(Neuron n)
	{
		Random rnd = new Random();
		for(Connection c : connections)
		{
			if(c.from == n)
			{
				return;
			}
		}	
		connections.add(new Connection(n, this, (rnd.nextDouble() * 2.0f) - 1.0f));
	}
	
	public void AddConnection(Neuron n, double weight)
	{
		for(Connection c : connections)
		{
			if(c.from == n)
			{
				return;
			}
		}	
		connections.add(new Connection(n, this, weight));
	}
	
	public void CalculateOutput(double value)
	{
		output = 0.0;
		
		switch(neuron_type)
		{
		case INPUT:
			output = value;
			break;
		case HIDDEN:
		case OUTPUT:
			for(Connection c : connections)
			{
				output += (c.weight * c.from.output);
			}
			
			output = ApplyActivationFunction(output);
			break;
			
		default:
			System.out.println("ERROR NO VALID NEURON TYPE");
			break;
		}
	}
	
	public void CalculateError(double expectedOutput)
	{
		switch(neuron_type)
		{
		case HIDDEN:

			//ONLY FOR SIGMOID
			error *= (output * (1 - output));
			
			break;
			
		case OUTPUT:
			error = 0.0;
			if(expectedOutput == output)
			{
				error = 0;
				return;
			}
			
			error = (expectedOutput - output) * (output * (1 - output));
			break;
			
		default:
			System.out.println("ERROR NO VALID NEURON TYPE");
			break;
		}
	}
	
	public void GetAccumulatedError(ArrayList<Neuron> nextLayerNeurons)
	{
		error = 0.0;
		
		for(Neuron nextLayerNeuron : nextLayerNeurons)
		{
			for(Connection c : nextLayerNeuron.connections)
			{
				if(c.from == this)
				{
					error += (c.weight * nextLayerNeuron.error);
				}
			}
		}
	}
	
	public void CorrectWeights(double learning_rate)
	{
		for(Connection c : connections)
		{
			c.delta_weight = learning_rate * error * c.from.output;
			c.weight += c.delta_weight;
			c.delta_weight = 0.0f;
		}
	}
	
	public double AccumulateWeights(double learning_rate)
	{
		double highestDelta = 0.0;
		for(Connection c : connections)
		{
			c.delta_weight += learning_rate * error * c.from.output;
			
			if(Math.abs(c.delta_weight) > highestDelta)
			{
				highestDelta = Math.abs(c.delta_weight);
			}
		}
		
		return highestDelta;
	}
	
	public void ChangeWeights(double learning_rate)
	{		
		for(Connection c : connections)
		{
			c.weight += c.delta_weight;
			c.delta_weight = 0.0f; //reset that delta weight!
		}
	}
	
	public double ApplyActivationFunction(double output)
	{
		//FOR NOW ONLY SIGMOID
		double return_value = (1.0/(1.0 + Math.pow(Math.E, (-1.0*output))));
		return return_value;
	}
}
