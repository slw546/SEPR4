package cls;

import java.util.ArrayList;
import java.util.Iterator;

public class AircraftBuffer implements Iterable<Aircraft> {
	
	/**
	 * Provides an object containing an arraylist of aircraft
	 * So that the buffer may be monitor locked externally to the threads e.g. game, networkThread that rely on them
	 */
	
	
	/**
	 * Arraylist to hold aircraft
	 */
	private ArrayList<Aircraft> buffer;
	
	/**
	 * Whether or not the buffer is locked for editing.
	 */
	private boolean locked;
	
	/**
	 * Constructor
	 */
	public AircraftBuffer(){
		buffer = new ArrayList<Aircraft>();
		locked = false;
	}
	
	public Aircraft get(int index){
		return buffer.get(index);
	}

	public void add(Aircraft obj){
		lockBuffer();
		buffer.add(obj);
		unlockBuffer();
	}

	public int size(){
		return buffer.size();
	}

	public void remove(int index){
		lockBuffer();
		buffer.remove(index);
		unlockBuffer();
	}
	
	/**
	 * Lock the buffer for editing
	 */
	public synchronized void lockBuffer(){
		while (locked){
    		try {
    			wait();
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	locked = true;
	}
	
	/**
	 * Unlock the buffer.
	 */
	public synchronized void unlockBuffer(){
		//System.out.println("unlocking airspace");
		locked = false;
		notifyAll();
	}

	@Override
	public Iterator<Aircraft> iterator() {
		Iterator<Aircraft> iprof = buffer.iterator();
        return iprof;
	}

}
