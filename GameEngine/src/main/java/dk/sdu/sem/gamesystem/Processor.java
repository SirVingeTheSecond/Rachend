package dk.sdu.sem.gamesystem;

import dk.sdu.sem.gamesystem.services.IProcessorListener;

import java.util.ArrayList;
import java.util.List;

// the class which contains all listeners for IProcessListener
public class Processor {
	private List<IProcessorListener> listeners = new ArrayList<>();

	// Arraylist is not thread syncronized
	public synchronized void addListener(IProcessorListener toAdd) {
		listeners.add(toAdd);

	}
	public void triggerListener() {
		for (IProcessorListener listener : listeners)
			listener.processorTrigger();
	}
}
