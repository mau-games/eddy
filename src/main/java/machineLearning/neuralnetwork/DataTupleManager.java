package machineLearning.neuralnetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

/**
 * This class uses the IO class in the PacMan framework to do the actual saving/loading of
 * training data.
 * @author andershh
 *
 */
public class DataTupleManager {
	
	private static String projectPath = System.getProperty("user.dir") + "\\my-data";
	
	public static void SaveHeader(DataTuple data, String internalPath, String fileName)
	{
		File file = new File(projectPath + internalPath + "\\" + fileName + ".csv");
		try {
			FileUtils.write(file, data.getHeader(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void SaveData(DataTuple data, String internalPath, String fileName)
	{
		File file = new File(projectPath + internalPath + "\\" + fileName + ".csv");
		try {
			FileUtils.write(file, data.getSaveString(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		IO.saveFile(FileName, data.getSaveString(), true);
	}
	
//	public static void SaveCompleteData(DataTuple data, String internalPath, String fileName)
//	{
//		File file = new File(projectPath + internalPath + "\\" + fileName + ".csv");
//		try {
//			FileUtils.write(file, data.getSaveString(), true);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////		IO.saveFile(FileName, data.getSaveString(), true);
//	}
	
	public static DataTuple[] LoadData(String internalPath, String fileName)
	{
		File file = new File(projectPath + internalPath + "\\" + fileName + ".csv");
		
		String data;
		try {
			data = FileUtils.readFileToString(file);
			String[] dataLine = data.split(System.lineSeparator());
			DataTuple[] dataTuples = new DataTuple[dataLine.length];
			
			for(int i = 0; i < dataLine.length; i++)
			{
				dataTuples[i] = new DataTuple(dataLine[i]);
			}
			
			return dataTuples;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static PreferenceModelDataTuple[] LoadPreferenceModelData (String internalPath, String fileName)
	{
		File file = new File(projectPath + internalPath + "\\" + fileName + ".csv");
		
		String data;
		try {
			data = FileUtils.readFileToString(file);
			String[] dataLine = data.split("\n");
			PreferenceModelDataTuple[] dataTuples = new PreferenceModelDataTuple[dataLine.length];
			
			for(int i = 0; i < dataLine.length; i++)
			{
				dataTuples[i] = new PreferenceModelDataTuple(dataLine[i]);
			}
			
			return dataTuples;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<PreferenceModelDataTuple> LoadPreferenceModelDataList (String internalPath, String fileName)
	{
		File file = new File(projectPath + "\\" + internalPath + "\\" + fileName + ".csv");
		
		String data;
		try {
			data = FileUtils.readFileToString(file);
			String[] dataLine = data.split("\n");
			ArrayList<PreferenceModelDataTuple> dataTuples = new ArrayList<PreferenceModelDataTuple>();
			
			for(int i = 1; i < dataLine.length; i++)
			{
				dataTuples.add(new PreferenceModelDataTuple(dataLine[i]));
			}
			
			return dataTuples;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<MapPreferenceModelTuple> LoadValueMapDataList (String internalPath, String fileName)
	{
		File file = new File(projectPath + "\\" + internalPath + "\\" + fileName + ".csv");
		
		String data;
		try {
			data = FileUtils.readFileToString(file);
			String[] dataLine = data.split("\n");
			ArrayList<MapPreferenceModelTuple> dataTuples = new ArrayList<MapPreferenceModelTuple>();
			
			for(int i = 1; i < dataLine.length; i++)
			{
				dataTuples.add(new MapPreferenceModelTuple(dataLine[i]));
			}
			
			return dataTuples;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
//	public static ArrayList<DataTuple> LoadPacManDataArrayList(String file)
//	{
//		String data = IO.loadFile(file);
//		String[] dataLine = data.split("\n");
//		ArrayList<DataTuple> dataTuples = new ArrayList<DataTuple>();
//		
//		for(int i = 0; i < dataLine.length; i++)
//		{
//			dataTuples.add(new DataTuple(dataLine[i]));
//		}
//		
//		return dataTuples;
//	}
//	
//	public static ArrayList<DataTuple> LoadPacManDataArrayList()
//	{
//		String data = IO.loadFile(FileName);
//		String[] dataLine = data.split("\n");
//		ArrayList<DataTuple> dataTuples = new ArrayList<DataTuple>();
//		
//		for(int i = 0; i < dataLine.length; i++)
//		{
//			dataTuples.add(new DataTuple(dataLine[i]));
//		}
//		
//		return dataTuples;
//	}
}
