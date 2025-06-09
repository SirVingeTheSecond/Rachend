package dk.sdu.sem.testing;

import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Scene;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IBulletFactory;
import dk.sdu.sem.commonweapon.IWeaponSPI;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.commonweapon.WeaponRegistry;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.PointLightComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.input.Input;
import dk.sdu.sem.gamesystem.input.Key;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;
import dk.sdu.sem.gamesystem.services.IGUIUpdate;
import dk.sdu.sem.gamesystem.services.ILateUpdate;
import dk.sdu.sem.gamesystem.services.IUpdate;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;
import java.util.ServiceLoader;

public class TestSystem implements IUpdate, IFixedUpdate, IGUIUpdate {
	boolean started = false;
	@Override
	public void update() {
		if (Input.getKeyDown(Key.F1)) {
			testBulletPerformancec();
		}
	}


	private static void testBulletPerformancec() {
		Scene testScene = new Scene("TestScene");
		SceneManager.getInstance().setActiveScene(testScene);
		testScene.getPersistedEntities().forEach(testScene::removeEntity);

		Entity weaponOwner = new Entity();
		weaponOwner.addComponent(new TransformComponent(new Vector2D(16*24,12*24), 0, new Vector2D(1,1)));
		IWeaponSPI weapon = WeaponRegistry.getWeapon("bullet_weapon");
		StatsComponent statsComponent = new StatsComponent();
		WeaponComponent weaponComponent = new WeaponComponent(statsComponent, List.of(weapon));

		testScene.addEntity(weaponOwner);

		IBulletFactory bulletFactory = ServiceLoader.load(IBulletFactory.class).iterator().next();
		float posChange = 24f*24f / 100;
		for (int i = 0; i < 500; i ++) {
			Vector2D pos = new Vector2D(
				(float) (Math.floor(i / 100f) * 96f),
				posChange * (i % 100f)
			);

			Entity bullet = bulletFactory.createBullet(pos, new Vector2D(1,0), weaponComponent, weaponOwner);
			//bullet.removeComponent(SpriteRendererComponent.class);
			//bullet.removeComponent(PointLightComponent.class);
			testScene.addEntity(bullet);
		}
	}

	@Override
	public void onGUI(GraphicsContext gc) {
		int fps = (int)(1 / Time.getDeltaTime());
		gc.fillText("FPS: " + fps, 20, 20);
		gc.fillText("PHYS: " + physicsFPS, 20, 40);
	}


	double lastFixedTime;
	static int physicsFPS;
	@Override
	public void fixedUpdate() {
		double fixedDelta = System.currentTimeMillis() - lastFixedTime;
		physicsFPS = (int) (1000 / fixedDelta);
		lastFixedTime = System.currentTimeMillis();
	}
}
