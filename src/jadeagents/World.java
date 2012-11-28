package jadeagents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jadeagents.Msg.RobotInform;

public class World extends GuiAgent {

	static final int TIME_LAPSE = 1000;
	static final int robotDelta = -10;
	static final int roomDelta = 1;
	static final int startRange = 50;
	static final int maxRange = 255;
	
	Gui gui;
	ArrayList<String> cleanRooms;
	PriorityQueue<Room> queue;
	HashMap<String, Integer> map;
	HashMap<String, String> robotMap;
	String name;
	int roomAmount, robotAmount;
	Statistic stats;
	protected void setup() {
		name = getLocalName();
		map = new HashMap<String, Integer>();
		robotMap = new HashMap<String, String>();
		gui = new Gui();
		roomAmount = 50;
		robotAmount = 10;
		String tmp;
		Random rand = new Random();
		for (int i = 0; i < roomAmount; i++) {
			tmp = "room"+i;
			map.put(tmp, rand.nextInt(startRange));
		}
		
		AgentContainer c = getContainerController();
		//add robots
		ArrayList<String> robots = new ArrayList<String>();
		for(int i = 0; i < robotAmount; i++){
			robots.add("robot"+i);
		}
		
		Object [] args = new Object[1];
        args[0] = "room";
		try {
			for (int i = 0; i < robots.size(); i++) {
				int roomrand = new Random().nextInt(roomAmount);
				args[0] = "room"+roomrand;
				c.createNewAgent( robots.get(i), "jadeagents.Robot", args).start();;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		addBehaviour(new Ticker(this, TIME_LAPSE));
		addBehaviour(new Listen(this));
		System.out.println(name + " is started!");
	}
	
	class Listen extends CyclicBehaviour {
		public Listen(Agent a){
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null){
				Object myObject = null;
				try {
					myObject = msg.getContentObject();
				} catch (UnreadableException e1) {
					System.err.println(name + " caught unreadable exception from " + msg.getSender().getLocalName());
				}
				
				if (myObject instanceof Msg.RobotInform) {
					RobotInform tmpMsg = (Msg.RobotInform) myObject;
					int value = map.get(tmpMsg.room);
					value += tmpMsg.delta;
					if (value <= 0){
						value = 0;
					}
					map.put(tmpMsg.room, value);
					robotMap.put(tmpMsg.robot, tmpMsg.room);
					// check if robot needs to vacate his room
					if (cleanRooms.contains(tmpMsg.room)){
						// tell robot to vacate
						Room newRoomForRobot = queue.poll();
						System.out.println(tmpMsg.robot + " will be asked to go to: " + newRoomForRobot.stringTest);
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(new AID(tmpMsg.robot, AID.ISLOCALNAME));
						Msg.WorldInform msgToRobot = new Msg.WorldInform();
						msgToRobot.robot = tmpMsg.robot;
						msgToRobot.room = newRoomForRobot.stringTest;
						try {
							msg.setContentObject(msgToRobot);
							send(msg);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}	
			}
			block(1000);
		}
	}
	
	class Ticker extends TickerBehaviour {

		public Ticker(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			String room;
			int value;
			System.out.print(new Date(System.currentTimeMillis()) + ": Updated values: ");
			for (Entry<String, Integer> entry : map.entrySet()){
				room = entry.getKey();
				value = entry.getValue();
				value += roomDelta;
				if (value >= maxRange){
					value = maxRange;
				}
				map.put(room, value);
				System.out.print(map.get(room)+"\t");
			}
			System.out.println("");
			
			stats = new Statistic(map);
			System.out.println(new Date(System.currentTimeMillis()) + ": avg:\tq1:\tmedian:\tq3:");
			System.out.println(new Date(System.currentTimeMillis()) + ": "+stats.avg + "\t" + stats.q1 + "\t"+stats.median+ "\t" + stats.q3);
			
			// find out if robot room is dirty or robot needs to change room
			cleanRooms = new ArrayList<String>(); 
			queue = new PriorityQueue<Room>();
			for (Entry<String, Integer> entry : map.entrySet()){
				room = entry.getKey();
				value = entry.getValue();
				queue.add(new  Room(value, room));
				if (value < Math.abs(robotDelta)*2){ // Robot has nothing to do
					cleanRooms.add(room);
				}
			}
			
			gui.updateRoomPanes(map, robotMap);
		}
		
	}
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		

	}

}
