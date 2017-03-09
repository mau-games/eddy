package collectors;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapRendered;

/**
 * RenderedMapCollector listens for MapRendered events and saves them to a preset
 * destination for later analysis.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class RenderedMapCollector implements Listener {
	
	private final Logger logger = LoggerFactory.getLogger(RenderedMapCollector.class);
	private ConfigurationUtility config;
	private String path;
	private boolean active;
	
	/**
	 * Creates an instance of RenderedMapCollector.
	 */
	public RenderedMapCollector() {
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		EventRouter.getInstance().registerListener(this, new MapRendered(null));
		path = Util.normalisePath(config.getString("collectors.image_exporter.path"));
		active = config.getBoolean("collectors.image_exporter.active");
	}

	@Override
	public void ping(PCGEvent e) {
		if (e instanceof MapRendered) {
			if (active) {
				saveImage((Image) e.getPayload());
			}
		}
	}

	/**
	 * Saves an image.
	 * 
	 * @param map The image to save.
	 */
	private void saveImage(Image map) {
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "renderedmap_" +
				LocalDateTime.now().format(format) + ".png";
		File directory = new File(path);
		File file = new File(path + name);
		logger.debug("Writing map to " + path + name);
		BufferedImage image = SwingFXUtils.fromFXImage(map, null);

		try {
			if (!directory.exists()) {
				directory.mkdir();
			}
			ImageIO.write(image, "png", file);
		} catch (IOException e1) {
			logger.error("Couldn't write map to " + path + name +
					":\n" + e1.getMessage());
		}
	}
}
