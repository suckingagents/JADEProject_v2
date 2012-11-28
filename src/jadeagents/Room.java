package jadeagents;
import java.util.PriorityQueue;
import java.util.Random;
 
public class Room implements Comparable<Room> {
        int intTest = 0;
        String stringTest = "";
       
        public Room(int value, String text) {
                this.intTest = value;
                this.stringTest = text;
        }
       
        public int compareTo(Room myQueue) {
 
                if(intTest >= myQueue.intTest) {
                        return -1;
                } else if (intTest <= myQueue.intTest) {
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