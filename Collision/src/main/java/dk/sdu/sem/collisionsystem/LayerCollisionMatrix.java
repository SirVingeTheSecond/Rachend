package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.data.PhysicsLayer;

/**
 * Manages which physics layers can collide with each other.
 */
public class LayerCollisionMatrix {
	// Matrix where [a][b] is true if layer a can collide with layer b
	private final boolean[][] collisionMatrix;

	/**
	 * Creates a new layer collision matrix with default settings.
	 */
	public LayerCollisionMatrix() {
		// Get number of layers from enum
		int layerCount = PhysicsLayer.values().length;
		collisionMatrix = new boolean[layerCount][layerCount];

		// Initialize matrix with default values
		resetToDefaults();
	}

	/**
	 * Resets the collision matrix to default settings.
	 * By default, all layers collide with each other except for specific rules.
	 */
	public void resetToDefaults() {
		// Set all to true by default
		for (int i = 0; i < collisionMatrix.length; i++) {
			for (int j = 0; j < collisionMatrix[i].length; j++) {
				collisionMatrix[i][j] = true;
			}
		}

		// DECORATION doesn't collide with anything
		disableLayerCollisions(PhysicsLayer.DECORATION);

		// PROJECTILES don't collide with other PROJECTILES
		setLayerCollision(PhysicsLayer.PLAYER_PROJECTILE, PhysicsLayer.PLAYER_PROJECTILE, false);
		setLayerCollision(PhysicsLayer.ENEMY_PROJECTILE, PhysicsLayer.ENEMY_PROJECTILE, false);
		setLayerCollision(PhysicsLayer.PLAYER_PROJECTILE, PhysicsLayer.ENEMY_PROJECTILE, false);

		// Projectiles collide with ENEMY, PLAYER, and OBSTACLE layers
		setLayerCollision(PhysicsLayer.PLAYER_PROJECTILE, PhysicsLayer.ENEMY, true);
		setLayerCollision(PhysicsLayer.ENEMY_PROJECTILE, PhysicsLayer.PLAYER, true);
		setLayerCollision(PhysicsLayer.PLAYER_PROJECTILE, PhysicsLayer.OBSTACLE, true);
		setLayerCollision(PhysicsLayer.ENEMY_PROJECTILE, PhysicsLayer.OBSTACLE, true);

		setLayerCollision(PhysicsLayer.PLAYER_PROJECTILE, PhysicsLayer.PLAYER, false);
		setLayerCollision(PhysicsLayer.ENEMY_PROJECTILE, PhysicsLayer.ENEMY, false);

		setLayerCollision(PhysicsLayer.HOLE, PhysicsLayer.ENEMY_PROJECTILE, false);
		setLayerCollision(PhysicsLayer.HOLE, PhysicsLayer.PLAYER_PROJECTILE½, false);

		// ENEMY doesn't collide with other ENEMY (prevent enemies from stacking)
		setLayerCollision(PhysicsLayer.ENEMY, PhysicsLayer.ENEMY, false);

		// ITEM doesn't collide with other ITEMS, but does collide with PLAYER
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.ITEM, false);
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.PLAYER, true);

		// Disable unneeded collisions for better performance
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.ENEMY, false);
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.PLAYER_PROJECTILE, false);
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.ENEMY_PROJECTILE, false);


		// TRIGGER layer collisions
		setLayerCollision(PhysicsLayer.TRIGGER, PhysicsLayer.PLAYER, true);
		setLayerCollision(PhysicsLayer.TRIGGER, PhysicsLayer.ENEMY, true);
		setLayerCollision(PhysicsLayer.TRIGGER, PhysicsLayer.ENEMY_PROJECTILE, false);
		setLayerCollision(PhysicsLayer.TRIGGER, PhysicsLayer.PLAYER_PROJECTILE, false);


		// Explicitly ensure PLAYER and ENEMY can collide with each other
		setLayerCollision(PhysicsLayer.PLAYER, PhysicsLayer.ENEMY, true);
	}

	/**
	 * Sets whether two layers can collide with each other.
	 * This is commutative - setting (A,B) also sets (B,A).
	 */
	public void setLayerCollision(PhysicsLayer layerA, PhysicsLayer layerB, boolean collide) {
		collisionMatrix[layerA.getValue()][layerB.getValue()] = collide;
		collisionMatrix[layerB.getValue()][layerA.getValue()] = collide;
	}

	/**
	 * Disables all collisions for a specific layer.
	 */
	public void disableLayerCollisions(PhysicsLayer layer) {
		int layerValue = layer.getValue();
		for (int i = 0; i < collisionMatrix.length; i++) {
			collisionMatrix[layerValue][i] = false;
			collisionMatrix[i][layerValue] = false;
		}
	}

	/**
	 * Checks if two layers can collide with each other.
	 */
	public boolean canLayersCollide(PhysicsLayer layerA, PhysicsLayer layerB) {
		return collisionMatrix[layerA.getValue()][layerB.getValue()];
	}
}