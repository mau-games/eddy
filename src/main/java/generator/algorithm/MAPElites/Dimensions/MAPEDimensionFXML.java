package generator.algorithm.MAPElites.Dimensions;

import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MetricInterpreter;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class MAPEDimensionFXML
{
//	protected DimensionTypes dimension;
	//This variable relates to how many values the dimension is divided and the assign granularity
	
	public final SimpleObjectProperty<DimensionTypes> dimension = new SimpleObjectProperty<DimensionTypes>();
	public SimpleIntegerProperty granularity = new SimpleIntegerProperty();
	public MetricInterpreter metric_interpreter;


	public MAPEDimensionFXML()
	{
		this(DimensionTypes.SIMILARITY, 0);
	}
	
	public MAPEDimensionFXML(@NamedArg("dimension") DimensionTypes dimension,  @NamedArg("granularity")int granularity)
	{
		setDimension(dimension);
		setGranularity(granularity);
	}

	public MAPEDimensionFXML(@NamedArg("dimension") DimensionTypes dimension,  @NamedArg("granularity")int granularity,
							 @NamedArg("metric")MetricInterpreter custom_metric)
	{
		setDimension(dimension);
		setGranularity(granularity);
		this.metric_interpreter = custom_metric;
	}
	
	public void setDimension(DimensionTypes type)
	{
		dimension.set(type);
	}
	
	public void setGranularity(int value)
	{
		granularity.set(value);
	}
	
	public DimensionTypes getDimension()
	{
		return dimension.get();
	}
	
	public int getGranularity()
	{
		return granularity.get();
	}

	public MetricInterpreter getMetricInterpreter() {return this.metric_interpreter;}
	
}
