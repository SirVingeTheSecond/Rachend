package dk.sdu.sem.commonitem;

public interface IItem {

	public String name = "";

	public ItemType getType();

	public void applyEffect();
}
