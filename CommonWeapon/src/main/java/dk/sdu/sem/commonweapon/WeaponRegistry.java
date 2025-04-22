package dk.sdu.sem.commonweapon;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class WeaponRegistry {
	private static final Map<String, IWeaponSPI> weapons = new HashMap<>();

	static {
		ServiceLoader<IWeaponSPI> loader = ServiceLoader.load(IWeaponSPI.class);
		for (IWeaponSPI weapon :loader) {
			weapons.put(weapon.getId(), weapon);
		}
	}

	public static IWeaponSPI getWeapon(String id) {
		return weapons.get(id);
	}
}
