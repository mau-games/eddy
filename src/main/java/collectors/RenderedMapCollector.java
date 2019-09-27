package collectors;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.imageio.ImageIO;

import game.ApplicationConfig;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.MapRendered;
import util.eventrouting.events.RenderingDone;

/**
 * RenderedMapCollector listens for MapRendered events and saves them to a preset
 * destination for later analysis.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class RenderedMapCollector implements Listener {
	
//	private final Logger logger = LoggerFactory.getLogger(RenderedMapCollector.class);
	private ApplicationConfig config;
	private String path;
	private boolean active;
	
	/**
	 * Creates an instance of RenderedMapCollector.
	 */
	public RenderedMapCollector() {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
//			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new MapRendered(null));
		EventRouter.getInstance().registerListener(this, new AlgorithmStarted());
		path = Util.normalisePath(config.getImageExporterPath());
		active = config.getImageExporterActive();

	}
	
	public void setPath(String path){
		this.path = Util.normalisePath(path);
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapRendered) {
			if (active) {
				saveImage(((MapRendered) e).getID(), (Image) e.getPayload());
			}
		} else if (e instanceof AlgorithmStarted) {
		}
	}

	/**
	 * Saves an image.
	 * 
	 * @param map The image to save.
	 */
	private synchronized void saveImage(UUID runID, Image map) {
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdir();
		}
		
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "renderedmap_" +
				LocalDateTime.now().format(format) + ".png";
		name = "run_" + runID + "_" + name;
		File file = new File(path + name);
//		logger.debug("Writing map to " + path + name);
		BufferedImage image = SwingFXUtils.fromFXImage(map, null);

		try {
			ImageIO.write(image, "png", file);
		} catch (IOException e1) {
//			logger.error("Couldn't write map to " + path + name +
//					":\n" + e1.getMessage());
		}
		
		EventRouter.getInstance().postEvent(new RenderingDone());
	}
}
