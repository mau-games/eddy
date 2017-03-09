package collectors;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
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

	private final Logger logger = LoggerFactory.getLogger(MapCollector.class);
	private ConfigurationUtility config;
	private String path;
	private boolean active;

	/**
	 * Creates an instance of MapCollector.
	 */
	public MapCollector() {
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new MapUpdate(null));
		path = Util.normalisePath(config.getString("collectors.map_collector.path"));
		active = config.getBoolean("collectors.map_collector.active");

		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (active) {
				Map map = (Map) e.getPayload();
				DateTimeFormatter format =
						DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
				String name = "map_" +
						LocalDateTime.now().format(format) + ".txt";
				File file = new File(path + name);
				logger.debug("Writing map to " + path + name);

				try {
					FileUtils.writeStringToFile(file, map.toString());
				} catch (IOException e1) {
					logger.error("Couldn't write map to " + path + name +
							":\n" + e1.getMessage());
				}
			}
		}
	}
}
