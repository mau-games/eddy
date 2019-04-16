package machineLearning.neuralnetwork.activationFunction;

public class Swish extends ActivationFunction 
{
	public Swish()
	{
		
	}
	
	@Override
	public double applyActivationFunction(double value)
	{
		return (value/(1.0 + Math.pow(Math.E, (-1.0*value))));
	}
	
	@Override
	public double derivateActivationFunction(double input, double output)
	{
		return output + output * (1.0-output);
	}
}
