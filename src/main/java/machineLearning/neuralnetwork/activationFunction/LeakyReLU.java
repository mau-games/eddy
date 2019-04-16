package machineLearning.neuralnetwork.activationFunction;

public class LeakyReLU extends ActivationFunction 
{
	private double alpha = 0.3;
	private double leakyParameter = 20.0;
	
	public LeakyReLU()
	{
		
	}
	
	@Override
	public double applyActivationFunction(double value)
	{
		return Math.max(alpha * value, value);
	}
	
	@Override
	public double derivateActivationFunction(double input, double output)
	{
		if(output >= 0) return 1.0;
		else if(output < 0) return 1.0/leakyParameter;
		else return 0.0;
	}
}
