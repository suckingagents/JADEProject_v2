package jadeagents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Statistic {

	int avg, median, q1, q3, sum;
	ArrayList<Integer> list;
	public Statistic(HashMap<String, Integer> l){
		list = new ArrayList<Integer>();
		list.addAll(l.values());
		doStatistics();
	}
	
	private void doStatistics(){
		// Calculate room statistics
		sum = 0;
		if (list.size() > 0){
			Collections.sort(list);
			for(Integer value : list){
				sum += value;
			}
			avg = sum / list.size();
			median = list.get(list.size()/2);
			q1 =list.get(list.size()/4);
			q3 =list.get((list.size()*3)/4);
		}
	}
}
