package dk.sdu.sem.gamesystem.spritemap;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

// which pixels to restrict view to, and reference to the spriteimage
// this should be named something else as this is view+spritemap
@Deprecated
public record ViewPort(Image spriteMap, Rectangle2D rectangle2D) {
}
