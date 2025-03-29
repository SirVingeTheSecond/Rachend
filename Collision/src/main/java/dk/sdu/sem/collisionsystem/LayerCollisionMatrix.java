package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.PhysicsLayer;

/**
 * Manages which physics layers can collide with each other.
 */
public class LayerCollisionMatrix {
	// Maximum number of supported layers (can be expanded if needed)
	private static final int MAX_LAYERS = 32;
	private static final boolean DEBUG = false;

	// Matrix where [a][b] is true if layer a can collide with layer b
	private final boolean[][] collisionMatrix = new boolean[MAX_LAYERS][MAX_LAYERS];

	/**
	 * Creates a new layer collision matrix with default settings.
	 */
	public LayerCollisionMatrix() {
		// Initialize matrix with default values
		resetToDefaults();
	}

	/**
	 * Resets the collision matrix to default settings.
	 * By default, all layers collide with each other except:
	 * <br>
	 * - DECORATION doesn't collide with anything
	 * <br>
	 * - PROJECTILE doesn't collide with other PROJECTILES
	 * <br>
	 * - ENEMY doesn't collide with other ENEMY
	 */
	public void resetToDefaults() {
		// Clear matrix
		for (int i = 0; i < MAX_LAYERS; i++) {
			for (int j = 0; j < MAX_LAYERS; j++) {
				collisionMatrix[i][j] = true; // Default is collide
			}
		}

		// Set up default exceptions
		PhysicsLayer decorationLayer = PhysicsLayer.DECORATION;

		// DECORATION doesn't collide with anything
		disableLayerCollisions(decorationLayer);

		// PROJECTILES don't collide with other PROJECTILES
		setLayerCollision(PhysicsLayer.PROJECTILE, PhysicsLayer.PROJECTILE, false);

		// ENEMY doesn't collide with other ENEMY
		setLayerCollision(PhysicsLayer.ENEMY, PhysicsLayer.ENEMY, false);

		// IMPORTANT: Make sure PLAYER can collide with ITEM!
		setLayerCollision(PhysicsLayer.PLAYER, PhysicsLayer.ITEM, true);

		// TRIGGERS need to interact with other layers
		setLayerCollision(PhysicsLayer.TRIGGER, PhysicsLayer.PLAYER, true);

		// Items don't need to collide with each other
		setLayerCollision(PhysicsLayer.ITEM, PhysicsLayer.ITEM, false);

		if (DEBUG) {
			debugLayerCollisions();
		}
	}

	/**
	 * Sets whether two layers can collide with each other.
	 * This is commutative - setting (A,B) also sets (B,A).
	 *
	 * @param layerA First layer
	 * @param layerB Second layer
	 * @param collide True if the layers can collide, false otherwise
	 */
	public void setLayerCollision(PhysicsLayer layerA, PhysicsLayer layerB, boolean collide) {
		collisionMatrix[layerA.getValue()][layerB.getValue()] = collide;
		collisionMatrix[layerB.getValue()][layerA.getValue()] = collide;
	}

	/**
	 * Enables collision between two layers.
	 * This is commutative - enabling (A,B) also enables (B,A).
	 *
	 * @param layerA First layer
	 * @param layerB Second layer
	 */
	public void enableLayerCollision(PhysicsLayer layerA, PhysicsLayer layerB) {
		setLayerCollision(layerA, layerB, true);
	}

	/**
	 * Disables collision between two layers.
	 * This is commutative - disabling (A,B) also disables (B,A).
	 *
	 * @param layerA First layer
	 * @param layerB Second layer
	 */
	public void disableLayerCollision(PhysicsLayer layerA, PhysicsLayer layerB) {
		setLayerCollision(layerA, layerB, false);
	}

	/**
	 * Disables collisions for a layer with all other layers.
	 *
	 * @param layer The layer to disable collisions for
	 */
	public void disableLayerCollisions(PhysicsLayer layer) {
		int layerValue = layer.getValue();
		for (int i = 0; i < MAX_LAYERS; i++) {
			collisionMatrix[layerValue][i] = false;
			collisionMatrix[i][layerValue] = false;
		}
	}

	/**
	 * Checks if two layers can collide with each other.
	 *
	 * @param layerA First layer
	 * @param layerB Second layer
	 * @return True if the layers can collide, false otherwise
	 */
	public boolean canLayersCollide(PhysicsLayer layerA, PhysicsLayer layerB) {
		return collisionMatrix[layerA.getValue()][layerB.getValue()];
	}

	/**
	 * Debugging method to check layer collisions
	 */
	public void debugLayerCollisions() {
		System.out.println("DEBUG: Physics Layer Collision Matrix:");
		System.out.println("  - PLAYER vs ITEM: " + canLayersCollide(PhysicsLayer.PLAYER, PhysicsLayer.ITEM));
		System.out.println("  - PLAYER vs TRIGGER: " + canLayersCollide(PhysicsLayer.PLAYER, PhysicsLayer.TRIGGER));
		System.out.println("  - ITEM vs TRIGGER: " + canLayersCollide(PhysicsLayer.ITEM, PhysicsLayer.TRIGGER));
		System.out.println("  - ITEM vs ITEM: " + canLayersCollide(PhysicsLayer.ITEM, PhysicsLayer.ITEM));
	}
}