package dk.sdu.sem.commonitem;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class ItemRegistry {
	private static final Map<String, IItem> items = new HashMap<>();

	static {
		ServiceLoader<IItem> loader = ServiceLoader.load(IItem.class);
		for (IItem item : loader) {
			items.put(item.getName(), item);
		}
	}

	public static IItem getItem(String name) {
		return items.get(name);
	}
}
