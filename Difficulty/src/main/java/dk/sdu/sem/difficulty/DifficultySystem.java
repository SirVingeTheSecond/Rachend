package dk.sdu.sem.difficulty;

import dk.sdu.sem.commonitem.IItemFactory;
import dk.sdu.sem.commonlevel.ILevelSPI;
import dk.sdu.sem.commonlevel.room.IRoomClearedListener;
import dk.sdu.sem.commonlevel.room.Room;
import dk.sdu.sem.commonlevel.room.RoomType;
import dk.sdu.sem.commonstats.StatModifier;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.*;
import dk.sdu.sem.enemy.EnemyComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.services.IStart;
import dk.sdu.sem.player.PlayerComponent;

import java.util.ServiceLoader;

public class DifficultySystem implements IRoomClearedListener, IEntityLifecycleListener, IStart {
	private static final IItemFactory itemFactory;

	static {
		itemFactory = ServiceLoader.load(IItemFactory.class).findFirst().orElse(null);
	}

	@Override
	public void onRoomCleared(Room room) {
		if (room.getRoomType() == RoomType.BOSS) {
			Difficulty.increaseLevel();
			DifficultyUI.difficultyIncreased = true;

			// 5-second delay before continuing, using this delay to write in UI.
			Time.after(5, () -> {
				// Generate new level, and item in start room
				ServiceLoader.load(ILevelSPI.class).findFirst().ifPresent(spi -> spi.generateLevel(8,12, 10, 10));

				if (itemFactory != null) {
					Entity item = itemFactory.createItemFromPool(new Vector2D(10 * GameConstants.TILE_SIZE, 13 * GameConstants.TILE_SIZE), "enemy");
					Scene.getActiveScene().addEntity(item);
				}

				// Set player location to middle of room
				Scene.getActiveScene().getEntitiesWithComponent(PlayerComponent.class).stream().findFirst().ifPresent(player ->
					player.getComponent(TransformComponent.class)
						.setPosition(new Vector2D(
							(GameConstants.WORLD_SIZE.x() / 2) * GameConstants.TILE_SIZE,
							(GameConstants.WORLD_SIZE.y() / 2) * GameConstants.TILE_SIZE)
						)
				);
			});
		}
	}

	@Override
	public void onEntityRemoved(Entity entity) {
		// Empty
	}

	@Override
	public void onEntityAdded(Entity entity) {
		if (entity.hasComponent(EnemyComponent.class)) {
			handleEnemySpawned(entity);
		}
	}

	private void handleEnemySpawned(Entity entity) {
		StatsComponent stats = entity.getComponent(StatsComponent.class);
		if (stats == null)
			return;

		//Also change increase health by 20% each level
		StatModifier healthModifier = StatModifier.createPermanentPercent("difficulty", (float) (Math.pow(Difficulty.getLevel(), 2) * 0.5f));
		stats.addModifier(StatType.MAX_HEALTH, healthModifier);
		stats.setCurrentHealth(stats.getMaxHealth());

		if (itemFactory == null)
			return;

		//Add random items to enemies based on difficulty
		int itemCount = (int) Math.round(Math.pow(Difficulty.getLevel(), 1.5));
		for (int i = 0; i < itemCount; i++)
			itemFactory.applyItemFromPool(entity, "enemy");
	}

	@Override
	public void start() {
		//Restart difficulty
		Difficulty.setLevel(0);
	}
}
