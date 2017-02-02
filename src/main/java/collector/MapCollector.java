package collector;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import generator.config.Config;
import util.config.ConfigurationReader;
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

	private final Logger logger = LoggerFactory.getLogger(Config.class);
	private ConfigurationReader config;
	private String path;
	private boolean active;

	/**
	 * Creates an instance of MapCollector.
	 */
	public MapCollector() {
		try {
			config = ConfigurationReader.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new MapUpdate(null));
		path = normalisePath(config.getString("map.collector.path"));
		active = config.getBoolean("map.collector.active");
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (active) {
				Map map = (Map) e.getPayload();
				DateTimeFormatter format =
						DateTimeFormatter.ofPattern("yyyy-MM-dd_H-m-s-n");
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

	/**
	 * Normalises a path to work equally well on Windows as on sane operating
	 * systems. The provided path should be Unix-formatted. ~/ will be
	 * converted to the current user's home directory.
	 * 
	 * @param path A Unix-formatted path.
	 * @return A path that is usable by the current operating system.
	 */
	private String normalisePath(String path) {
		if (path.startsWith("~/")) {
			path = path.replace("~", System.getProperty("user.home"));
		}
		if (File.separator.equals("\\")) {
			path = path.replace("/", "\\");
		}

		return path;
	}
}
