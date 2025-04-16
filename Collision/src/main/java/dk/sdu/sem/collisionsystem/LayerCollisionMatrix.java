package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.PhysicsLayer;

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
	 * By default, all layers collide with each other except:
	 * - DECORATION doesn't collide with anything
	 * - PROJECTILE doesn't collide with other PROJECTILES
	 * - ENEMY doesn't collide with other ENEMY
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
		setLayerCollision(PhysicsLayer.PROJECTILE, PhysicsLayer.PROJECTILE, false);

		// ENEMY doesn't collide with other ENEMY
		setLayerCollision(PhysicsLayer.ENEMY, PhysicsLayer.ENEMY, true);

		// ITEM doesn't collide with other ITEMS, but does collide with PLAYER
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.ITEM, false);
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.PLAYER, true);

		// TRIGGER layer collisions
		setLayerCollision(PhysicsLayer.TRIGGER, PhysicsLayer.PLAYER, true);
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