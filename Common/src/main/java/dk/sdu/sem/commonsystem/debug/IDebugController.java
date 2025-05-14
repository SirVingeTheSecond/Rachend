package dk.sdu.sem.commonsystem.debug;

import java.util.ServiceLoader;

/**
 * Central controller interface for managing debug visualization modes.
 */
public interface IDebugController {
	/** Toggle collider visualization (F5) */
	void toggleColliderVisualization();

	/** Toggle raycast visualization (F6) */
	void toggleRaycastVisualization();

	/** Toggle pathfinding visualization (F7) */
	void togglePathfindingVisualization();

	/** Check if collider visualization is enabled */
	boolean isColliderVisualizationEnabled();

	/** Check if raycast visualization is enabled */
	boolean isRaycastVisualizationEnabled();

	/** Check if pathfinding visualization is enabled */
	boolean isPathfindingVisualizationEnabled();

	/** Set collider visualization enabled state */
	void setColliderVisualizationEnabled(boolean enabled);

	/** Set raycast visualization enabled state */
	void setRaycastVisualizationEnabled(boolean enabled);

	/** Set pathfinding visualization enabled state */
	void setPathfindingVisualizationEnabled(boolean enabled);

	/**
	 * Gets the controller instance.
	 * This provides consistent access to the implementation across modules.
	 */
	static IDebugController getInstance() {
		return ServiceLoader.load(IDebugController.class)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("No IDebugController implementation found"));
	}
}