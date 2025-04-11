package dk.sdu.sem.collisionsystem.raycasting;

import dk.sdu.sem.collision.PhysicsLayer;

/**
 * Options for raycasts.
 */
// NOT USED
public class RaycastOptions {
	private int raysPerSide = 4;
	private float rayLength = 1.0f;
	private PhysicsLayer[] layerMask;
	private boolean ignoreTriggers = false;

	public static RaycastOptions defaults() {
		return new RaycastOptions();
	}

	// Getters and setters with builder pattern
	public RaycastOptions setRaysPerSide(int raysPerSide) {
		this.raysPerSide = Math.max(1, raysPerSide);
		return this;
	}

	public RaycastOptions setRayLength(float rayLength) {
		this.rayLength = rayLength;
		return this;
	}

	public RaycastOptions setLayerMask(PhysicsLayer[] layerMask) {
		this.layerMask = layerMask;
		return this;
	}

	public RaycastOptions setIgnoreTriggers(boolean ignoreTriggers) {
		this.ignoreTriggers = ignoreTriggers;
		return this;
	}

	public int getRaysPerSide() { return raysPerSide; }
	public float getRayLength() { return rayLength; }
	public PhysicsLayer[] getLayerMask() { return layerMask; }
	public boolean isIgnoreTriggers() { return ignoreTriggers; }
}