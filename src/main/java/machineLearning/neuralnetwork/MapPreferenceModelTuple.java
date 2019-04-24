package machineLearning.neuralnetwork;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import game.Room;
import game.TileTypes;

public class MapPreferenceModelTuple extends DataTuple 
{
	protected static DecimalFormat df3 = new DecimalFormat("#.###");
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public MapPreferenceModelTuple(Room r, boolean like)
	{	
		
		this.numericalData = new ArrayList<Double>();
		
		for (int j = 0; j < r.getRowCount(); j++)
		{
			for (int i = 0; i < r.getColCount(); i++) 
			{
				this.numericalData.add(round(CategoryToNumeric(r.getTile(i, j).GetType()), 2));
			}
		}

		this.label = like;
		
	}
	
	public MapPreferenceModelTuple(String data)
	{
		String[] dataSplit = data.split(";");
		
		this.label = Boolean.parseBoolean(dataSplit[0]); //class
		this.numericalData = new ArrayList<Double>();
		
		for(int i = 1; i < dataSplit.length; i++)
		{
			this.numericalData.add(round(Double.parseDouble(dataSplit[i]), 2));
		}

	}
	
	@Override
	public String getSaveString()
	{
		StringBuilder prefTuple = new StringBuilder();
		
		prefTuple.append(this.label + ";");
		
		for(int i = 0; i < numericalData.size() - 1; i++)
		{
			prefTuple.append(this.numericalData.get(i) + ";");
		}
		
		prefTuple.append(this.numericalData.get(numericalData.size() -1) + System.lineSeparator());

		return prefTuple.toString();
	}
	
	@Override
	public String getHeader() 
	{
		StringBuilder prefTuple = new StringBuilder();
		
		prefTuple.append("class;");
		
		for(int i = 0; i < numericalData.size() - 1; i++)
		{
			prefTuple.append("Tile " + i + ";");
		}
		
		prefTuple.append("Tile " + (numericalData.size() -1) + System.lineSeparator());
		
		return prefTuple.toString();
		
	}
	
	private double CategoryToNumeric(TileTypes tile)
	{
		return (double)(tile.getValue())/(double)(TileTypes.NONE.ordinal());
	}
}
