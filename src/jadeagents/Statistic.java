package jadeagents;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class Statistic {

	int avg, median, q1, q3, sum;
	ArrayList<Room> list;
	String filename;
	World w;
	public Statistic(World w){
		this.w = w;
		this.filename = w.filename;
		list = new ArrayList<Room>();
		list.addAll(w.map.values());
		doStatistics();
	}
	
	public ArrayList<Room> getSortedList(HashMap<String, Room> map){
		ArrayList<Room> l = new ArrayList<Room>();
		for(Entry<String, Room> entry : map.entrySet()){
			l.add(entry.getValue());
		}
		Collections.sort(l);
		return l;
	}
	public String getLastString(int minusi){
		return list.get(list.size()-minusi-1).name;
	}
	
	private void doStatistics(){
		ArrayList<Integer> printlist = new ArrayList<Integer>();
		// Calculate room statistics
		sum = 0;
		if (list.size() > 0){
//			Collections.sort(list);
//			Collections.reverse(list);
			for(Room r : list){
				sum += r.dustlevel;
			}
			avg = sum / list.size();
			median = list.get(list.size()/2).dustlevel;
			q1 =list.get(list.size()/4).dustlevel;
			q3 =list.get((list.size()*3)/4).dustlevel;
			
			
			printlist.add((int) System.currentTimeMillis());
			printlist.add(w.roomAmount);
			printlist.add(w.robotAmount);
			printlist.add(w.robotAmount);
			printlist.add(sum);
			int maxvalue = w.roomAmount*World.maxRange;
			printlist.add(maxvalue);
			printlist.add((sum*100)/maxvalue);
			printlist.add(avg);
			printlist.add(q1);
			printlist.add(median);
			printlist.add(q3);
			for(Room r : list){
				printlist.add(r.dustlevel);
			}
			
		}
		
		
		
		// save to file
		if (list.size() > 0){
			try{
				// Create file 
				FileWriter fstream = new FileWriter(filename,true);
				BufferedWriter out = new BufferedWriter(fstream);
				for (Integer i : printlist){
					out.write(i + ",");  
				}
			out.write("\n");
			//Close the output stream
			out.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}	
		}
		
		
	}
	
	
}
