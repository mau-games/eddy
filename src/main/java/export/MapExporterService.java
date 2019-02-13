package export;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import export.models.DungeonModel;
import game.Room;


import java.beans.XMLEncoder;

import java.io.FileOutputStream;

public final class MapExporterService {
	
	private MapExporterService() {};
	
	
	public static void SaveDungeonToXML(String fileDestination, DungeonModel dungeonPOCO)
	{
		try {
			//logger.debug("Writing map to " + selectedFile.getPath());
			System.out.print("Saving to " + fileDestination);
			FileOutputStream fos = new FileOutputStream(new File(fileDestination));
			XMLEncoder encoder = new XMLEncoder(fos);
			encoder.writeObject(dungeonPOCO);
			encoder.close();
			fos.close();
		} catch (IOException e) {
			//logger.error("Couldn't write map to " + selectedFile +
					//":\n" + e.getMessage());
			System.out.print(e);
		}
	}
	
	
	
}

