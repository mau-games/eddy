package collectors;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.ApplicationConfig;
import game.Room;
import game.MapContainer;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmEvent;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.MapUpdate;

/**
 * MapCollector listens for MapUpdate events and saves them to a preset
 * destination for later analysis.
 * 
 * @author Johan Holmberg
 */
public class MapCollector implements Listener {

	private final Logger logger = LoggerFactory.getLogger(MapCollector.class);
	private ApplicationConfig config;
	private String path;
	private boolean active;
	private boolean saveAll;

	/**
	 * Creates an instance of MapCollector.
	 */
	public MapCollector() {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new MapUpdate(null));
		EventRouter.getInstance().registerListener(this, new AlgorithmDone(null));
		EventRouter.getInstance().registerListener(this, new AlgorithmStarted());
		path = Util.normalisePath(config.getMapCollectorPath());
		active = config.getMapCollectorActive();
		saveAll = config.getMapCollectorSaveAll();

	}
	
	public void setPath(String path){
		this.path = Util.normalisePath(path);
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (saveAll && e instanceof MapUpdate || !saveAll && e instanceof AlgorithmDone) {
			if (active) {
				UUID runID = ((AlgorithmEvent)e).getID();
				File directory = new File(path);
				if (!directory.exists()) {
					directory.mkdir();
				}
				
				Room room;
				if(e instanceof AlgorithmDone)
					room = ((MapContainer) e.getPayload()).getMap();
				else		
					room = (Room) e.getPayload();
				DateTimeFormatter format =
						DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
				String name = "map_" +
						LocalDateTime.now().format(format) + ".txt";
				name = "run_" + runID + "_" + name;

				File file = new File(path + name);
				logger.debug("Writing map to " + path + name);

				try {
					FileUtils.writeStringToFile(file, room.toString());
				} catch (IOException e1) {
					logger.error("Couldn't write map to " + path + name +
							":\n" + e1.getMessage());
				}
			}
		} else if (e instanceof AlgorithmStarted) {

		}
	}
}
