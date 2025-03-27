package dk.sdu.sem.weaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.gamesystem.components.TransformComponent;

public class BulletFactory {

    public static Entity createBullet(TransformComponent transform, WeaponComponent weapon) {
        Entity entity = new Entity();
        entity.addComponent(transform.copy());
        entity.addComponent(new BulletComponent(1, 20));
        return entity;
    }
}
