package com.sample.model;

public class Sprinkler {

	private Room room;
	private Boolean on = false;

	public Sprinkler(Room room) {
		this.room = room;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Boolean getOn() {
		return on;
	}

	public void setOn(Boolean on) {
		this.on = on;
	}

}
