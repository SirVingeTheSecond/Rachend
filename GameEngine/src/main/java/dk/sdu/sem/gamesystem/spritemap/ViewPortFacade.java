package dk.sdu.sem.gamesystem.spritemap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// this class is for providing ViewPorts of the
// spritemap to make code more readable
// or the viewports can be made at runtime via ViewPortFactory
@Deprecated
public class ViewPortFacade { Map<String,ViewPort> map;

	public static void main(String[] args) {
		ConcurrentHashMap<String,ViewPort>	viewPortMap = new ConcurrentHashMap<>();
		ViewPortFactory viewPortFactory = new ViewPortFactory();
		// below will not do anything as no spritemap is loaded.
		ViewPort viewPort = viewPortFactory.createViewPort(0,0,0,400,600);
		viewPortMap.put("playerOne",viewPort);


		// this could be automated by using the filename as the key in the
		// filepath
		// hard write values for Image path and add
		// arbitrary names for the sprite here


	}
}


