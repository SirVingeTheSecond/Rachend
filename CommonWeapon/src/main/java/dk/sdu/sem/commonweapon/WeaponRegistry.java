package dk.sdu.sem.commonweapon;

import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class WeaponRegistry {
	private static final Logging LOGGER = Logging.createLogger("WeaponRegistry", LoggingLevel.DEBUG);
	private static final Map<String, IWeaponSPI> weapons = new HashMap<>();
	private static final Map<String, IMeleeWeaponSPI> meleeWeapons = new HashMap<>();
	private static final Map<String, IRangedWeaponSPI> rangedWeapons = new HashMap<>();

	static {
		loadWeapons();
	}

	private static void loadWeapons() {
		// Load all IWeaponSPI implementations
		ServiceLoader.load(IWeaponSPI.class).forEach(weapon -> {
			weapons.put(weapon.getId(), weapon);
			LOGGER.debug("Registered weapon: %s", weapon.getId());

			// Also register specialized types in their respective maps
			if (weapon instanceof IMeleeWeaponSPI) {
				meleeWeapons.put(weapon.getId(), (IMeleeWeaponSPI) weapon);
				LOGGER.debug("Registered as melee weapon: %s", weapon.getId());
			}

			if (weapon instanceof IRangedWeaponSPI) {
				rangedWeapons.put(weapon.getId(), (IRangedWeaponSPI) weapon);
				LOGGER.debug("Registered as ranged weapon: %s", weapon.getId());
			}
		});

		// Also load any IMeleeWeaponSPI or IRangedWeaponSPI implementations that might not implement IWeaponSPI directly
		// (This shouldn't happen with our architecture, but it's a safety measure)
		ServiceLoader.load(IMeleeWeaponSPI.class).forEach(weapon -> {
			if (!weapons.containsKey(weapon.getId())) {
				weapons.put(weapon.getId(), weapon);
				meleeWeapons.put(weapon.getId(), weapon);
				LOGGER.debug("Registered melee-specific weapon: %s", weapon.getId());
			}
		});

		ServiceLoader.load(IRangedWeaponSPI.class).forEach(weapon -> {
			if (!weapons.containsKey(weapon.getId())) {
				weapons.put(weapon.getId(), weapon);
				rangedWeapons.put(weapon.getId(), weapon);
				LOGGER.debug("Registered ranged-specific weapon: %s", weapon.getId());
			}
		});
	}

	/**
	 * Gets a weapon by ID regardless of type
	 */
	public static IWeaponSPI getWeapon(String id) {
		return weapons.get(id);
	}

	/**
	 * Gets a weapon by ID and casts to the specified type if compatible
	 */
	public static <T extends IWeaponSPI> T getWeapon(String id, Class<T> type) {
		IWeaponSPI weapon = getWeapon(id);

		if (weapon == null) {
			return null;
		}

		if (type.isInstance(weapon)) {
			return type.cast(weapon);
		}

		LOGGER.error("Weapon %s is not of type %s", id, type.getSimpleName());
		return null;
	}

	/**
	 * Gets a melee weapon by ID
	 */
	public static IMeleeWeaponSPI getMeleeWeapon(String id) {
		return meleeWeapons.get(id);
	}

	/**
	 * Gets a ranged weapon by ID
	 */
	public static IRangedWeaponSPI getRangedWeapon(String id) {
		return rangedWeapons.get(id);
	}

	/**
	 * Gets all registered weapons
	 */
	public static List<IWeaponSPI> getAllWeapons() {
		return new ArrayList<>(weapons.values());
	}

	/**
	 * Gets all registered melee weapons
	 */
	public static List<IMeleeWeaponSPI> getAllMeleeWeapons() {
		return new ArrayList<>(meleeWeapons.values());
	}

	/**
	 * Gets all registered ranged weapons
	 */
	public static List<IRangedWeaponSPI> getAllRangedWeapons() {
		return new ArrayList<>(rangedWeapons.values());
	}
}