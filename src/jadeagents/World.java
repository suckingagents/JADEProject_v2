package jadeagents;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import jadeagents.Msg.RobotInform;

public class World extends GuiAgent {
	static final int TIME_LAPSE = 1000;
	static final int robotDelta = -6;
	static final int roomDelta = 1;
	static final int startRange = 50;
	static final int maxRange = 255;
	
	
	static final String filepath = "D:/agent/";
	String filename = filepath + "out.txt";
	Gui gui;
	ArrayList<String> cleanRooms;
	PriorityQueue<Room> queue;
	LinkedList<String> fifo;
	HashMap<String, Room> map;
	HashMap<String, String> robotMap;
	String name;
	int roomAmount, robotAmount;
	Statistic stats;
	
	// Method paramaters
	Integer clean_threshold;
	Integer dirty_threshold;
	
	// List for implementing the methods
	ArrayList<String> rooms_clean;
	ArrayList<String> rooms_dirty;
	ArrayList<Room> rooms_all;
	ArrayList<String> robots;
	int next_robot; // For round-robin robots.
	
	protected void setup() {
		name = getLocalName();
		map = new HashMap<String, Room>();
		robotMap = new HashMap<String, String>();
		fifo = new LinkedList<String>();
		gui = new Gui(this);
		
		// Create file
		File f =new File(filename);
		int k = 0;
		while(f.exists()){
			k++;
			filename = filepath + "out" + k + ".txt";
			f = new File(filename);
		}
		try {
			f.createNewFile();
			System.out.println("New data file created: " + filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		roomAmount = 50;
		robotAmount = 10;
		
		// Method Parameters
		clean_threshold = 25;
		dirty_threshold = 50;
		next_robot = 0;
		
		String tmp;
		Random rand = new Random();
		Room tmpRoom;
		for (int i = 0; i < roomAmount; i++) {
			tmp = "room"+i;
			tmpRoom = new Room(rand.nextInt(startRange), tmp, 0);
			map.put(tmp, tmpRoom);
		}
		
		AgentContainer c = getContainerController();
		//add robots
		robots = new ArrayList<String>();
		for(int i = 0; i < robotAmount; i++){
			robots.add("robot"+i);
		}
		
		Object [] args = new Object[1];
        args[0] = "room";
		try {
			for (int i = 0; i < robots.size(); i++) {
				int roomrand = new Random().nextInt(roomAmount);
				args[0] = "room"+roomrand;
				c.createNewAgent( robots.get(i), "jadeagents.Robot", args).start();
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
		World w;
		public Listen(World w){
			super(w);
			this.w = w;
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
					Room getRoom = map.get(tmpMsg.room); 
					int value = getRoom.dustlevel;
					value += tmpMsg.delta;
					if (value <= 0){
						value = 0;
					}
					getRoom.dustlevel = value;
					map.put(tmpMsg.room, getRoom);
					robotMap.put(tmpMsg.robot, tmpMsg.room);
					// Pop myself
					boolean removed = false;
					for(String str : fifo){
						if (str.equals(tmpMsg.room)){
							removed = true;
							//System.err.println("Compare for removal: " + str + " vs " + tmpMsg.room + " " + removed + " LENGTH: " + fifo.size());
							removed = false;
							removed = fifo.remove(str);
							//System.err.println("WAS REMOVED: " + removed + " length: " + fifo.size()) ;
							break;
						}
					}
					// UPDATE ROBOTS IN ROOMS
					for (Entry<String, Room> entry : map.entrySet()){
						String roo = entry.getKey();
						Room room = entry.getValue();
						room.robots = 0;
						//System.out.println("Clear: " + room.name + " robots: " + room.robots);
						map.put(roo, room);
					}
					for (Entry<String, String> entry : robotMap.entrySet()){
						String rob = entry.getKey();
						String roo = entry.getValue();
						if (map.get(roo) != null){
							Room tmpRoom = map.get(roo);
							tmpRoom.robots++;
							//System.out.println("Update: " + tmpRoom.name + " robots: " + tmpRoom.robots);
							map.put(roo, tmpRoom);
						}
					}
					stats = new Statistic(w);
					System.out.println(new Date(System.currentTimeMillis()) + ": avg:\tq1:\tmedian:\tq3:");
					System.out.println(new Date(System.currentTimeMillis()) + ": "+stats.avg + "\t" + stats.q1 + "\t"+stats.median+ "\t" + stats.q3);
					gui.statusLbl.setText("Status - Robots: " + robotAmount + " - Rooms: " + roomAmount + " - Avg: " + stats.avg + " - Q1: " + stats.q1 + " - Q2: " + stats.median + " - Q: " + stats.q3 + " - Sum: " + stats.sum + " / " + roomAmount * 255 + " - fillrate: " + ((stats.sum*100) / (roomAmount*255)) + "%" );
					//ArrayList<Room> l = stats.getSortedList(map);
					
					// The dumb paradigm
//					if (rooms_clean != null && rooms_clean.contains(tmpMsg.room)) {
//						// The robots current room is clean
//						
//						// Get a list of rooms
//					    Set set = map.entrySet();
//					    Iterator it = set.iterator();
//					    ArrayList<Room> l = new ArrayList<Room>();
//					    while (it.hasNext()) {
//					      Map.Entry entry = (Map.Entry) it.next();
//					      l.add((Room) entry.getValue());
//					    }
//						
//						// Determining the new room the robot should move to
//						String newRoomForRobot = null;
//						Integer index = 0;
//						
//						// Method 1, the next room.
////						for (int i = 0; i < l.size(); i++) {
////							if (tmpMsg.room.equals(l.get(i).name)) {
////								index = i + 1;
////								if (index.equals(l.size())) {
////									index = 0;
////								}
////								break;
////							}
////						}
//						
//						// Method 2, a random room.
////						Random rand = new Random();
////						index = rand.nextInt(l.size());
//						
//						// Method 3, the dirtiest room.
//						int max_dust = 0;
//						for (int i = 0; i < l.size(); i++) {
//							if (l.get(i).dustlevel > max_dust) {
//								max_dust = l.get(i).dustlevel;
//								index = i;
//							}
//						}
//						
//						
//						// Send the robot a message with the new room to move to.
//						newRoomForRobot = l.get(index).name;
//						msg = new ACLMessage(ACLMessage.INFORM);
//						msg.addReceiver(new AID(tmpMsg.robot, AID.ISLOCALNAME));
//						Msg.WorldInform msgToRobot = new Msg.WorldInform();
//						msgToRobot.robot = tmpMsg.robot;
//						msgToRobot.room = newRoomForRobot;
//						try {
//							msg.setContentObject(msgToRobot);
//							send(msg);
//							//robotMap.put(msgToRobot.robot, msgToRobot.room);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
					
					// The intelligent paradigm
					if (rooms_dirty != null) {
						
						// Get the room that needs cleaning
						String newRoomForRobot = null;
						for (int room = 0; room < rooms_dirty.size(); room++) {
							newRoomForRobot = rooms_dirty.get(room);
							
							// Determine which robot should be sent to the room
							
							// Method 4, round robin robots
							if (next_robot >= robots.size() - 1) {
								next_robot = 0;
							} else {
								next_robot++;
							}
							
							String robotStr = robots.get(next_robot);
							
							// Send the robot a message with the new room to move to.
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(new AID(robotStr, AID.ISLOCALNAME));
							Msg.WorldInform msgToRobot = new Msg.WorldInform();
							msgToRobot.robot = robotStr;
							msgToRobot.room = newRoomForRobot;
							try {
								msg.setContentObject(msgToRobot);
								send(msg);
								//robotMap.put(msgToRobot.robot, msgToRobot.room);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					
					
				}	
				gui.updateRoomPanes(map, robotMap);
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
			String roomStr;
			Room room;
			int value;
			String strout = new Date(System.currentTimeMillis()) + ": Updated values: ";
			for (Entry<String, Room> entry : map.entrySet()){
				roomStr = entry.getKey();
				room = entry.getValue();
				value = room.dustlevel;
				value += roomDelta;
				if (value >= maxRange){
					value = maxRange;
				}
				room.dustlevel  = value;
				map.put(roomStr, room);
				strout += map.get(room)+"\t";
			}
			
			// Pop item from fifo
			if (fifo != null && !fifo.isEmpty()){
				fifo.removeFirst();
			}
			
			// Fill up new lists for the methods
			rooms_clean = new ArrayList<String>();
			rooms_dirty = new ArrayList<String>();
			rooms_all = new ArrayList<Room>();
			queue = new PriorityQueue<Room>();
			for (Entry<String, Room> entry : map.entrySet()){
				roomStr = entry.getKey();
				room = entry.getValue();
				value = room.dustlevel;
				queue.add(new  Room(value, roomStr, room.robots));
				rooms_all.add(new  Room(value, roomStr, room.robots));
				if (value < clean_threshold){ // Robot has nothing to do
					rooms_clean.add(roomStr);
				} else if (value > dirty_threshold & rooms_dirty.size() < 10) {
					rooms_dirty.add(roomStr);
				}
			}
		}
		
	}
	
	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		System.err.println("asdsd");
	}

	public void addAgent(){
		robotAmount++;
		AgentContainer c = getContainerController();
		//add robots
		Object [] args = new Object[1];
        args[0] = "room1";
		try {
			c.createNewAgent("robot"+robotAmount,"jadeagents.Robot", args).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean removedAgent(int i){
		if (robotAmount > 0 && i >= 0){
			AgentContainer c = getContainerController();
			try {
				c.getAgent("robot"+(robotAmount-i)).kill();
			} catch (Exception e) {
				return removedAgent(i+1);
			}
			robotAmount--;
			return true;
		}
		return false;
	}
	
	public void removeAgent(){
		if (removedAgent(0)){
			System.err.println("Agent killed!");
		}
	}
}
