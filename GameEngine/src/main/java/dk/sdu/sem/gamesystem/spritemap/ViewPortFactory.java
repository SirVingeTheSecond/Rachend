package dk.sdu.sem.gamesystem.spritemap;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// class to deduplication of a spritemap
public class ViewPortFactory {
	// this could be a String to Image map, for easier references when used
	// outside the factory when you want a throwaway ViewPort.
	// could also be array of arrays if we want better performance
	List<Image> spriteMaps = Collections.synchronizedList(new ArrayList<Image>());

	public Image getImage(int index){
		return spriteMaps.get(index);
	}

	// x0,y0 is where the ViewPort crop starts from.
	public ViewPort createViewPort(int spriteMapsIndex, double x0, double y0,
								   double x,
								   double y) {
		return new ViewPort(
			spriteMaps.get(spriteMapsIndex),
			new Rectangle2D(x0,y0,x,y)
		);
	}

	/* auto split spritemap based on a spanning xy to xy box which is then
	 * divided as spriteimage.xmax/xbox.xmax times, provided that there is
	 *  even spacing in the source spritemap.
	 */
	public ViewPort[] createViewPortFromGridRow(int imageIndex,
										  double spriteWidth,
										  double spriteHeight){
		Image image = spriteMaps.get(imageIndex);
			// how many sprites are on a row
			 int spriteRowInstances = (int) (spriteWidth/image.getWidth());
			 ViewPort[] viewPorts = new ViewPort[spriteRowInstances];
			 for (int i = 0; i < spriteRowInstances; i++) {
				 createViewPort(imageIndex,spriteWidth,spriteHeight,
					 spriteWidth*2,spriteHeight);
				 // offset to next column in row
				 spriteWidth += spriteWidth;
			 }
			 return viewPorts;

	}


//	public void importSpriteMapDir(String Path){
//	}

}
