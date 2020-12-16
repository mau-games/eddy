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

	private int ITER_GENERATIONS = 200;
	public int getITER_GENERATIONS() { return ITER_GENERATIONS; }
	public void setITER_GENERATIONS(int ITER_GENERATIONS) { this.ITER_GENERATIONS = ITER_GENERATIONS;	}

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
