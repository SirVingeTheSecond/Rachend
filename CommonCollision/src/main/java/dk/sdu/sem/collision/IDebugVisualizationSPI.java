package dk.sdu.sem.collision;

/**
 * Service Provider Interface for debug visualization services
 */
public interface IDebugVisualizationSPI {
	/**
	 * Toggle debug visualization on/off
	 */
	void toggleDebugVisualization();

	/**
	 * Toggle collider visualization on/off
	 */
	void toggleColliderVisualization();

	/**
	 * Set debug visualization enabled state
	 * @param enabled Whether debug visualization should be enabled
	 */
	void setDebugVisualizationEnabled(boolean enabled);

	/**
	 * Set collider visualization enabled state
	 * @param enabled Whether collider visualization should be enabled
	 */
	void setColliderVisualizationEnabled(boolean enabled);

	/**
	 * Check if debug visualization is enabled
	 * @return True if debug visualization is enabled
	 */
	boolean isDebugVisualizationEnabled();

	/**
	 * Check if collider visualization is enabled
	 * @return True if collider visualization is enabled
	 */
	boolean isColliderVisualizationEnabled();
}