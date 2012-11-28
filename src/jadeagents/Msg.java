package jadeagents;

import java.io.Serializable;

public class Msg implements Serializable{
	String robot, room;
	static class RobotInform extends Msg implements Serializable {
		int delta;
	}
	
	static class WorldInform extends Msg implements Serializable {
		
	}
}
