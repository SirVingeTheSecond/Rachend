import dk.sdu.sem.commonitem.IItem;

module CommonItem {
	uses dk.sdu.sem.commonitem.IItem;
	exports dk.sdu.sem.commonitem;

	requires java.logging;
	requires CommonCollision;
	requires Common;
}