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
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.GenerationDone;

/**
 * GenerationCollector listens for MapUpdate events and saves them to a preset
 * destination for later analysis.
 * 
 * @author Johan Holmberg
 */
public class GenerationCollector implements Listener {

	private final Logger logger = LoggerFactory.getLogger(GenerationCollector.class);
	private ApplicationConfig config;
	private String path;
	private boolean active;
	//private StringBuffer data = new StringBuffer();
	private HashMap<UUID, StringBuffer> data = new HashMap<UUID, StringBuffer>();

	/**
	 * Creates an instance of MapCollector.
	 */
	public GenerationCollector() {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new GenerationDone(null));
		EventRouter.getInstance().registerListener(this, new AlgorithmDone(null));
		EventRouter.getInstance().registerListener(this, new AlgorithmStarted());
		path = Util.normalisePath(config.getGenerationCollectorPath());
		active = config.getGenerationCollectorActive();

	}
	
	public void setPath(String path){
		this.path = Util.normalisePath(path);
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof GenerationDone) {
			if (active) {
				data.get(((GenerationDone) e).getID()).append((String) e.getPayload() + "\n");
			}
		} else if (e instanceof AlgorithmDone) {
			if (active) {
				saveRun(((AlgorithmDone) e).getID());
			}
		} else if (e instanceof AlgorithmStarted) {
			data.put(((AlgorithmStarted) e).getID(), new StringBuffer());
		}
	}
	
	private synchronized void saveRun(UUID runID) {
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}
		
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "algorithm_result_" +
				LocalDateTime.now().format(format) + ".txt";
		name = "run_" + runID + "_" + name;
		File file = new File(path + name);
		logger.debug("Writing map to " + path + name);

		try {
			FileUtils.writeStringToFile(file, data.toString());
		} catch (IOException e1) {
			logger.error("Couldn't write data to " + path + name +
					":\n" + e1.getMessage());
		}
		data.get(runID).delete(0, data.get(runID).length());
	}
}
