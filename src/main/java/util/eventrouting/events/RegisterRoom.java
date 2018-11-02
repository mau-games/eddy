package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

/***
 * was it me?
 * @author Alberto Alvarez, Malmö University
 *
 */
public class RegisterRoom extends PCGEvent
{
	public RegisterRoom(Room currentRoom)
	{
		setPayload(currentRoom);
	}
}
