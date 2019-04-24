package machineLearning.neuralnetwork.activationFunction;

public class ReLU extends ActivationFunction 
{
	public ReLU()
	{
		
	}
	
	@Override
	public double applyActivationFunction(double value)
	{
		return Math.max(0.0, value);
	}
	
	@Override
	public double derivateActivationFunction(double input, double output)
	{
		if(output >= 0) return 1.0;
		else if(output < 0) return 0.0;
		else return 0.0;
	}
}
