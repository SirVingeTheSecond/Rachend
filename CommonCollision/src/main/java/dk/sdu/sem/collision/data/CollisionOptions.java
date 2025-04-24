package dk.sdu.sem.collision.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CollisionOptions {
	private final boolean preventStaticCollisions;
	private final boolean preventDynamicCollisions;
	private final Set<PhysicsLayer> ignoreLayers;
	private boolean triggerEvents;

	private CollisionOptions(boolean preventStaticCollisions, boolean preventDynamicCollisions, Set<PhysicsLayer> ignoreLayers, boolean triggerEvents) {
		this.preventStaticCollisions = preventStaticCollisions;
		this.preventDynamicCollisions = preventDynamicCollisions;
		this.ignoreLayers = Collections.unmodifiableSet(ignoreLayers != null ? new HashSet<>(ignoreLayers) : new HashSet<>());
		this.triggerEvents = triggerEvents;
	}

	public boolean shouldPreventStaticCollisions() {
		return preventStaticCollisions;
	}

	public boolean shouldPreventDynamicCollisions() {
		return preventDynamicCollisions;
	}

	public Set<PhysicsLayer> getIgnoreLayers() {
		return ignoreLayers;
	}

	public boolean shouldIgnoreLayer(PhysicsLayer layer) {
		return ignoreLayers.contains(layer);
	}

	// Factory methods
	public static CollisionOptions preventAll(boolean triggerEvents) {
		return new CollisionOptions(true, true, null, triggerEvents);
	}

	public static CollisionOptions preventStaticOnly(boolean triggerEvents) {
		return new CollisionOptions(true, false, null, triggerEvents);
	}

	public static CollisionOptions custom(boolean preventStatic, boolean preventDynamic, Set<PhysicsLayer> ignoreLayers, boolean triggerEvents) {
		return new CollisionOptions(preventStatic, preventDynamic, ignoreLayers, triggerEvents);
	}

	public boolean isTriggerEvents() {
		return triggerEvents;
	}

	public void setTriggerEvents(boolean triggerEvents) {
		this.triggerEvents = triggerEvents;
	}
}