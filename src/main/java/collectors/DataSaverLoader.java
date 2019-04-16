package collectors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class DataSaverLoader {
	
	public static final String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\";
	
	public static boolean saveFile(String directoryName, String fileName,String data,boolean append)
	{
        try 
        {
            FileOutputStream outS=new FileOutputStream((DIRECTORY + directoryName + "\\" + fileName), append);
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
}
