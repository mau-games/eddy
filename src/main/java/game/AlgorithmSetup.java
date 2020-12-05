package game;

public class AlgorithmSetup {
	
	private static AlgorithmSetup instance = null;
	private boolean SAVE_DATA = false;
	
	public boolean getSaveData() {
		return SAVE_DATA;
	}

	public void setSaveData(boolean saveData) {
		this.SAVE_DATA = saveData;
	}

	private AlgorithmSetup()
	{

	}
	
	public static AlgorithmSetup getInstance()
	{
		if(instance == null)
		{
			instance = new AlgorithmSetup();
		}
		
		return instance;
	}

}
