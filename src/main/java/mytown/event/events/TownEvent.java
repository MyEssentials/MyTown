package mytown.event.events;

import mytown.entities.Town;

/**
 * Town based events
 * @author Joe Goett
 */
public class TownEvent extends MyTownEvent {
	public Town town;
	
	public TownEvent(Town town){
		this.town = town;
	}
	
	public static class TownCreateEvent extends TownEvent {
		public TownCreateEvent(Town town){
			super(town);
		}
	}
	
	public static class TownDeleteEvent extends TownEvent {
		public TownDeleteEvent(Town town) {
			super(town);
		}
	}
}