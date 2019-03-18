package generator.algorithm.MAPElites;

public class GADimensionsGranularity 
{
	private double dimensionValue;
	
	private double minValue;
	private double maxValue;
	private double dimensionGranularity;
	
	private double index;
	
	public GADimensionsGranularity(double value, double min, double max, double gran, int index)
	{
		this.setDimensionValue(value);
		this.setMinValue(min);
		this.setMaxValue(max);
		this.setDimensionGranularity(gran);
		this.setIndex(index);
	}

	public GADimensionsGranularity(double value, double gran, int index)
	{
		this.setDimensionValue(value);
		this.setDimensionGranularity(gran);
		this.setIndex(index);
		double increment = 1/(gran * 2);
		this.setMinValue(index <= 0 ? 0 : value - increment);
		this.setMaxValue(value + increment);
	}

	public double getDimensionValue() {
		return dimensionValue;
	}

	public void setDimensionValue(double dimensionValue) {
		this.dimensionValue = dimensionValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getDimensionGranularity() {
		return dimensionGranularity;
	}

	public void setDimensionGranularity(double dimensionGranularity) {
		this.dimensionGranularity = dimensionGranularity;
	}

	public double getIndex() {
		return index;
	}

	public void setIndex(double index) {
		this.index = index;
	}
}
