package dk.sdu.sem.itemsystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.HashMap;

public class PoolManager {
	private static PoolManager INSTANCE = new PoolManager();
	private HashMap<String, ItemPool> pools;

	private PoolManager() {
		ObjectMapper mapper = new ObjectMapper();

		try {
			pools = mapper.readValue(PoolManager.class.getResource("/ItemPools.json"), new TypeReference<>(){});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ItemPool getItemPool(String poolName) {
		return pools.get(poolName);
	}

	public static PoolManager getInstance() {
		return INSTANCE;
	}
}
