package machineLearning.neuralnetwork;

public class Connection
{
	public Neuron from;
	public Neuron to;
	public double weight;
	public double delta_weight;
	public double previous_delta;
	
	public Connection(Neuron from, Neuron to, double weight)
	{
		this.from = from;
		this.to = to;
		this.weight = weight;
		previous_delta = 0.0;
	}
}