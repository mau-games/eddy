package machineLearning.neuralnetwork.activationFunction;

public class LogisticSigmoid extends ActivationFunction 
{
	public LogisticSigmoid()
	{
		
	}
	
	@Override
	public double applyActivationFunction(double value)
	{
		double return_value = (1.0/(1.0 + Math.pow(Math.E, (-1.0*value))));
		return return_value;
	}
	
	@Override
	public double derivateActivationFunction(double input, double output)
	{
		return output * (1.0 - output);
	}
	
}
