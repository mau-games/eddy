package collectors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

public class DataSaverLoader {
	
	public static final String DIRECTORY= System.getProperty("user.dir") + File.separator + File.separator + "my-data" + File.separator + File.separator ;
	public static String projectPath = System.getProperty("user.dir") + File.separator + File.separator + "my-data";
	
	public static boolean saveFile(String directoryName, String fileName,String data,boolean append)
	{
        try 
        {
            FileOutputStream outS=new FileOutputStream((DIRECTORY + directoryName + File.separator + File.separator + fileName), append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        
        return true;
	}
	
	public static void SaveData(String data, String internalPath, String fileName)
	{
		File file = new File(projectPath + internalPath + File.separator + File.separator + fileName + ".csv");
		try {
			FileUtils.write(file, data, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		IO.saveFile(FileName, data.getSaveString(), true);
	}
}
