package jadeagents;

import jade.core.Agent;
import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Gui extends JFrame {
	JPanel pane;
	JButton addRobotBtn, removeRobotBtn;
	JLabel statusLbl;
	public HashMap<String, GuiRoom> roomMap;
	World world;
	public Gui(World w){
		world = w;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		
		pane = new JPanel();
		pane.setLayout(new FlowLayout());
		pane.setBackground(Color.white);
		getContentPane().add(pane, BorderLayout.CENTER);
		
		roomMap = new HashMap<String, GuiRoom>();
		
		addRobotBtn = new JButton("Add");
		addRobotBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("Adding Agent!");
				world.addAgent();
			}
		});
		removeRobotBtn = new JButton("Remove");
		removeRobotBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("Removing Agent!");
				world.removeAgent();
			}
		});
		
		statusLbl = new JLabel("Status: ");
		getContentPane().add(statusLbl, BorderLayout.NORTH);
		getContentPane().add(addRobotBtn, BorderLayout.WEST);
		getContentPane().add(removeRobotBtn, BorderLayout.EAST);
		setVisible(true);
	}
	
	public void updateRoomPanes(HashMap<String, Room> map, HashMap<String, String> rmap){
		String roomStr, robot;
		Room room;
		int value;
		for (Map.Entry<String, Room> entry : map.entrySet()){
			roomStr = entry.getKey();
			room = entry.getValue();
			value = room.dustlevel;
			updateRoomPane(roomStr, value);
		}
		
		for (Map.Entry<String, String> entry : rmap.entrySet()){
			robot = entry.getKey();
			roomStr = entry.getValue();
			value = Integer.parseInt(roomMap.get(roomStr).robotAmountLbl.getText());
			value++;
			roomMap.get(roomStr).robotAmountLbl.setText(""+value);
		}
	}
	
	private void createNewRoomPane(String name, int dustlevel){
		GuiRoom room = new GuiRoom(name, dustlevel);
		roomMap.put(name, room);
		pane.add(room);
		pane.updateUI();
	}
	
	private void updateRoomPane(String name, int dustlevel){
		GuiRoom room = roomMap.get(name);
		if (room != null){
			room.setDustlevel(dustlevel);
			room.robotAmountLbl.setText("0");
		} else {
			createNewRoomPane(name, dustlevel);
		}
	}
	
	class GuiRoom extends JPanel{
		private String name;
		private JLabel nameLbl;
		private JLabel dustLbl;
		public JLabel robotAmountLbl;
		private int dustlevel;
		public GuiRoom(String name, int dustlevel){
			this.setLayout(new BorderLayout());
			this.setBorder(new EmptyBorder(10, 10, 10, 10) );
			this.name = name;
			this.nameLbl = new JLabel(name);
			this.dustLbl = new JLabel();
			robotAmountLbl = new JLabel("0");
			this.add(nameLbl, BorderLayout.NORTH);
			this.add(robotAmountLbl, BorderLayout.CENTER);
			//this.add(robotList);
			this.add(dustLbl, BorderLayout.SOUTH);
			this.setMinimumSize(new Dimension(200, 50));
			
			// Set level
			this.setDustlevel(dustlevel);
		}
		
		public void setDustlevel(int dustlevel){
			int green, red;
			this.dustlevel = dustlevel;
			dustLbl.setText(dustlevel + "");
			
			if (dustlevel < 127){ // Colour management
				red = dustlevel * 2;
				green = 255;
			}else{
				green = 255 - (dustlevel-128)*2-1;
				red = 255;
			}
			
			if (red >= 255 ){
				red = 255;
			}else if (red <= 0){
				red = 0;
			}
			
			if (green >= 255 ){
				green = 255;
			}else if (green <= 0){
				green = 0;
			}
			this.setBackground(new Color(red, green,0));
			Color c = this.getBackground();
			c = new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue());
			nameLbl.setForeground(c);
			dustLbl.setForeground(c);
			robotAmountLbl.setForeground(c);
			
			//nameLbl.setForeground(255,255,255);
		}
		
	}
}
