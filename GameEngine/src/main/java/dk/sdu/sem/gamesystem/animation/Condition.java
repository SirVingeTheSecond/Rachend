package dk.sdu.sem.gamesystem.animation;

import java.util.Map;


/**
 * Interface for transition conditions.
 */
public interface Condition {
	boolean evaluate(Map<String, Object> parameters);
}
