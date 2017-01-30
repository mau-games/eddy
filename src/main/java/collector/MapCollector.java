package collector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import generator.config.Config;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;

/**
 * MapCollector listens for MapUpdate events and saves them to a preset
 * destination for later analysis.
 * 
 * @author Johan Holmberg
 */
public class MapCollector implements Listener {

	private final Logger logger = LoggerFactory.getLogger(Config.class);
	private String path;
	
	/**
	 * Creates an instance of MapCollector.
	 */
	public MapCollector() {
		EventRouter.getInstance().registerListener(this, new MapUpdate(null));
		// TODO: Set this somewhere intelligent
		path = "";
	}
	
	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			Map map = (Map) e.getPayload();
			// TODO: Fix name generation business.
			String name = "map_"; // + Datetime.now().toString();
			File file = new File(path + name);
			FileWriter fw;
			logger.debug("Writing map to " + path + name);
			
			try {
				fw = new FileWriter(file);
				fw.write(map.toString().toCharArray());
				fw.flush();
				fw.close();
			} catch (IOException e1) {
				logger.error("Couldn't write map to " + path + name + ":\n" +
						e1.getMessage());
			}
		}
	}

}
