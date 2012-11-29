package jadeagents;
import java.util.PriorityQueue;
import java.util.Random;
 
public class Room implements Comparable<Room> {
        int dustlevel;
        String name = "";
        int robots;
       
        public Room(int value, String text, int robots) {
                this.dustlevel = value;
                this.name = text;
                this.robots = robots;
        }
       
        public int getCompareVal(){
        	//System.out.println(name + " has " + robots + " robots which makes compare value: " + dustlevel/(robots+1));
        	//return dustlevel/(robots + 1);
        	
        	// Only use dustlevel to compare
        	return dustlevel;
        }
        
        public int compareTo(Room r1) {
                if(getCompareVal() >= r1.getCompareVal()) {                	
                        return -1;
                } else if (getCompareVal() <= r1.getCompareVal()) {
                        return 1;
                } else {
                        return 0;
                }
        }
       /*
        *        
        //public static PriorityQueue<Room> test = new PriorityQueue<Room>();
        public static void main(String[] args) {
                Random rnd = new Random();
                rnd.setSeed(System.currentTimeMillis());
                for (int i = 0; i < 200; i++) {
                        test.add(new Room(rnd.nextInt(232112312), "Wooop" + i));
                }
               
                test.add(new Room(3424, "Wooop" + 201));
 
                for (int i = 0; i < 200; i++) {
                        test.add(new Room(rnd.nextInt(232112312), "Wooop" + i));
                }
 
               
                Room myQueue = test.poll();
                System.out.println(myQueue.stringTest + " " + myQueue.intTest);
        }
        */
}