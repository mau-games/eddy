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
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.GenerationDone;
import util.eventrouting.events.MapUpdate;

/**
 * GenerationCollector listens for MapUpdate events and saves them to a preset
 * destination for later analysis.
 * 
 * @author Johan Holmberg
 */
public class GenerationCollector implements Listener {

	private final Logger logger = LoggerFactory.getLogger(GenerationCollector.class);
	private ConfigurationUtility config;
	private String path;
	private boolean active;
	private StringBuffer data = new StringBuffer();

	/**
	 * Creates an instance of MapCollector.
	 */
	public GenerationCollector() {
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new GenerationDone(null));
		EventRouter.getInstance().registerListener(this, new AlgorithmDone(null));
		path = Util.normalisePath(config.getString("collectors.generation_collector.path"));
		active = config.getBoolean("collectors.generation_collector.active");

		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof GenerationDone) {
			if (active) {
				data.append((String) e.getPayload() + "\n");
			}
		} else if (e instanceof AlgorithmDone) {
			if (active) {
				saveRun();
			}
		}
	}
	
	private void saveRun() {
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "algorithm_result_" +
				LocalDateTime.now().format(format) + ".txt";
		File file = new File(path + name);
		logger.debug("Writing map to " + path + name);

		try {
			FileUtils.writeStringToFile(file, data.toString());
		} catch (IOException e1) {
			logger.error("Couldn't write data to " + path + name +
					":\n" + e1.getMessage());
		}
	}
}
