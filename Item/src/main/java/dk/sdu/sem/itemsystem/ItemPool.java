package dk.sdu.sem.itemsystem;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

public class ItemPool {
	@JsonProperty("items")
	public List<ItemEntry> items = new ArrayList<>();
	private float totalWeight;

	@JsonSetter("items")
	public void setItems(List<ItemEntry> items) {
		this.items = items;
		for (ItemEntry item : items) {
			totalWeight += item.weight;
		}
	}

	@JsonIgnore
	public ItemEntry getRandomItem() {
		//Get random area
		double r = Math.random() * totalWeight;

		// Seek cursor which is in the random area
		float cursor = 0;
		for (ItemEntry entry : items) {
			cursor += entry.weight;
			if (cursor >= r) {
				return entry;
			}
		}
		return null;
	}

	public static class ItemEntry {
		@JsonProperty("name")
		public String name;
		@JsonProperty("weight")
		public float weight;
	}
}
