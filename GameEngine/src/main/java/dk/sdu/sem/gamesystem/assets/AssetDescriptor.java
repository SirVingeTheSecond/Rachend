package dk.sdu.sem.gamesystem.assets;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an asset that can be loaded.
 * Contains metadata needed to load the asset.
 */
// Will rename this to AssetData
public class AssetDescriptor<T> {
	private final String id;
	private final Class<T> assetType;
	private final String path;
	private final Map<String, Object> metadata;

	public AssetDescriptor(String id, Class<T> assetType, String path) {
		this.id = id;
		this.assetType = assetType;
		this.path = path;
		this.metadata = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public Class<T> getAssetType() {
		return assetType;
	}

	public String getPath() {
		return path;
	}

	public void setMetadata(String key, Object value) {
		metadata.put(key, value);
	}

	public Object getMetadata(String key) {
		return metadata.get(key);
	}
}