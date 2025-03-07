import services.IProcessorListener;

import java.util.ArrayList;
import java.util.List;

// the class which contians all listeners for IProcessListener procs
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
