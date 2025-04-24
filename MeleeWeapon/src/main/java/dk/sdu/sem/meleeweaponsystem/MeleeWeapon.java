package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.collision.ICollisionSPI;
import dk.sdu.sem.collision.data.PhysicsLayer;
import dk.sdu.sem.collision.shapes.CircleShape;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.collisionsystem.nodes.ColliderNode;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IMeleeWeapon;
import dk.sdu.sem.commonweapon.WeaponComponent;
import dk.sdu.sem.enemy.EnemyComponent;

import java.util.List;
import java.util.ServiceLoader;

public class MeleeWeapon implements IMeleeWeapon {
	private final ICollisionSPI collisionService;
	private final MeleeAnimationController animationController = new MeleeAnimationController(this);
	private Vector2D position;

	public MeleeWeapon () {
		collisionService = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);
	}

	/**
	* @param direction Direction of the attack check.
	* @param activator The entity which activates the weapon.
	*/
	@Override
	public void activateWeapon(Entity activator, Vector2D direction) {
		if (collisionService == null) {
			System.out.println("Collision service not available");
			return;
		}

		System.out.println("Attack size from weapon component: " +
				activator.getComponent(WeaponComponent.class).getAttackSize());

		// TODO Code to play attack animation
		// Could use an arc shape rather than circle shape
		//this.radius = activator.getComponent(WeaponComponent.class).getAttackSize()*direction.normalize().y();

		TransformComponent transform = activator.getComponent(TransformComponent.class);
		Vector2D position = transform.getPosition();
		float attackSize = activator.getComponent(WeaponComponent.class).getAttackSize();
		Vector2D circleCenter = position.add(direction.scale(attackSize / 2));

		System.out.println("Attack circle: center=" + circleCenter + ", radius=" + attackSize);

		// Potential enemy entities and their positions/colliders
		NodeManager.active().getNodes(ColliderNode.class).stream()
				.filter(node -> node.collider.getLayer() == PhysicsLayer.ENEMY)
				.forEach(node -> {
					Vector2D enemyPos = node.transform.getPosition();
					Vector2D colliderPos = node.collider.getWorldPosition();
					ICollisionShape shape = node.collider.getShape();
					float radius = 0;
					if (shape instanceof CircleShape) {
						radius = ((CircleShape)shape).getRadius();
					}
					System.out.println("Enemy: " + node.getEntity().getID() +
							", pos=" + enemyPos +
							", colliderPos=" + colliderPos +
							", colliderType=" + shape.getClass().getSimpleName() +
							(shape instanceof CircleShape ? ", radius=" + radius : ""));

					// Calculate distance between attack circle and enemy
					float distance = Vector2D.euclidean_distance(circleCenter, colliderPos);
					float minDistance = attackSize + radius;
					System.out.println("Distance: " + distance + ", min distance for collision: " + minDistance);
				});

		List<Entity> overlappedEntities = collisionService.overlapCircle(
				circleCenter,
				attackSize,
				PhysicsLayer.ENEMY
		);

		for (Entity entity : overlappedEntities) {
			System.out.print("\n Count of overlappedEntities: " + overlappedEntities.size());
			if (entity.hasComponent(EnemyComponent.class)) {
				System.out.println("\nPlayer overlapped an Enemy: " + entity.getID());
				if (entity.hasComponent(StatsComponent.class)) {
					System.out.println("\nEnemy has stats");
					// rounding might cause issues
					float currentHealth = entity.getComponent(StatsComponent.class).getCurrentHealth();
					entity.getComponent(StatsComponent.class).setCurrentHealth(currentHealth - 1);
				}
			}
		}
	}

	@Override
	public String getId() {
		return "melee_sweep";
	}
}