package jadeagents;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Robot extends Agent {
	static final int ROOM_CHANGE_WAIT = 4*World.TIME_LAPSE;
	String name, room;
	Msg.RobotInform inform;
	protected void setup(){
		
		Object args[] = getArguments();
		// argument 1 i room
		if (args != null){
			room = (String) args[0];
		}
		name = getLocalName();
		addBehaviour(new Ticker(this, World.TIME_LAPSE));
		addBehaviour( new Listen(this));
		System.out.println(name + " is alive! Position: " + room);
	}
	
	class Listen extends CyclicBehaviour {
		long t0;
		public Listen(Agent a){
			super(a);
			t0 = System.currentTimeMillis();
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
				
				if (myObject instanceof Msg.WorldInform) {
					// When we receive this, change room
					if (System.currentTimeMillis() - t0 > 0){
						t0 = System.currentTimeMillis();
						Msg.WorldInform info = (Msg.WorldInform) myObject;
						System.err.println(name + " changed from " + room + " to " + info.room);
						room = info.room;
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
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver( new AID("world", AID.ISLOCALNAME));
			inform = new Msg.RobotInform();
			inform.robot = name;
			inform.room = room;
			inform.delta = World.robotDelta;
			try {
				msg.setContentObject(inform);
				send(msg);
				//System.out.println(name + " cleaning " + room);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
