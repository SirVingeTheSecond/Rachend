package dk.sdu.sem.gamesystem.spritemap;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// this class is for providing ViewPorts of the
// spritemap to make code more readable
// or the viewports can be made at runtime via ViewPortFactory
public class ViewPortFacade {
private Map<String, Image> map;

	// primary external map
	public  Image getImageViewPort(String spriteName) {
		return map.get(spriteName);
	}

	public static void main(String[] args) {
		// responsibility for correct naming is on the developer not the program
		ConcurrentHashMap<String,Image>	imageviewPortMap = new ConcurrentHashMap<>();
		ViewPortFactory viewPortFactory = new ViewPortFactory();
		// below will not do anything as no spritemap is loaded.
//		javafx.scene.image.Image viewPort = viewPortFactory.createImageView(0,0,0,400,600);
//		imageviewPortMap.put("playerOne",viewPort);


		// this could be automated by using the filename as the key in the
		// filepath
		// hard write values for Image path and add
		// arbitrary names for the sprite here


	}
}


