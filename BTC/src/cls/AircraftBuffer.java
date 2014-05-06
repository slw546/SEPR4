package cls;

import java.util.ArrayList;
import java.util.Iterator;

public class AircraftBuffer implements Iterable<Aircraft> {
	
	/**
	 * Provides an object containing an arraylist of aircraft
	 * So that the buffer may be monitor locked externally to the threads e.g. game, networkThread, that rely on them
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
	
	/**
	 * Gets the aircraft at the specified index.
	 * Requires monitor lock
	 * If an out of bounds index is accessed, returns the last element of the list instead.
	 * @param index the index of the desired aircraft
	 * @return the aircraft at the given index.
	 */
	public synchronized Aircraft get(int index){
		lockBuffer();
		Aircraft a;
		if (index < buffer.size()){
			a = buffer.get(index);
		} else {
			int size = buffer.size();
			size = size - 1;
			if (size < 0){
				size = 0;
			}
			a = buffer.get(size);
		}
		unlockBuffer();
		return a;
	}
	
	/**
	 * Add an aircraft to the buffer
	 * Calling thread must obtain the monitor lock before adding to the buffer.
	 * @param obj the aircraft to be added
	 */
	public void add(Aircraft obj){
		lockBuffer();
		buffer.add(obj);
		unlockBuffer();
	}

	/**
	 * Gets the current size of the arraylist
	 * @return the size of the arraylist
	 */
	public int size(){
		return buffer.size();
	}

	/**
	 * Removes the aircraft at the specified index from the arraylist
	 * Calling thread must obtain monitor lock before removing the aircraft.
	 * @param index the index of the aircraft to remove
	 */
	public void remove(int index){
		lockBuffer();
		try {
		buffer.remove(index);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
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
	/**
	 * Allows for iteration over the arraylist within the buffer object.
	 */
	public Iterator<Aircraft> iterator() {
		Iterator<Aircraft> iprof = buffer.iterator();
        return iprof;
	}

}
