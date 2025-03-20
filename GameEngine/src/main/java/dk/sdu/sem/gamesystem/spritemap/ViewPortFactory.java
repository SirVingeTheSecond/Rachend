package dk.sdu.sem.gamesystem.spritemap;

import javafx.scene.image.*;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// class to deduplication of a spritemap
public class ViewPortFactory {
	// could be made as pixelbuffer
	List<Image> spriteMaps = Collections.synchronizedList(new ArrayList<>());

	// x0,y0 is where the ViewPort crop starts from.
	public Image createImageView(int spriteMapsIndex, int startWidtpx,
								 int startHeightpx,
								 int endWidthpx, int endHeightpx) {

		Image spritemap = spriteMaps.get(spriteMapsIndex);
		// Creating a PixelBuffer using INT_ARGB_PRE pixel format.
		// the height and width of the sub-image
		int deltaHeight = endHeightpx - startHeightpx;
		int deltaWidth = endWidthpx - startWidtpx;
		IntBuffer intBuffer = IntBuffer.allocate(deltaWidth * deltaWidth * 4);
		WritablePixelFormat<IntBuffer> pixelFormat =
			PixelFormat.getIntArgbPreInstance();
		PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(deltaWidth, deltaHeight,
			intBuffer, pixelFormat);
		spritemap.getPixelReader().getPixels(startWidtpx,startHeightpx,
			endWidthpx,endHeightpx,
			// i4 is the height of data in bytes, so ARGB is 4 bytes per pixel
			pixelFormat, intBuffer, deltaWidth * 4);
		Image img = new WritableImage(pixelBuffer);
			return img;
	}

	/* auto split spritemap based on a spanning xy to xy box which is then
	 * divided as spriteimage.xmax/xbox.xmax times, provided that there is
	 *  even spacing in the source spritemap.
	 */
	public Image[] createImageViewFromGridRow(int imageIndex,
										  int spriteWidth,
										  int spriteHeight){
		Image image = spriteMaps.get(imageIndex);
			// how many sprites are on a row visually
			 int spriteRowInstances = (int) (spriteWidth/image.getWidth());
			 Image[] viewPorts = new Image[spriteRowInstances];
			 for (int i = 0; i < spriteRowInstances; i++) {

				 createImageView(imageIndex,spriteWidth,spriteHeight,
					 spriteWidth*2,spriteHeight);
				 // offset to next column in row
				 spriteWidth += spriteWidth;
			 }
			 return viewPorts;

	}


//	public void importSpriteMapDir(String Path){
//	}

}
