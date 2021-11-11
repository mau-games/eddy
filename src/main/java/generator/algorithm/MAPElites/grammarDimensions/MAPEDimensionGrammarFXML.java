package generator.algorithm.MAPElites.grammarDimensions;

import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar.GrammarDimensionTypes;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MAPEDimensionGrammarFXML
{
//	protected DimensionTypes dimension;
	//This variable relates to how many values the dimension is divided and the assign granularity

	public final SimpleObjectProperty<GrammarDimensionTypes> dimension = new SimpleObjectProperty<GrammarDimensionTypes>();
	public SimpleIntegerProperty granularity = new SimpleIntegerProperty();

	public MAPEDimensionGrammarFXML()
	{
		this(GrammarDimensionTypes.CONFLICT, 0);
	}

	public MAPEDimensionGrammarFXML(@NamedArg("dimension") GrammarDimensionTypes dimension, @NamedArg("granularity")int granularity)
	{
		setDimension(dimension);
		setGranularity(granularity);
	}
	
	public void setDimension(GrammarDimensionTypes type)
	{
		dimension.set(type);
	}
	
	public void setGranularity(int value)
	{
		granularity.set(value);
	}
	
	public GrammarDimensionTypes getDimension()
	{
		return dimension.get();
	}
	
	public int getGranularity()
	{
		return granularity.get();
	}
	
}
