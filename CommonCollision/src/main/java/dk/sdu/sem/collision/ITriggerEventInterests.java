package dk.sdu.sem.collision;

import dk.sdu.sem.commonsystem.IComponent;
import java.util.Set;

// This should be optional!
public interface ITriggerEventInterests {
	Set<Class<? extends IComponent>> getComponentsOfInterest();
}