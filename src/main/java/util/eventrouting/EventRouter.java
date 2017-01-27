package util.eventrouting;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * EventRouter is used to pass events around the different parts of the
 * system. It holds a list of event listeners (@see Listener), which can be
 * e.g. GUI objects, file writers, etc.
 * 
 * This class is implemented as a singleton. In order to use it, call its
 * getInstance() method.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class EventRouter {
	private static EventRouter instance = null;
	
	Dictionary<String, List<Listener>> roster;
	
	/**
	 * This constructor is only to be called by the getInstance() method.
	 */
	protected EventRouter() {
		roster = new Hashtable<String, List<Listener>>();
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An instance of EventRouter.
	 */
	public static EventRouter getInstance() {
		if (instance == null) {
			instance = new EventRouter();
		}
		return instance;
	}
	
	/**
	 * Posts an event that will be distributed to all interested listeners.
	 * 
	 * @param e The event to be sent.
	 */
	public synchronized void postEvent(PCGEvent e) {
		List<Listener> listeners = roster.get(e.getClass());
		if (listeners != null) {
			for (Listener listener : listeners) {
				listener.ping(e);
			}
		}
	}
	
	/**
	 * Registers a listener to listen for events.
	 * 
	 * @param listener A listener, e.g. a part of a GUI.
	 * @param eventType The type of events to listen for.
	 */
	public synchronized void registerListener(Listener listener,
			Object eventType) {
		List<Listener> listeners = roster.get(eventType);
		if (listeners == null) {
			listeners = new ArrayList<Listener>();
			roster.put(eventType.getClass().getName(), listeners);
		}
		
		listeners.add(listener);
	}
}
