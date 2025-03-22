package dk.sdu.sem.gamesystem;

import dk.sdu.sem.gamesystem.assets.AssetManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Monitors memory usage and logs significant changes.
 * This can help identify memory leaks and track resource usage.
 */
public class MemoryMonitor implements IUpdate {
	private long lastUsedMem = 0;
	private long lastTotalMem = 0;
	private final long reportThresholdMB = 10; // Report when memory changes by this many MB
	private int updateCounter = 0;
	private final int logInterval = 300; // Log every 300 frames (about 5 seconds at 60fps)
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	// Memory high water mark
	private long peakMemoryUsed = 0;

	// Asset counts at last report
	private int lastAssetCount = 0;

	@Override
	public void update() {
		updateCounter++;

		// Get current memory stats
		Runtime runtime = Runtime.getRuntime();
		long totalMemory = runtime.totalMemory() / (1024 * 1024);
		long freeMemory = runtime.freeMemory() / (1024 * 1024);
		long usedMemory = totalMemory - freeMemory;

		// Get asset count
		int assetCount = AssetManager.getInstance().getLoadedAssetCount();
		int descriptorCount = AssetManager.getInstance().getDescriptorCount();

		// Update peak memory
		if (usedMemory > peakMemoryUsed) {
			peakMemoryUsed = usedMemory;
		}

		// Log periodically or on significant changes
		boolean memoryChanged = Math.abs(usedMemory - lastUsedMem) >= reportThresholdMB;
		boolean totalChanged = Math.abs(totalMemory - lastTotalMem) >= reportThresholdMB;
		boolean assetsChanged = assetCount != lastAssetCount;

		if (memoryChanged || totalChanged || assetsChanged || updateCounter % logInterval == 0) {
			String timestamp = timeFormat.format(new Date());
			StringBuilder sb = new StringBuilder();

			sb.append(String.format("[%s] Memory: %d MB used / %d MB total (%.1f%%), ",
				timestamp, usedMemory, totalMemory,
				(usedMemory * 100.0f) / totalMemory));

			// Add trending indicator
			if (usedMemory > lastUsedMem) {
				sb.append(String.format("UP | %d MB", usedMemory - lastUsedMem));
			} else if (usedMemory < lastUsedMem) {
				sb.append(String.format("DOWN | %d MB", lastUsedMem - usedMemory));
			} else {
				sb.append("―");
			}

			// Add peak memory
			sb.append(String.format(", Peak: %d MB", peakMemoryUsed));

			// Add asset stats
			sb.append(String.format(" | Assets: %d loaded, %d descriptors",
				assetCount, descriptorCount));

			// Add frame count
			sb.append(String.format(" | Frame: %d", updateCounter));

			System.out.println(sb.toString());

			// Update the last values
			lastUsedMem = usedMemory;
			lastTotalMem = totalMemory;
			lastAssetCount = assetCount;
		}
	}

	/**
	 * Explicitly requests garbage collection and logs memory stats
	 * Should only be called at scene transitions.
	 */
	public void forceGarbageCollection() {
		// Log pre-GC state
		Runtime runtime = Runtime.getRuntime();
		long beforeTotal = runtime.totalMemory() / (1024 * 1024);
		long beforeFree = runtime.freeMemory() / (1024 * 1024);
		long beforeUsed = beforeTotal - beforeFree;

		System.out.println("[GC] Before collection: " + beforeUsed + " MB used / " + beforeTotal + " MB total");

		// Request garbage collection
		System.gc();

		// Give GC some time to run
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// Ignore
		}

		// Log post-GC state
		long afterTotal = runtime.totalMemory() / (1024 * 1024);
		long afterFree = runtime.freeMemory() / (1024 * 1024);
		long afterUsed = afterTotal - afterFree;

		System.out.println("[GC] After collection: " + afterUsed + " MB used / " + afterTotal + " MB total");
		System.out.println("[GC] Memory freed: " + (beforeUsed - afterUsed) + " MB");

		// Update monitoring state
		lastUsedMem = afterUsed;
		lastTotalMem = afterTotal;
	}
}