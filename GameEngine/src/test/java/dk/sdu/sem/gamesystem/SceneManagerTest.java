package dk.sdu.sem.gamesystem;

import dk.sdu.sem.gamesystem.data.Entity;
import dk.sdu.sem.gamesystem.data.Scene;

import static org.junit.jupiter.api.Assertions.*;
class SceneManagerTest {

	@org.junit.jupiter.api.Test
	void test_entitiesArePersisted() {
		Scene scene1 = new Scene("Scene1");
		Scene scene2 = new Scene("Scene2");

		SceneManager.getInstance().setActiveScene(scene1);

		Entity entity = new Entity();
		entity.persist();
		scene1.addEntity(entity);

		SceneManager.getInstance().setActiveScene(scene2);

		assertTrue(scene2.getEntities().contains(entity));
		assertFalse(scene1.getEntities().contains(entity));
		assertFalse(scene1.getPersistedEntities().contains(entity));
		assertTrue(scene2.getPersistedEntities().contains(entity));
	}

	@org.junit.jupiter.api.Test
	void test_entitiesAreAddedAndDeleted() {
		Scene scene1 = new Scene("Scene1");
		SceneManager.getInstance().setActiveScene(scene1);

		Entity entity = new Entity();
		entity.persist();

		scene1.addEntity(entity);

		assertTrue(scene1.getEntities().contains(entity));
		assertTrue(scene1.getPersistedEntities().contains(entity));

		scene1.removeEntity(entity);

		assertFalse(scene1.getEntities().contains(entity));
		assertFalse(scene1.getPersistedEntities().contains(entity));
	}
}