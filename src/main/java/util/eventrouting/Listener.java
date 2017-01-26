package util.eventrouting;

/**
 * This interface is used to listen for events within the system.
 * 
 * @author Johan Holmberg, Malmö University
 */
public interface Listener {
	
	/**
	 * This method is called when a new event is posted to the event router.
	 * 
	 * @param e A relevant event of some kind.
	 */
	public void ping(PCGEvent e);
}
