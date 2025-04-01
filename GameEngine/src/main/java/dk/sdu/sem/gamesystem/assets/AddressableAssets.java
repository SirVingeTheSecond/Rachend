package dk.sdu.sem.gamesystem.assets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Addressable assets like Unity would do it.
 * This is a simplified version that supports string-based asset references.
 */
// Could be used, might get deleted
public class AddressableAssets {
	private static final AddressableAssets instance = new AddressableAssets();

	// Maps addresses to asset suppliers
	private final Map<String, AssetEntry<?>> addressMap = new ConcurrentHashMap<>();

	private AddressableAssets() {}

	public static AddressableAssets getInstance() {
		return instance;
	}

	/**
	 * Registers an asset at a specific address.
	 */
	public <T> void registerAddress(String address, Class<T> type, Supplier<T> loader) {
		addressMap.put(address, new AssetEntry<>(type, loader));
	}

	/**
	 * Gets an asset by its address.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAsset(String address, Class<T> type) {
		AssetEntry<?> entry = addressMap.get(address);
		if (entry == null) {
			throw new IllegalArgumentException("No asset registered at address: " + address);
		}

		if (!entry.type.equals(type)) {
			throw new IllegalArgumentException(
				String.format("Type mismatch: Asset at '%s' is of type %s but requested as %s",
					address, entry.type.getName(), type.getName()));
		}

		AssetEntry<T> typedEntry = (AssetEntry<T>) entry;
		return typedEntry.loader.get();
	}

	/**
	 * Removes an asset address.
	 */
	public void unregisterAddress(String address) {
		addressMap.remove(address);
	}

	/**
	 * Checks if an address exists.
	 */
	public boolean hasAddress(String address) {
		return addressMap.containsKey(address);
	}

	private static class AssetEntry<T> {
		final Class<T> type;
		final Supplier<T> loader;

		AssetEntry(Class<T> type, Supplier<T> loader) {
			this.type = type;
			this.loader = loader;
		}
	}
}